/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.lists.ListDeviceItemDto;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Device")
@RequestMapping("/rest")
public class DeviceController extends AbstractController implements DeviceConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceDao dao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceCommandService commandService;
    @Autowired
    private DeviceResolver deviceResolver;

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
            checkAccess(user, Role.NormalUser);

            final Device d = createSerializer(user).parseDevice(device);

            final Device old = dao.findByImei(d.getImei());
            checkCompanyAccess(user, old);

            d.setCompany(user.getCompany());
            if (!Role.Admin.hasRole(user)) {
                //not admin can't change active state
                d.setActive(old.isActive());
            }

            dao.save(d);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
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
            checkAccess(user, Role.BasicUser);

            final DeviceSerializer ser = createSerializer(user);
            final DateFormat isoFormat = DateTimeUtils.createDateFormat(user, "yyyy-MM-dd HH:mm");

            final List<ListDeviceItem> devices = dao.getDevices(user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 1),
                    page);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final ListDeviceItem item : devices) {
                array.add(ser.toJson(createDto(item, isoFormat, user.getTemperatureUnits())));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param item
     * @param isoFormat
     * @param temperatureUnits
     * @return
     */
    private ListDeviceItemDto createDto(final ListDeviceItem item, final DateFormat isoFormat,
            final TemperatureUnits temperatureUnits) {
        final ListDeviceItemDto dto = new ListDeviceItemDto();
        dto.setActive(item.isActive());
        dto.setDescription(item.getDescription());
        dto.setImei(item.getImei());
        dto.setSn(Device.getSerialNumber(item.getImei()));
        dto.setName(item.getName());
        dto.setAutostartTemplateId(item.getAutostartTemplateId());
        dto.setAutostartTemplateName(item.getAutostartTemplateName());

        if (item.getLastReadingTime() != null) {
            dto.setLastReadingTimeISO(isoFormat.format(item.getLastReadingTime()));
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
                PROPERTY_LAST_READING_TIME,
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
            checkAccess(user, Role.BasicUser);

            final Device device = dao.findByImei(imei);
            checkCompanyAccess(user, device);

            return createSuccessResponse(createSerializer(user).toJson(device));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param imei device ID.
     * @return device.
     */
    @RequestMapping(value = "/deleteDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteDevice(@PathVariable final String authToken,
            @RequestParam final String imei) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Device d = dao.findOne(imei);
            checkCompanyAccess(user, d);

            dao.delete(d);
            return createSuccessResponse(null);
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
            final @RequestParam String imei,
            final @RequestParam(required = false) Long shipmentId) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            //get device to shutdown.
            final Device device = dao.findByImei(imei);
            checkCompanyAccess(user, device);

            Shipment shipment;
            if (shipmentId != null) {
                shipment = shipmentDao.findOne(shipmentId);
                if (shipment != null && !shipment.getDevice().getImei().equals(imei)) {
                    //check device is for given shipment.
                    log.error("Shipment " + shipmentId + " device " + shipment.getDevice().getImei()
                            + " not matches the shutting down device " + imei);
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Shipment " + shipmentId + " users another device than " + imei);
                }
            } else {
                shipment = shipmentDao.findLastShipment(imei);
            }

            checkCompanyAccess(user, shipment);
            stopShipmentAndShutdownDevice(shipment, device);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to shutdown device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param shipment
     * @param device
     */
    private void stopShipmentAndShutdownDevice(final Shipment shipment, final Device device) {
        commandService.shutdownDevice(device, new Date());

        if (shipment != null) {
            //stop shipment
            final ShipmentStatus status = shipment.getStatus();
            if (status == ShipmentStatus.Arrived) {
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
        }
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
