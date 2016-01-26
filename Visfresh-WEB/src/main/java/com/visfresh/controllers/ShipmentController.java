/**
 *
 */
package com.visfresh.controllers;


import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.UserResolver;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentDtoNew;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.io.shipment.SingleShipmentTimeItem;
import com.visfresh.rules.AlertDescriptionBuilder;
import com.visfresh.services.ArrivalEstimation;
import com.visfresh.services.ArrivalEstimationService;
import com.visfresh.services.LocationService;
import com.visfresh.services.ShipmentSiblingService;
import com.visfresh.services.lists.ListShipmentItem;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Shipment")
@RequestMapping("/rest")
public class ShipmentController extends AbstractController implements ShipmentConstants {
    private static final String ISO_FORMAT = "yyyy-MM-dd HH:mm";
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);
    /**
     * Report service.
     */
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private ReferenceResolver referenceResolver;
    @Autowired
    private UserResolver userResolver;
    @Autowired
    private ArrivalEstimationService arrivalEstimationService;
    @Autowired
    private AlertDescriptionBuilder alertDescriptionBuilder;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ShipmentSiblingService siblingService;

//    //start time
//    private static ThreadLocal<DateFormat> ISO_FORMAT = new ThreadLocal<DateFormat>() {
//        /* (non-Javadoc)
//         * @see java.lang.ThreadLocal#initialValue()
//         */
//        @Override
//        protected DateFormat initialValue() {
//            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        }
//    };
//    private static ThreadLocal<DateFormat> PRETTY_FORMAT = new ThreadLocal<DateFormat>() {
//        /* (non-Javadoc)
//         * @see java.lang.ThreadLocal#initialValue()
//         */
//        @Override
//        protected DateFormat initialValue() {
//            //9:40pm 12-Aug-2014
//            return new SimpleDateFormat("h:mm d'-'MMM'-'yyyy");
//        }
//    };


    /**
     * Default constructor.
     */
    public ShipmentController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param shipment shipment.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    public JsonObject saveShipment(@PathVariable final String authToken,
            final @RequestBody JsonObject shipment) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipment(user);

            final SaveShipmentRequest req = getSerializer(user).parseSaveShipmentRequest(shipment);
            checkCompanyAccess(user, req.getShipment());

            req.getShipment().setCompany(user.getCompany());
            final Long id = shipmentDao.save(req.getShipment()).getId();

            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = createShipmentTemplate(
                        user.getCompany(), req.getShipment(), req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(getSerializer(user).toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    private Long createShipmentTemplate(final Company company, final Shipment shipment, final String templateName) {
        final ShipmentTemplate tpl = new ShipmentTemplate(shipment);
        tpl.setCompany(company);
        tpl.setAddDateShipped(true);
        tpl.setDetectLocationForShippedFrom(false);
        tpl.setName(templateName);
        return shipmentTemplateDao.save(tpl).getId();
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.POST)
    public JsonObject getShipments(@PathVariable final String authToken,
            @RequestBody final JsonObject request) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final ShipmentSerializer ser = getSerializer(user);
            final GetFilteredShipmentsRequest req = ser.parseGetFilteredShipmentsRequest(request);

            final Integer pageIndex = req.getPageIndex();
            final Integer pageSize = req.getPageSize();
            final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

            final Filter filter = createFilter(req, ser);
            final List<ListShipmentItem> shipments = getShipments(
                    user.getCompany(),
                    createSorting(
                            req.getSortColumn(),
                            req.getSortOrder(),
                            getDefaultListShipmentsSortingOrder(), 2),
                    filter,
                    page);
            final int total = shipmentDao.getEntityCount(user.getCompany(), filter);

            final JsonArray array = new JsonArray();
            for (final ListShipmentItem s : shipments) {
                array.add(ser.toJson(s));
            }
            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            PROPERTY_SHIPMENT_DESCRIPTION,
            PROPERTY_SHIPMENT_ID,
            PROPERTY_ALERT_PROFILE_ID,
            PROPERTY_SHIPPED_FROM,
            PROPERTY_SHIPPED_TO,
            PROPERTY_COMMENTS_FOR_RECEIVER,
            PROPERTY_ALERT_PROFILE
        };
    }
    private Filter createFilter(final GetFilteredShipmentsRequest req, final ShipmentSerializer ser) {
        Date shippedFrom = req.getShipmentDateFrom();
        Date shippedTo = req.getShipmentDateTo();

        //date ranges
        if (shippedFrom == null || shippedTo == null) {
            shippedTo = new Date();
            final long oneDay = 24 * 60 * 60 * 1000l;

            if (Boolean.TRUE.equals(req.getLastDay())) {
                shippedFrom = new Date(shippedTo.getTime() - oneDay);
            } else if (Boolean.TRUE.equals(req.getLast2Days())) {
                shippedFrom = new Date(shippedTo.getTime() - 2 * oneDay);
            } else if (Boolean.TRUE.equals(req.getLastWeek())) {
                shippedFrom = new Date(shippedTo.getTime() - 7 * oneDay);
            } else if (Boolean.TRUE.equals(req.getLastMonth())) {
                shippedFrom = new Date(shippedTo.getTime() - 31 * oneDay);
            } else {
                //two weeks by default
                shippedFrom = new Date(shippedTo.getTime() - 14 * oneDay);
            }
        }

        final Filter f = new Filter();
        if (shippedFrom != null) {
            f.addFilter(PROPERTY_SHIPPED_FROM_DATE, shippedFrom);
        }
        if (shippedTo != null) {
            f.addFilter(PROPERTY_SHIPPED_TO_DATE, shippedTo);
        }
        if (req.getShipmentDescription() != null) {
            f.addFilter(PROPERTY_SHIPMENT_DESCRIPTION, req.getShipmentDescription());
        }
        if (req.getDeviceImei() != null) {
            f.addFilter(PROPERTY_DEVICE_IMEI, req.getDeviceImei());
        }
        if (req.getStatus() != null) {
            f.addFilter(PROPERTY_STATUS, req.getStatus());
        }
        if (req.getShippedFrom() != null && !req.getShippedFrom().isEmpty()) {
            f.addFilter(PROPERTY_SHIPPED_FROM, req.getShippedFrom());
        }
        if (req.getShippedTo() != null && !req.getShippedTo().isEmpty()) {
            f.addFilter(PROPERTY_SHIPPED_TO, req.getShippedTo());
        }
        if (req.isAlertsOnly()) {
            f.addFilter(PROPERTY_ONLY_WITH_ALERTS, Boolean.TRUE);
        }
        return f;
    }
    /**
     * @param company
     * @return
     */
    private List<ListShipmentItem> getShipments(final Company company,
            final Sorting sorting,
            final Filter filter,
            final Page page) {
        final List<Shipment> shipments = shipmentDao.findByCompany(company, sorting, page, filter);
        final List<ListShipmentItem> result = new LinkedList<ListShipmentItem>();
        //add alerts to each shipment.
        for (final Shipment s : shipments) {
            final List<Alert> alerts = alertDao.getAlerts(s);
            final ListShipmentItem dto = new ListShipmentItem(s);
            dto.setSiblingCount(siblingService.getSiblingCount(s));
            dto.getAlertSummary().putAll(toSummaryMap(alerts));
            result.add(dto);
        }
        return result;
    }

    /**
     * @param authToken authentication token.
     * @param shipmentId shipment ID.
     * @return shipment.
     */
    @RequestMapping(value = "/getShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final Shipment shipment = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, shipment);

            return createSuccessResponse(getSerializer(user).toJson(shipment));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private ShipmentSerializer getSerializer(final User user) {
        final ShipmentSerializer s = new ShipmentSerializer(user);
        s.setReferenceResolver(referenceResolver);
        s.setUserResolver(userResolver);
        return s;
    }
    @RequestMapping(value = "/deleteShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipment(user);

            final Shipment s = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, s);

            shipmentDao.delete(s);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
//
//    @RequestMapping(value = "/getSingleShipmentOld/{authToken}", method = RequestMethod.GET)
//    public JsonObject getShipmentData(@PathVariable final String authToken,
//            @RequestParam final Long shipmentId) {
//
//        try {
//            //check logged in.
//            final User user = getLoggedInUser(authToken);
//            security.checkCanGetShipmentData(user);
//
//            final ShipmentSerializer ser = getSerializer(user);
//
//            final Shipment s = shipmentDao.findOne(shipmentId);
//            checkCompanyAccess(user, s);
//            if (s == null) {
//                return null;
//            }
//
//            final SingleShipmentDto dto = getShipmentData(s, true);
//
//            return createSuccessResponse(dto == null ? null : ser.toJson(dto));
//        } catch (final Exception e) {
//            log.error("Failed to get devices", e);
//            return createErrorResponse(e);
//        }
//    }
    @RequestMapping(value = "/getSingleShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject getSingleShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentData(user);

            final ShipmentSerializer ser = getSerializer(user);

            final Shipment s = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, s);
            if (s == null) {
                return createSuccessResponse(null);
            }

            final SingleShipmentDtoNew dto = createDto(s, user);

            //add siblings
            final List<Shipment> siblings = siblingService.getSiblings(s);
            for (final Shipment sibling : siblings) {
                dto.getSiblings().add(createDto(sibling, user));
            }

            return createSuccessResponse(dto == null ? null : ser.toJson(dto));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param s
     * @param user
     * @param addAllReadings
     * @return
     */
    protected SingleShipmentDtoNew createDto(final Shipment s, final User user) {
        final SingleShipmentDto dtoOld = getShipmentData(s);
        final String description = dtoOld.getShipmentDescription();
        if (description != null && description.toLowerCase().contains("test")) {
            generateTestData(dtoOld, s);
        }

        final SingleShipmentDtoNew dto = createNewSingleShipmentDate(s, dtoOld, user);

        double minTemp = 1000.;
        double maxTemp = -273.;
        Double lastReadingTemperature = null;
        long timeOfFirstReading = System.currentTimeMillis();
        long timeOfLastReading = 0;

        for (final SingleShipmentTimeItem item : dtoOld.getItems()) {
            final double t = item.getEvent().getTemperature();
            final long time = item.getEvent().getTime().getTime();

            if (t < minTemp) {
                minTemp = t;
            }
            if (t > maxTemp) {
                maxTemp = t;
            }

            if (timeOfFirstReading > time) {
                timeOfFirstReading = time;
            }
            if (timeOfLastReading < time) {
                timeOfLastReading = time;
                lastReadingTemperature = t;
            }
        }

        dto.setMinTemp(minTemp);
        dto.setMaxTemp(maxTemp);

        final DateFormat isoFmt = createDateFormat(user, ISO_FORMAT);

        dto.setTimeOfFirstReading(isoFmt.format(new Date(timeOfFirstReading)));

        //last readings
        if (lastReadingTemperature != null) {
            final Date lastReadingTime = new Date(timeOfFirstReading);
            dto.setLastReadingTimeIso(isoFmt.format(lastReadingTime));
            dto.setLastReadingTemperature(lastReadingTemperature.doubleValue());
        }

        dto.getAlertSummary().addAll(dtoOld.getAlertSummary().keySet());
        dto.setAlertYetToFire(buildAlertYetToFire(s.getAlertProfile(), user));

        final Arrival arrival = getArrival(dtoOld);
        if (arrival != null) {
            //"arrivalNotificationTimeISO": "2014-08-12 12:10",
            // NEW - ISO for actual time arrival notification sent out
            dto.setArrivalNotificationTimeIso(isoFmt.format(arrival.getDate()));
            dto.setArrivalTimeIso(dto.getArrivalNotificationTimeIso());
        }

        final Date shutdownTime = s.getDeviceShutdownTime();
        if (shutdownTime != null) {
            dto.setShutdownTimeIso(isoFmt.format(shutdownTime));
        }

        return dto;
    }
    /**
     * @param dtoOld
     * @return arrival if found
     */
    private Arrival getArrival(final SingleShipmentDto dtoOld) {
        final LinkedList<SingleShipmentTimeItem> items = new LinkedList<SingleShipmentTimeItem>(
                dtoOld.getItems());
        Collections.reverse(items);

        for (final SingleShipmentTimeItem item : items) {
            final List<Arrival> arrivals = item.getArrivals();
            if (!arrivals.isEmpty()) {
                return arrivals.get(0);
            }
        }

        return null;
    }
    /**
     * @param p alert profile.
     * @param user current user.
     * @return temperature alerts yet to fire so user can suppress future notifications.
     */
    private String buildAlertYetToFire(final AlertProfile p, final User user) {
        if (p == null) {
            return null;
        }

        final List<String> list = new LinkedList<>();
        for (final AlertRule rule: p.getAlertRules()) {
            switch (rule.getType()) {
                case Cold:
                case CriticalCold:
                case Hot:
                case CriticalHot:
                    list.add(alertDescriptionBuilder.alertRuleToString(
                            rule, user.getTemperatureUnits()));
                    break;
                    default:
                        //nothing
            }
        }
        return StringUtils.combine(list, ", ");
    }
    /**
     * @param dto
     */
    private void generateTestData(final SingleShipmentDto dto, final Shipment s) {
        final int count = 20;
        final long ddate = 5 * 60 * 1000l;
        final long t0 = System.currentTimeMillis() - ddate * count;
        dto.setShipmentDate(new Date(t0));

        if (s.getShippedFrom() == null || s.getShippedTo() == null) {
            return;
        }
        final List<Alert> allAlerts = new LinkedList<>();

        final double lat0 = s.getShippedFrom().getLocation().getLatitude();
        double nlat = lat0 - s.getShippedTo().getLocation().getLatitude();
        final double lon0 = s.getShippedFrom().getLocation().getLongitude();
        double nlon = lon0 - s.getShippedTo().getLocation().getLongitude();
        final double norma = Math.sqrt(nlat * nlat + nlon * nlon);

        //normalize n
        nlat /= norma;
        nlon /= norma;

        final Random random = new Random();
        final double dl = norma / (count + 5);
        for (int i = 0; i < count; i++) {
            final double lat;
            final double lon;

            if (dl > 0) {
                lat = lat0 + i * dl * nlat;
                lon = lon0 + i * dl * nlon;
            } else {
                lat = lat0;
                lon = lon0;
            }

            final Date date = new Date(t0 + i * ddate);
            final TrackerEvent e = new TrackerEvent();
            e.setDevice(s.getDevice());
            e.setId((long) i);
            e.setTime(date);
            e.setLatitude(lat);
            e.setLongitude(lon);
            e.setTemperature(13. + 3. * (random.nextDouble() - 0.5));
            e.setShipment(s);
            e.setType(TrackerEventType.AUT);

            final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
            item.setEvent(e);

            if (i % 5 == 0) {
                //create alert
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(e.getTemperature());
                ta.setDate(date);
                ta.setId((long) i);
                ta.setType(AlertType.Hot);
                ta.setShipment(s);
                ta.setDevice(s.getDevice());
                item.getAlerts().add(ta);
                allAlerts.add(ta);
            }

            dto.getItems().add(item);
        }

        dto.getAlertSummary().putAll(toSummaryMap(allAlerts));
    }
    /**
     * @param dtoOld
     * @return
     */
    private SingleShipmentDtoNew createNewSingleShipmentDate(final Shipment shipment,
            final SingleShipmentDto dtoOld, final User user) {
        //"startTimeISO": "2014-08-12 12:10",
        final DateFormat isoFmt = createDateFormat(user, ISO_FORMAT);

        final SingleShipmentDtoNew dto = new SingleShipmentDtoNew();
        dto.setAlertProfileId(dtoOld.getAlertProfileId());
        dto.setAlertProfileName(dtoOld.getAlertProfileName());

        dto.setAlertSuppressionMinutes(dtoOld.getAlertSuppressionMinutes());
        dto.setArrivalNotificationWithinKm(dtoOld.getArrivalNotificationWithInKm());
        dto.setAssetNum(dtoOld.getAssetNum());
        dto.setAssetType(dtoOld.getAssetType());
        dto.setCommentsForReceiver(dtoOld.getCommentsForReceiver());
        dto.setCurrentLocation(dtoOld.getCurrentLocation());

        final List<SingleShipmentTimeItem> items = dtoOld.getItems();
        if (items.size() > 0) {
            final TrackerEvent lastEvent = items.get(items.size() - 1).getEvent();
            dto.setCurrentLocationForMap(new Location(lastEvent.getLatitude(), lastEvent.getLongitude()));
        }

        dto.setDeviceName(dtoOld.getDeviceName());
        dto.setDeviceSN(dtoOld.getDeviceSn());
        if (shipment.getShippedTo() != null) {
            dto.setEndLocation(shipment.getShippedTo().getName());
            dto.setEndLocationForMap(shipment.getShippedTo().getLocation());

            if (shipment.getShippedFrom() != null
                    && dto.getCurrentLocation() != null
                    && dto.getCurrentLocationForMap() != null) {
                //get last location
                final ArrivalEstimation estimation = arrivalEstimationService.estimateArrivalDate(
                        shipment, dto.getCurrentLocationForMap(), new Date());
                if (estimation != null) {
                    dto.setEta(isoFmt.format(estimation.getArrivalDate()));
                    dto.setPercentageComplete(estimation.getPercentageComplete());
                }
            }
        }

        dto.setExcludeNotificationsIfNoAlerts(dtoOld.isExcludeNotificationsIfNoAlertsFired());
        dto.setPalletId(dtoOld.getPalletId());
        dto.setShipmentDescription(dtoOld.getShipmentDescription());
        dto.setShipmentId(shipment.getId());
        if (shipment.getAlertProfile() != null) {
            dto.setShutdownDeviceAfterMinutes(shipment.getShutdownDeviceTimeOut());
        }
        if (shipment.getShippedFrom() != null) {
            dto.setStartLocation(shipment.getShippedFrom().getName());
            dto.setStartLocationForMap(shipment.getShippedFrom().getLocation());
        }
        final Date date = shipment.getShipmentDate();

        dto.setStartTimeISO(isoFmt.format(date));
        dto.setStatus(shipment.getStatus());
        dto.setTripCount(dtoOld.getTripCount());

        //"6:47pm"
        final DateFormat shortFormat = createDateFormat(user, "h:mmaa");

        int i = 0;
        for (final SingleShipmentTimeItem item : items) {
            dto.getLocations().addAll(createLocations(item, isoFmt, shortFormat,
                    user, dto.getEta(), dto.getEndLocation(), i == items.size() - 1));
            i++;
        }

        dto.getAlertsNotificationSchedules().addAll(dtoOld.getAlertsNotificationSchedules());
        dto.getArrivalNotificationSchedules().addAll(dtoOld.getArrivalNotificationSchedules());
        return dto;
    }
    /**
     * @param item
     * @return
     */
    private List<SingleShipmentLocation> createLocations(
            final SingleShipmentTimeItem item,
            final DateFormat timeIsoFmt,
            final DateFormat shortFormat,
            final User user,
            final String etaIso,
            final String shippedTo,
            final boolean isLast) {
        final List<SingleShipmentLocation> list = new LinkedList<SingleShipmentLocation>();

        final TrackerEvent event = item.getEvent();

        //create location
        final SingleShipmentLocation lo = new SingleShipmentLocation();
        lo.setLatitude(event.getLatitude());
        lo.setLongitude(event.getLongitude());
        lo.setTemperature(event.getTemperature());
        lo.setTimeIso(timeIsoFmt.format(event.getTime()));
        list.add(lo);

        final String address = this.locationService.getLocationDescription(
                new Location(event.getLatitude(), event.getLongitude()));

        //add tracker event
        if (isLast) {
            lo.getAlerts().add(createLastReadingAlert(event, user, address, lo.getTimeIso()));
        }

        //add alerts
        for (final Alert a : item.getAlerts()) {
            lo.getAlerts().add(createSingleShipmentAlert(a, user, address));
        }
        //add arrivals
        for (final Arrival a: item.getArrivals()) {
            lo.getAlerts().add(createSingleShipmentAlert(a, user, address, lo.getTimeIso()));
        }

        return list;
    }
    /**
     * @param event event.
     * @param user user
     * @param address address.
     * @param timeIso time in ISO format
     * @return
     */
    private SingleShipmentAlert createLastReadingAlert(final TrackerEvent event,
            final User user, final String address, final String timeIso) {
        final SingleShipmentAlert alert = new SingleShipmentAlert();

        alert.setTitle(alertDescriptionBuilder.buildLastReadingDescription(event, user));
        alert.setType("LastReading");
        alert.getLines().add(alertDescriptionBuilder.buildShortDescription(event, user));
        alert.getLines().add(address);

        return alert;
    }
    /**
     * @param a arrival.
     * @param user user.
     * @param address address.
     * @param timeIso time ISO.
     * @return
     */
    private SingleShipmentAlert createSingleShipmentAlert(final Arrival a, final User user,
            final String address, final String timeIso) {
        final SingleShipmentAlert alert = new SingleShipmentAlert();

        alert.setTitle(alertDescriptionBuilder.buildDescription(a, user));
        alert.setType("ArrivalNotice");
        alert.getLines().add(alertDescriptionBuilder.buildShortDescription(a, user));
        alert.getLines().add(address);

        return alert;
    }
    /**
     * @param a
     * @param user user.
     * @param address address
     * @return
     */
    private SingleShipmentAlert createSingleShipmentAlert(
            final Alert a, final User user, final String address) {
        final SingleShipmentAlert alert = new SingleShipmentAlert();

        alert.setTitle(alertDescriptionBuilder.buildDescription(a, user));
        alert.setType(a.getType().name());
        alert.getLines().add(alertDescriptionBuilder.buildShortDescription(a, user));
        alert.getLines().add(address);

        return alert;
    }
    /**
     * @param s shipment.
     * @param addAllReadings whether or not should include alerts.
     * @return single shipment data.
     */
    private SingleShipmentDto getShipmentData(final Shipment s) {
        final SingleShipmentDto dto = creatSingleShipmentDto(s);

        //create best tracker event candidate/alert map
        final List<TrackerEvent> events = trackerEventDao.getEvents(s);
        for (final TrackerEvent e : events) {
            final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
            item.setEvent(e);
            dto.getItems().add(item);
        }

        if (events.size() > 0) {
            //add alerts
            final List<Alert> alerts = alertDao.getAlerts(s);
            for (final Alert alert : alerts) {
                final SingleShipmentTimeItem item = getBestCandidate(dto.getItems(), alert.getDate());
                item.getAlerts().add(alert);
            }

            dto.getAlertSummary().putAll(toSummaryMap(alerts));
        }
        return dto;
    }
    /**
     * @param alerts
     * @return
     */
    public static  Map<AlertType, Integer> toSummaryMap(
            final List<Alert> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final Alert alert : alerts) {
            Integer numAlerts = map.get(alert.getType());
            if (numAlerts == null) {
                numAlerts = 0;
            }
            numAlerts = numAlerts + 1;
            map.put(alert.getType(), numAlerts);
        }
        return map;
    }
    /**
     * @param items
     * @param date
     * @return
     */
    private SingleShipmentTimeItem getBestCandidate(final List<SingleShipmentTimeItem> items, final Date date) {
        for (final SingleShipmentTimeItem i : items) {
            if (i.getEvent().getTime().equals(date) || i.getEvent().getTime().after(date)) {
                return i;
            }
        }
        return items.get(items.size() - 1);
    }

    /**
     * @param shipment
     * @return
     */
    private SingleShipmentDto creatSingleShipmentDto(final Shipment shipment) {
        return new SingleShipmentDto(shipment);
    }
}
