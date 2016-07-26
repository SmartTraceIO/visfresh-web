/**
 *
 */
package com.visfresh.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.lists.DeviceDto;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.services.EmailService;
import com.visfresh.services.ShipmentShutdownService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Device")
@RequestMapping("/rest")
public class DeviceController extends AbstractController implements DeviceConstants {
    private static MediaType TEXT_PLAIN = MediaType.parseMediaType("text/plain");
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceDao dao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AutoStartShipmentDao autoStartShipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceCommandService commandService;
    @Autowired
    private DeviceGroupDao deviceGroupDao;
    @Autowired
    private DeviceResolver deviceResolver;
    @Autowired
    private ShipmentShutdownService shutdownService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public DeviceController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param device device.
     * @return ID of saved device.
     */
    @RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    public JsonObject saveDevice(@PathVariable final String authToken,
            final @RequestBody JsonObject device) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final DeviceSerializer ser = createSerializer(user);
            Device d = ser.parseDevice(device);

            final Device old = dao.findByImei(d.getImei());
            if (old == null && !Role.SmartTraceAdmin.hasRole(user)) {
                log.error("User " + user.getEmail() + " attempts to add new device " + d.getImei());
                return createErrorResponse(ErrorCodes.SECURITY_ERROR, "Only SmartTrace admin can add new device");
            }
            checkCompanyAccess(user, old);

            if (old != null) {
                //first of all merge saved device with existing. It needs for
                //avoid of rewriting to NULL values
                final JsonObject mereged = SerializerUtils.merge(device, ser.toJson(old));
                d = ser.parseDevice(mereged);
                d.setCompany(old.getCompany());

                //not allow to overwrite trip count
                d.setTripCount(old.getTripCount());

                final boolean oldActive = old.isActive();
                if (!Role.Admin.hasRole(user)) {
                    //not admin can't change active state
                    d.setActive(old.isActive());
                }

                if (oldActive != d.isActive()) {
                    final StringBuilder msg = new StringBuilder("User ");
                    msg.append(user.getEmail());
                    msg.append(" set device ");
                    msg.append(d.getImei());
                    msg.append(" to ");
                    if (d.isActive()) {
                        msg.append("active");
                    } else {
                        msg.append("inactive");
                    }
                    msg.append(" state");

                    //notify support team.
                    emailService.sendMessageToSupport("Device " + d.getImei() + " state changed", msg.toString());
                }
            } else {
                d.setCompany(user.getCompany());
            }

            dao.save(d);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/moveDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject moveDevice(@PathVariable final String authToken,
            final @RequestParam String device,
            final @RequestParam Long company) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.SmartTraceAdmin);

            //get device
            final Device oldDevice = dao.findByImei(device);
            if (oldDevice == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Not found device for moving to another company " + device);
            } else if (isVirtualDevice(oldDevice)) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Can't move device beause is virtual " + device);
            }

            //get company
            final Company c = companyDao.findOne(company);
            if (c == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Target company for moving device is not found " + company);
            }

            //create new device
            final Device newDevice = createVistualDevice(oldDevice);

            //switch device to new company
            dao.moveToNewCompany(oldDevice, c);

            deviceCommandDao.deleteCommandsFor(oldDevice);
            deviceGroupDao.moveToNewDevice(oldDevice, newDevice);
            arrivalDao.moveToNewDevice(oldDevice, newDevice);
            alertDao.moveToNewDevice(oldDevice, newDevice);
            trackerEventDao.moveToNewDevice(oldDevice, newDevice);
            shipmentDao.moveToNewDevice(oldDevice, newDevice);

            //close all active shipments
            final List<Shipment> activeShipments = shipmentDao.findActiveShipments(newDevice.getImei());
            for (final Shipment s : activeShipments) {
                if (!s.hasFinalStatus()) {
                    s.setStatus(ShipmentStatus.Ended);
                    shipmentDao.save(s);
                }
            }

            return createIdResponse("deviceImei", newDevice.getImei());
        } catch (final Exception e) {
            log.error("Failed to move device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param device device.
     * @return true if the device is virtual.
     */
    private boolean isVirtualDevice(final Device device) {
        return device.getImei().startsWith(createVirtualPrefix(device));
    }
    /**
     * @param device
     * @return
     */
    private Device createVistualDevice(final Device device) {
        final Device d = new Device();
        d.setActive(device.isActive());
        d.setAutostartTemplateId(null);
        d.setCompany(device.getCompany());
        d.setDescription(device.getDescription());
        d.setImei(createVirtualPrefix(device) + device.getImei());
        d.setName(device.getName());
        d.setTripCount(device.getTripCount());
        d.setColor(device.getColor());
        return deviceDao.save(d);
    }
    /**
     * @param device device.
     * @return virtual prefix for given device.
     */
    private String createVirtualPrefix(final Device device) {
        return device.getCompany().getId() + "_";
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of devices.
     */
    @RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    public JsonObject getDevices(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final DeviceSerializer ser = createSerializer(user);

            final List<ListDeviceItem> devices = dao.getDevices(user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 1),
                    page);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final ListDeviceItem item : devices) {
                array.add(ser.toJson(createDto(item, user)));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param item
     * @param user
     * @return
     */
    private DeviceDto createDto(final ListDeviceItem item,final User user) {
        final DateFormat isoFormat = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFormat = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());
        final TemperatureUnits temperatureUnits = user.getTemperatureUnits();

        final DeviceDto dto = new DeviceDto();
        dto.setActive(item.isActive());
        dto.setDescription(item.getDescription());
        dto.setImei(item.getImei());
        dto.setSn(Device.getSerialNumber(item.getImei()));
        dto.setName(item.getName());
        dto.setAutostartTemplateId(item.getAutostartTemplateId());
        dto.setAutostartTemplateName(item.getAutostartTemplateName());
        if (item.getColor() != null) {
            dto.setColor(item.getColor().name());
        }

        if (item.getLastReadingTime() != null) {
            dto.setLastReadingTimeISO(isoFormat.format(item.getLastReadingTime()));
            dto.setLastReadingTime(prettyFormat.format(item.getLastReadingTime()));
            dto.setLastReadingBattery(item.getBattery());
            dto.setLastReadingLat(item.getLatitude());
            dto.setLastReadingLong(item.getLongitude());
            dto.setLastReadingTemperature(LocalizationUtils.getTemperatureString(
                    item.getTemperature(), temperatureUnits));

            if (item.getShipmentId() != null) {
                dto.setLastShipmentId(item.getShipmentId());
                dto.setShipmentNumber(dto.getSn() + "(" + item.getTripCount() + ")");
                dto.setShipmentStatus(item.getShipmentStatus().name());
            }
        }

        return dto;
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
                PROPERTY_IMEI,
                PROPERTY_NAME,
                PROPERTY_DESCRIPTION,
                PROPERTY_ACTIVE,
                PROPERTY_SN,
                PROPERTY_AUTOSTART_TEMPLATE_ID,
                PROPERTY_AUTOSTART_TEMPLATE_NAME,
                PROPERTY_SHIPMENT_NUMBER,
                PROPERTY_LAST_SHIPMENT,
                PROPERTY_LAST_READING_LAT,
                PROPERTY_LAST_READING_LONG,
                PROPERTY_LAST_READING_BATTERY,
                PROPERTY_LAST_READING_TEMPERATURE,
                PROPERTY_LAST_READING_TIME_ISO,
                PROPERTY_SHIPMENT_STATUS
        };
    }
    /**
     * @param authToken authentication token.
     * @param imei device ID.
     * @return device.
     */
    @RequestMapping(value = "/getDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject getDevice(@PathVariable final String authToken,
            @RequestParam final String imei) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Device device = dao.findByImei(imei);
            checkCompanyAccess(user, device);

            //create result
            final ListDeviceItem item = new ListDeviceItem(device);
            //add last reading date
            final ShortTrackerEvent e = trackerEventDao.getLastEvent(device);
            if (e != null) {
                item.setLastReadingTime(e.getTime());
                item.setBattery(e.getBattery());
                item.setLatitude(e.getLatitude());
                item.setLongitude(e.getLongitude());
                item.setTemperature(e.getTemperature());

                if (e.getShipmentId() != null) {
                    item.setShipmentId(e.getShipmentId());
                    final Shipment s = shipmentDao.findOne(e.getShipmentId());
                    item.setShipmentStatus(s.getStatus());
                }
            }

            //add autostart template data
            if (device.getAutostartTemplateId() != null) {
                final AutoStartShipment aut = autoStartShipmentDao.findOne(device.getAutostartTemplateId());
                if (aut != null) {
                    item.setAutostartTemplateName(aut.getTemplate().getName());
                }
            }

            //format result
            return createSuccessResponse(createSerializer(user).toJson(
                    createDto(item, user)));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/sendCommandToDevice/{authToken}", method = RequestMethod.POST)
    public JsonObject sendCommandToDevice(@PathVariable final String authToken,
            final @RequestBody JsonObject req) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.Admin);

            final DeviceCommand cmd = createSerializer(user).parseDeviceCommand(req);
            checkCompanyAccess(user, cmd.getDevice());

            commandService.sendCommand(cmd, new Date());

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/shutdownDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject shutdownDevice(@PathVariable final String authToken,
            final @RequestParam Long shipmentId) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Shipment shipment = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, shipment);

            //get device to shutdown.
            final Device device = shipment.getDevice();
            if (device == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Shipment " + shipmentId + " has not assigned device");
            }
            checkCompanyAccess(user, device);

            stopShipmentAndShutdownDevice(shipment, device);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to shutdown device for shipment " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/initDeviceColors/{authToken}", method = RequestMethod.GET)
    public JsonObject initDeviceColors(@PathVariable final String authToken,
            final @RequestParam(required = false) Long company) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.Admin);

            final Company c;
            if (company != null) {
                c = companyDao.findOne(company);
            } else {
                c = user.getCompany();
            }

            checkCompanyAccess(user, c);
            final List<Device> devices = deviceDao.findByCompany(c, null, null, null);

            final ColorInitializeTool tool = new ColorInitializeTool();
            tool.initColors(devices);

            //save colors.
            for (final Device d : devices) {
                deviceDao.updateColor(d, d.getColor());
                log.debug("Color " + d.getColor().name() + " has set for device " + d.getImei());
            }
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to initialize device colors for company " + company, e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getReadings/{authToken}",
            method = RequestMethod.GET, produces = "text/plain")
    public ResponseEntity<?> getTrackerEvents(
            @PathVariable final String authToken,
            @RequestParam(value = "startDate", required = false) final String startDateArg,
            @RequestParam(value = "endDate", required = false) final String endDateArg,
            @RequestParam(value = "device", required = false) final String deviceImei
            ) throws Exception {

        //check logged in.
        final User user = getLoggedInUser(authToken);
        checkAccess(user, Role.BasicUser);

        final Device device = deviceDao.findByImei(deviceImei);

        checkCompanyAccess(user, device);
        if (device == null) {
            return badRequest("Device " + deviceImei + " not found");
        }

        //create date format
        final DateFormat df = DateTimeUtils.createDateFormat(
                "yyyy-MM-dd'T'HH-mm-ss", user.getLanguage(), user.getTimeZone());

        Date startDate = null;
        Date endDate = null;

        if (startDateArg != null || endDateArg != null) {
            //start date
            if (startDateArg != null) {
                startDate = df.parse(startDateArg);
            }
            //end date
            if (endDateArg != null) {
                endDate = df.parse(endDateArg);
            }
        }

        //correct null date ranges
        if (endDate == null) {
            endDate = new Date();
        }
        if (startDate == null) {
            startDate = new Date(endDate.getTime() - 30 * 24 * 60 * 60 * 1000l);
        }

        final List<ShortTrackerEvent> events = trackerEventDao.findBy(device.getImei(), startDate, endDate);

        //create tmp file with report PDF content.
        final File file = createTmpFile("readings");

        try {
            final OutputStream out = new FileOutputStream(file);
            try {
                readingsToCsv(events, out, df);
            } finally {
                out.close();
            }
        } catch (final Throwable e) {
            log.error("Failed to create pefromance report", e);
            file.delete();
            throw new IOException("Failed to create performance report", e);
        }

        final InputStream in = new TmpFileInputStream(file);
        return ResponseEntity
                .ok()
                .contentType(TEXT_PLAIN)
                .contentLength(file.length())
                .body(new InputStreamResource(in, file.getName()));
    }

    /**
     * @param events tracker events.
     * @param out CSV output stream.
     * @throws IOException
     */
    public static void readingsToCsv(final List<ShortTrackerEvent> events, final OutputStream out, final DateFormat fmt)
            throws IOException {
        //+-------------+--------------+------+-----+---------+----------------+
        //| Field       | Type         | Null | Key | Default | Extra          |
        //+-------------+--------------+------+-----+---------+----------------+
        //| id          | bigint(20)   | NO   | PRI | NULL    | auto_increment |
        //| type        | varchar(20)  | NO   |     | NULL    |                |
        //| time        | timestamp    | YES  |     | NULL    |                |
        //| battery     | int(11)      | NO   |     | NULL    |                |
        //| temperature | double       | NO   |     | NULL    |                |
        //| latitude    | double       | YES  |     | NULL    |                |
        //| longitude   | double       | YES  |     | NULL    |                |
        //| device      | varchar(127) | NO   | MUL | NULL    |                |
        //| shipment    | bigint(20)   | YES  | MUL | NULL    |                |
        //| createdon   | timestamp    | YES  |     | NULL    |                |
        //+-------------+--------------+------+-----+---------+----------------+

        out.write("id,type,time,battery,temperature,latitude,longitude,device,shipment,createdon".getBytes());
        out.write((byte) '\n');

        //print headers
        for (final ShortTrackerEvent e : events) {
            final StringBuilder sb = new StringBuilder();
            out.write(sb.toString().getBytes());

            //| id          | bigint(20)   | NO   | PRI | NULL    | auto_increment |
            out.write(Long.toString(e.getId()).getBytes());
            out.write((byte) ',');
            //| type        | varchar(20)  | NO   |     | NULL    |                |
            out.write(e.getType().name().getBytes());
            out.write((byte) ',');
            //| time        | timestamp    | YES  |     | NULL    |                |
            out.write(fmt.format(e.getTime()).getBytes());
            out.write((byte) ',');
            //| battery     | int(11)      | NO   |     | NULL    |                |
            out.write(Integer.toString(e.getBattery()).getBytes());
            out.write((byte) ',');
            //| temperature | double       | NO   |     | NULL    |                |
            out.write(Double.toString(e.getTemperature()).getBytes());
            out.write((byte) ',');
            //| latitude    | double       | YES  |     | NULL    |                |
            out.write(Double.toString(e.getLatitude()).getBytes());
            out.write((byte) ',');
            //| longitude   | double       | YES  |     | NULL    |                |
            out.write(Double.toString(e.getLongitude()).getBytes());
            out.write((byte) ',');
            //| device      | varchar(127) | NO   | MUL | NULL    |                |
            out.write(e.getDeviceImei().getBytes());
            out.write((byte) ',');
            //| shipment    | bigint(20)   | YES  | MUL | NULL    |                |
            if (e.getShipmentId() != null) {
                out.write(Long.toString(e.getShipmentId()).getBytes());
            }
            out.write((byte) ',');
            //| createdon   | timestamp    | YES  |     | NULL    |                |
            out.write(fmt.format(e.getCreatedOn()).getBytes());
            //+-------------+--------------+------+-----+---------+----------------+
            out.write((byte) '\n');
        }

        out.flush();
    }
    /**
     * @param name resource name.
     * @return temporary file.
     * @throws IOException
     */
    private File createTmpFile(final String name) throws IOException {
        return File.createTempFile("visfreshtmp-", "-" + name);
    }
    /**
     * @param logMessage log message.
     * @return empty response body.
     */
    protected ResponseEntity<ByteArrayInputStream> badRequest(final String logMessage) {
        log.error(logMessage);
        return ResponseEntity
                .status(HttpServletResponse.SC_BAD_REQUEST)
                .contentLength(0)
                .body(new ByteArrayInputStream(new byte[0]));
    }
    /**
     * @param shipment
     * @param device
     */
    private void stopShipmentAndShutdownDevice(final Shipment shipment, final Device device) {
        //stop shipment
        final ShipmentStatus status = shipment.getStatus();
        if (shipment.hasFinalStatus()) {
            //if status = "Arrived" (shutdown) => status='arrived"
            //[20:24:37] James Richardson: if stayts = 'Ended' (shutown) ==> status = 'Ended
            //do nothing
            log.debug("Shipment " + shipment.getId() + " is already in final status " + status);
        } else {
            //[20:23:46] James Richardson: if status = "InProgress" (shutdown) ==> status = 'ended'
            //[20:24:21] James Richardson: if status = 'Default' (shutdown) ==> statsus- 'Ended'
            shipment.setStatus(ShipmentStatus.Ended);
            shipmentDao.save(shipment);
            log.debug("Shipment " + shipment.getId() + " status has set to " + ShipmentStatus.Ended
                    + " according device " + device.getImei() + " shutdown");
        }

        shutdownService.sendShipmentShutdown(shipment, new Date());
    }
    /**
     * @param user
     * @return
     */
    private DeviceSerializer createSerializer(final User user) {
        final DeviceSerializer deviceSerializer = new DeviceSerializer(user.getTimeZone());
        deviceSerializer.setDeviceResolver(deviceResolver);
        return deviceSerializer;
    }
}
