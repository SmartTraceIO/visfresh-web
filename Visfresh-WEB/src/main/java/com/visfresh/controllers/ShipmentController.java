/**
 *
 */
package com.visfresh.controllers;


import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Note;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.KeyLocation;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.io.shipment.SingleShipmentTimeItem;
import com.visfresh.l12n.ChartBundle;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.services.LocationService;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.ShipmentSiblingService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Shipment")
@RequestMapping("/rest")
public class ShipmentController extends AbstractShipmentBaseController implements ShipmentConstants {
    /**
     * 2 hours by default.
     */
    private static final long MAX_DEFAULT_SHIPMENT_INACTIVE_TIME = 2 * 60 * 60 * 1000L;
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

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
    private ChartBundle chartBundle;
    @Autowired
    private LocationService locationService;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private ShipmentSiblingService siblingService;
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private RuleBundle ruleBundle;
    @Autowired
    private AlternativeLocationsDao alternativeLocationsDao;
    @Autowired
    private InterimStopDao interimStopDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private AutoStartShipmentService autoStartService;
    @Autowired
    private DeviceGroupDao deviceGroupDao;

    /**
     * Default constructor.
     */
    public ShipmentController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    public JsonObject saveShipment(@PathVariable final String authToken,
            final @RequestBody JsonObject jsonRequest) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final ShipmentSerializer serializer = getSerializer(user);
            Long id = serializer.getShipmentIdFromSaveRequest(jsonRequest);
            Shipment oldShipment = null;

            if (id != null) {
                //merge the shipment from request by existing shipment
                //it is required to avoid the set to null the fields
                //which are absent in save request.
                oldShipment = shipmentDao.findOne(id);
                if (oldShipment != null) {
                    checkCompanyAccess(user, oldShipment);

                    final JsonObject shipmentFromReqest = serializer.getShipmentFromRequest(
                            jsonRequest);
                    final JsonObject merged = SerializerUtils.merge(
                            shipmentFromReqest,
                            serializer.toJson(new ShipmentDto(oldShipment)).getAsJsonObject());
                    //correct shipment to save in request
                    serializer.setShipmentToRequest(jsonRequest, merged);
                }
            }

            final SaveShipmentRequest req = serializer.parseSaveShipmentRequest(jsonRequest);

            //save shipment
            final Shipment newShipment = createShipment(req.getShipment(), oldShipment);
            resolveReferences(user, req.getShipment(), newShipment);

            newShipment.setCompany(user.getCompany());
            newShipment.setCreatedBy(user.getEmail());

            if (id != null) {
                shipmentDao.save(newShipment);
            } else {
                id = saveNewShipment(newShipment, !Boolean.FALSE.equals(req.isIncludePreviousData()));
            }

            saveInterimLoations(user, newShipment, req.getShipment().getInterimLocations());
            updateInterimStops(newShipment, req.getShipment().getInterimStops());

            //build response
            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = createShipmentTemplate(
                        user.getCompany(), newShipment, req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(serializer.toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save shipment by request: " + jsonRequest, e);
            return createErrorResponse(e);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.AbstractShipmentBaseController#saveInterimLoations(com.visfresh.entities.ShipmentBase, java.util.Collection)
     */
    @Override
    protected void saveInterimLoations(final ShipmentBase s,
            final Collection<LocationProfile> locs) {
        super.saveInterimLoations(s, locs);
        ruleEngine.setInterimLocations(s, new LinkedList<>(locs));
    }
    /**
     * @param dto
     * @param oldShipment
     * @return
     */
    private Shipment createShipment(final ShipmentDto dto, final Shipment oldShipment) {
        final boolean isNew = oldShipment == null;
        final Shipment s = isNew ? new Shipment() : oldShipment;
        copyBaseData(dto, s);

        s.setPalletId(dto.getPalletId());
        s.setAssetNum(dto.getAssetNum());
        s.setTripCount(dto.getTripCount());
        s.setPoNum(dto.getPoNum());
        s.setAssetType(dto.getAssetType());
        s.getCustomFields().putAll(dto.getCustomFields());

        s.setShipmentDate(isNew || dto.getShipmentDate() == null
                ? new Date() : dto.getShipmentDate());
        if (!isNew && dto.getLastEventDate() != null) {
            s.setLastEventDate(dto.getLastEventDate());
        }
        s.setStartDate(isNew || dto.getStartDate() == null
                ? new Date() : dto.getStartDate());

        s.setCreatedBy(dto.getCreatedBy());
        s.setStatus(dto.getStatus());
        if (!isNew) {
            s.setDeviceShutdownTime(dto.getDeviceShutdownTime());
            s.setArrivalDate(dto.getArrivalDate());
            s.setEta(dto.getEta());
        }

        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.AbstractShipmentBaseController#resolveReferences(com.visfresh.entities.User, com.visfresh.io.ShipmentBaseDto, com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected void resolveReferences(final User user, final ShipmentBaseDto dto,
            final ShipmentBase t) throws RestServiceException {
        final ShipmentDto shipmentDto = (ShipmentDto) dto;
        final Shipment s = (Shipment) t;

        final Device d = deviceDao.findByImei(shipmentDto.getDeviceImei());
        checkCompanyAccess(user, d);
        s.setDevice(d);

        super.resolveReferences(user, dto, t);
    }
    /**
     * @param newShipment
     * @return
     */
    private Long saveNewShipment(final Shipment newShipment, final boolean includePreviousData) {
        final String imei = newShipment.getDevice().getImei();
        final Shipment current = includePreviousData ? shipmentDao.findLastShipment(imei) : null;

        boolean shouldOverwritePrevious =
                includePreviousData
                && current != null
                && current.getStatus() == ShipmentStatus.Default;
        if (shouldOverwritePrevious) {
            final TrackerEvent e = trackerEventDao.getLastEvent(current);
            // e == null is not mandatory check because the Default shipment can be created only
            // by autocreate rule in case of INIT event.
            shouldOverwritePrevious = e == null
                    || e.getTime().getTime() + MAX_DEFAULT_SHIPMENT_INACTIVE_TIME > System.currentTimeMillis();
        }

        if (shouldOverwritePrevious) {
            log.debug("Found Default shipment " + current.getId()
                    + " for device " + imei + ". Will reused instead of crate new");
            //copy all settings from created shipment to current.
            newShipment.setId(current.getId());
            newShipment.setStatus(ShipmentStatus.InProgress);
            newShipment.setTripCount(current.getTripCount());
        }

        //add date shipped
        final String dateShipped = DateTimeUtils.formatShipmentDate(
                newShipment.getCompany(), newShipment.getShipmentDate());
        String desc = newShipment.getShipmentDescription();
        if (desc == null) {
            desc = dateShipped;
        } else {
            desc += " " + dateShipped;
        }

        newShipment.setShipmentDescription(desc);
        newShipment.setStartDate(new Date());

        final Long resultId = shipmentDao.save(newShipment).getId();

        if (!shouldOverwritePrevious && current != null && !current.hasFinalStatus()) {
            log.debug("Status " + ShipmentStatus.Ended + " has set for " + current.getId()
                    + " after create new shipment for device " + imei);
            current.setStatus(ShipmentStatus.Ended);
            shipmentDao.save(current);
        }

        return resultId;
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
            checkAccess(user, Role.NormalUser);

            final ShipmentSerializer ser = getSerializer(user);
            final GetFilteredShipmentsRequest req = ser.parseGetFilteredShipmentsRequest(request);

            final Integer pageIndex = req.getPageIndex();
            final Integer pageSize = req.getPageSize();
            final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

            final Filter filter = createFilter(req);
            final List<ListShipmentItem> shipments = getShipments(
                    user.getCompany(),
                    createSortingShipments(
                            req.getSortColumn(),
                            req.getSortOrder(),
                            getDefaultListShipmentsSortingOrder(), 2),
                    filter,
                    page, user);
            final int total = shipmentDao.getEntityCount(user.getCompany(), filter);

            //add interim stops
            addInterimStops(shipments, user);

            //add events data
            addKeyLocations(shipments, user);

            final JsonArray array = new JsonArray();
            for (final ListShipmentItem s : shipments) {
                array.add(ser.toJson(s));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get shipments", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param shipments
     * @throws ParseException
     */
    private void addKeyLocations(final List<ListShipmentItem> shipments, final User user) throws ParseException {
        final Collection<Long> shipmentIds = EntityUtils.getIdList(shipments);
        final Map<Long, List<TrackerEventDto>> eventMap = trackerEventDao.getEventsForShipmentIds(
                shipmentIds);
        //alerts
        final Map<Long, List<Alert>> alertMap = alertDao.getAlertsForShipmentIds(
                shipmentIds);
        final DateFormat format = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        //events
        for (final ListShipmentItem s : shipments) {
            final List<TrackerEventDto> events = eventMap.get(s.getId());

            final List<KeyLocation> keyLocs = buildKeyLocations(events);
            addInterimStopKeyLocations(keyLocs, s.getInterimStops(), user.getTimeZone());

            //add events
            if (events.size() > 0) {
                //create reverse events
                final List<TrackerEventDto> reverted = new LinkedList<TrackerEventDto>(events);
                Collections.reverse(reverted);

                //shipped from
                if (s.getShippedFrom() != null) {
                    final Double lat = s.getShippedFromLat();
                    final Double lon = s.getShippedFromLong();

                    final TrackerEventDto e = findNearestEvent(lat, lon, reverted);
                    if (e != null) {
                        final KeyLocation loc = new KeyLocation();
                        loc.setKey("shippedFrom");
                        loc.setDescription(s.getShippedFrom());
                        loc.setLatitude(lat);
                        loc.setLongitude(lon);
                        loc.setTime(e.getTime().getTime());

                        insertKeyLocation(loc, keyLocs);
                    }
                }

                //shipped to
                if (s.getShippedTo() != null) {
                    final Double lat = s.getShippedToLat();
                    final Double lon = s.getShippedToLong();

                    final TrackerEventDto e = findNearestEvent(lat, lon, reverted);
                    if (e != null) {
                        final KeyLocation loc = new KeyLocation();
                        loc.setKey("shippedTo");
                        loc.setDescription(s.getShippedTo());
                        loc.setLatitude(lat);
                        loc.setLongitude(lon);
                        loc.setTime(e.getTime().getTime());

                        insertKeyLocation(loc, keyLocs);
                    }
                }

                //alerts
                boolean lightOnProcessed = false;
                for (final Alert alert : alertMap.get(s.getId())) {
                    final AlertType type = alert.getType();
                    if (type == AlertType.LightOff) {
                        continue;
                    }
                    if (type == AlertType.LightOn) {
                        if (lightOnProcessed) {
                            continue;
                        } else {
                            lightOnProcessed = true;
                        }
                    }

                    final KeyLocation loc = buildKeyLocation(alert, events, user);
                    if (loc != null) {
                        insertKeyLocation(loc, keyLocs);
                    }
                }

                //set pretty time
                for (final KeyLocation loc : keyLocs) {
                    loc.setPrettyTime(format.format(new Date(loc.getTime())));
                }
            }

            s.setKeyLocations(keyLocs);
        }
    }

    /**
     * @param alert
     * @param events
     * @return
     */
    private KeyLocation buildKeyLocation(final Alert alert,
            final List<TrackerEventDto> events, final User user) {
        final Long eventId = alert.getTrackerEventId();

        if (eventId != null) {
            for (final TrackerEventDto e : events) {
                if (e.getId().equals(eventId)) {
                    final KeyLocation loc = new KeyLocation();
                    loc.setKey(alert.getType().name() + "Alert");
                    loc.setLatitude(e.getLatitude());
                    loc.setLongitude(e.getLongitude());
                    loc.setTime(e.getTime().getTime());
                    loc.setDescription(chartBundle.buildDescription(alert, e,
                            user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits()));
                    return loc;
                }
            }
        }
        return null;
    }
    /**
     * @param lat
     * @param lon
     * @param reverted
     * @return
     */
    private TrackerEventDto findNearestEvent(final Double lat, final Double lon,
            final List<TrackerEventDto> events) {
        if (lat == null || lon == null) {
            return null;
        }

        double dl = Double.MAX_VALUE;
        TrackerEventDto e = null;

        for (final TrackerEventDto dto: events) {
            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                final double dlat = lat - dto.getLatitude();
                final double dlon = lon - dto.getLongitude();

                final double dist = Math.sqrt(dlat * dlat + dlon * dlon);
                if (dist < dl) {
                    e = dto;
                    dl = dist;
                }
            }
        }
        return e;
    }
    /**
     * @param keyLocs
     * @param interimStops
     * @throws ParseException
     */
    private void addInterimStopKeyLocations(final List<KeyLocation> keyLocs,
            final List<SingleShipmentInterimStop> interimStops, final TimeZone tz) throws ParseException {
        final DateFormat fmt = DateTimeUtils.createIsoFormat(Language.English, tz);

        for (final SingleShipmentInterimStop stp : interimStops) {
            final KeyLocation loc = createKeyLocation(stp);
            loc.setTime(fmt.parse(stp.getStopDateIso()).getTime());
            insertKeyLocation(loc, keyLocs);
        }

    }
    /**
     * @param loc location.
     * @param keyLocs location list.
     */
    private void insertKeyLocation(final KeyLocation loc,
            final List<KeyLocation> keyLocs) {
        boolean inserted = false;

        //insert to best position.
        for (int i = 1; i < keyLocs.size() - 1; i++) {
            if (keyLocs.get(i + 1).getTime() > loc.getTime()) {
                inserted = true;
                keyLocs.add(i, loc);
                break;
            }
        }

        //insert before end
        if (!inserted) {
            keyLocs.add(keyLocs.size() - 1, loc);
        }
    }
    /**
     * @param stp
     * @return
     */
    private KeyLocation createKeyLocation(final SingleShipmentInterimStop stp) {
        final KeyLocation loc = new KeyLocation();
        loc.setKey("interimStop");
        loc.setDescription(stp.getLocation().getName());
        loc.setLatitude(stp.getLatitude());
        loc.setLongitude(stp.getLongitude());
        return loc;
    }
    /**
     * @param id
     * @param events
     * @return
     */
    private List<KeyLocation> buildKeyLocations(final List<TrackerEventDto> events) {
        if (events.size() == 0) {
            return null;
        }

        final long start = events.get(0).getTime().getTime();
        final long end = events.get(events.size() - 1).getTime().getTime();

        final List<KeyLocation> locs = new LinkedList<>();
        locs.add(createKeyLocation("firstReading", findNearestEvent(start, events)));

        KeyLocation current = locs.get(0);
        for (long persent = 10; persent < 100; persent+=10) {
            final TrackerEventDto event = findNearestEvent(start + (end - start) * persent / 100, events);
            if (event.getTime().getTime() > current.getTime()) {
                final KeyLocation loc = createKeyLocation("reading", event);
                locs.add(loc);
                current = loc;
            }
        }

        locs.add(createKeyLocation("lastReading", findNearestEvent(end, events)));
        return locs;
    }
    /**
     * @param e
     * @return
     */
    private KeyLocation createKeyLocation(final String key, final TrackerEventDto e) {
        final KeyLocation loc = new KeyLocation();
        loc.setKey(key);
        loc.setLatitude(e.getLatitude());
        loc.setLongitude(e.getLongitude());
        loc.setTime(e.getTime().getTime());
        return loc;
    }
    /**
     * @param time
     * @param events
     * @return
     */
    private TrackerEventDto findNearestEvent(final long time, final List<TrackerEventDto> events) {
        long dt = Long.MAX_VALUE;

        TrackerEventDto e = null;
        for (final TrackerEventDto dto : events) {
            final long dt0 = Math.abs(dto.getTime().getTime() - time);
            if (dt0 < dt) {
                dt = dt0;
                e = dto;
            }
        }
        return e;
    }
    /**
     * @param shipments
     */
    private void addInterimStops(final List<ListShipmentItem> shipments, final User user) {
        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        final Map<Long, List<InterimStop>> stopMap = interimStopDao.getByShipmentIds(EntityUtils.getIdList(shipments));
        for (final ListShipmentItem s : shipments) {
            for (final InterimStop stop : stopMap.get(s.getId())) {
                final SingleShipmentInterimStop dto = new SingleShipmentInterimStop();
                dto.setId(stop.getId());

                final LocationProfile l = stop.getLocation();
                dto.setLatitude(l.getLocation().getLatitude());
                dto.setLongitude(l.getLocation().getLongitude());
                dto.setLocation(l);
                dto.setStopDate(prettyFmt.format(stop.getDate()));
                dto.setStopDateIso(isoFmt.format(stop.getDate()));

                s.getInterimStops().add(dto);
            }
        }
//
//"interimStop1": "ABC Warehouse",
//"interimStop1Time": "14:33 02-May-2016",
//"interimStop1TimeISO": "2016-05-02 02:32",
//
//"interimStop2": "XYZ Warehouse",
//"interimStop2Time": "14:33 02-May-2016",
//"interimStop2TimeISO": "2016-05-02 02:32",
//

    }
    private Sorting createSortingShipments(final String sc, final String so,
            final String[] defaultSortOrder, final int maxNumOfSortColumns) {
        String sortColumn;
        if (SHIPPED_FROM.equals(sc)) {
            sortColumn = SHIPPED_FROM_LOCATION_NAME;
        } else if (SHIPPED_TO.equals(sc)) {
            sortColumn = SHIPPED_TO_LOCATION_NAME;
        } else {
            sortColumn = sc;
        }
        return super.createSorting(sortColumn, so, defaultSortOrder, maxNumOfSortColumns);
    }
    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            SHIPMENT_ID,
            SHIPMENT_DATE,
            STATUS,
            SHIPPED_FROM_LOCATION_NAME,
            SHIPPED_TO_LOCATION_NAME,
            DEVICE_SN, //should add trip count next
            ARRIVAL_DATE,
            COMMENTS_FOR_RECEIVER,
            ETA,
            SHIPMENT_DESCRIPTION,
            ALERT_PROFILE_ID,
            ALERT_PROFILE,
            SIBLING_COUNT,
            LAST_READING_TIME,
            LAST_READING_TIME_ISO,
            LAST_READING_TEMPERATURE,
            ALERT_SUMMARY
        };
    }

    private Filter createFilter(final GetFilteredShipmentsRequest req) {
        Date shippedFrom = req.getShipmentDateFrom();
        final Date shippedTo = req.getShipmentDateTo();

        //date ranges
        if (shippedFrom == null || shippedTo == null) {
            final Date d = new Date();
            final long oneDay = 24 * 60 * 60 * 1000l;

            if (Boolean.TRUE.equals(req.getLastDay())) {
                shippedFrom = new Date(d.getTime() - oneDay);
            } else if (Boolean.TRUE.equals(req.getLast2Days())) {
                shippedFrom = new Date(d.getTime() - 2 * oneDay);
            } else if (Boolean.TRUE.equals(req.getLastWeek())) {
                shippedFrom = new Date(d.getTime() - 7 * oneDay);
            } else if (Boolean.TRUE.equals(req.getLastMonth())) {
                shippedFrom = new Date(d.getTime() - 31 * oneDay);
            } else {
                //two weeks by default
                shippedFrom = new Date(d.getTime() - 14 * oneDay);
            }
        }

        final Filter f = new Filter();
        if (shippedFrom != null) {
            f.addFilter(SHIPPED_FROM_DATE, shippedFrom);
        }
        if (shippedTo != null) {
            f.addFilter(SHIPPED_TO_DATE, shippedTo);
        }
        if (req.getShipmentDescription() != null) {
            f.addFilter(SHIPMENT_DESCRIPTION, req.getShipmentDescription());
        }
        if (req.getDeviceImei() != null) {
            f.addFilter(DEVICE_IMEI, req.getDeviceImei());
        }
        if (req.getStatus() != null) {
            f.addFilter(STATUS, req.getStatus());
        }
        if (req.getShippedFrom() != null && !req.getShippedFrom().isEmpty()) {
            f.addFilter(SHIPPED_FROM, req.getShippedFrom());
        }
        if (req.getShippedTo() != null && !req.getShippedTo().isEmpty()) {
            f.addFilter(SHIPPED_TO, req.getShippedTo());
        }
        if (req.isAlertsOnly()) {
            f.addFilter(ONLY_WITH_ALERTS, Boolean.TRUE);
        }
        if (req.getGoods() != null) {
            f.addFilter(GOODS, req.getGoods());
        }
        if (Boolean.TRUE.equals(req.getExcludePriorShipments())) {
            f.addFilter(EXCLUDE_PRIOR_SHIPMENTS, true);
        }
        return f;
    }
    /**
     * @param company
     * @param user the user.
     * @return
     */
    private List<ListShipmentItem> getShipments(final Company company,
            final Sorting sorting,
            final Filter filter,
            final Page page, final User user) {
        final List<Shipment> shipments = shipmentDao.findByCompany(company, sorting, page, filter);
        final List<ListShipmentItem> result = new LinkedList<ListShipmentItem>();

        final Date currentTime = new Date();
        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        //add alerts to each shipment.
        for (final Shipment s : shipments) {
            final ListShipmentItem dto = new ListShipmentItem(s);
            result.add(dto);

            //siblings.
            dto.setSiblingCount(siblingService.getSiblingCount(s));
            //alerts
            final List<Alert> alerts = alertDao.getAlerts(s);
            dto.getAlertSummary().putAll(toSummaryMap(alerts));

            //percentage complete.
            if (s.hasFinalStatus()) {
                dto.setPercentageComplete(100);
            } else {
                final Date eta = s.getEta();
                if (eta != null) {
                    dto.setEstArrivalDate(prettyFmt.format(eta));
                    dto.setEstArrivalDateISO(isoFmt.format(eta));
                    dto.setPercentageComplete(getPercentageCompleted(s, currentTime, eta));
                }
            }

            //last reading
            if (s.getLastEventDate() != null) {
                dto.setLastReadingTime(prettyFmt.format(s.getLastEventDate()));
                dto.setLastReadingTimeISO(isoFmt.format(s.getLastEventDate()));
            }

            //last event
            final TrackerEvent lastEvent = trackerEventDao.getLastEvent(s);
            if (lastEvent != null) {
                //set last reading data
                dto.setLastReadingTemperature(LocalizationUtils.convertToUnits(
                        lastEvent.getTemperature(), user.getTemperatureUnits()));
                dto.setLastReadingBattery(lastEvent.getBattery());
                dto.setLastReadingLat(lastEvent.getLatitude());
                dto.setLastReadingLong(lastEvent.getLongitude());
            }

            //first event
            final TrackerEvent firstEvent = trackerEventDao.getFirstEvent(s);
            if (firstEvent != null) {
                dto.setFirstReadingTime(prettyFmt.format(firstEvent.getTime()));
                dto.setFirstReadingTimeISO(isoFmt.format(firstEvent.getTime()));
                dto.setFirstReadingLat(firstEvent.getLatitude());
                dto.setFirstReadingLong(firstEvent.getLongitude());
            }

            if (s.getStatus() == ShipmentStatus.Default || s.getStatus() == ShipmentStatus.Ended) {
                dto.setEstArrivalDate(null);
                dto.setActualArrivalDate(null);
                dto.setActualArrivalDateISO(null);
                if (s.getStatus() == ShipmentStatus.Default) {
                    dto.setShippedTo(null);
                }
            } else if (s.getStatus() == ShipmentStatus.Arrived && s.getArrivalDate() != null) {
                //arrival date.
                dto.setActualArrivalDate(prettyFmt.format(s.getArrivalDate()));
                dto.setActualArrivalDateISO(isoFmt.format(s.getArrivalDate()));
            }

            if (s.getShipmentDate() != null) {
                dto.setShipmentDate(prettyFmt.format(s.getShipmentDate()));
                dto.setShipmentDateISO(isoFmt.format(s.getShipmentDate()));
            }

            //start location
            if (s.getShippedFrom() != null) {
                dto.setShippedFromLat(s.getShippedFrom().getLocation().getLatitude());
                dto.setShippedFromLong(s.getShippedFrom().getLocation().getLongitude());
            }
            //end location
            if (s.getShippedTo() != null) {
                dto.setShippedToLat(s.getShippedTo().getLocation().getLatitude());
                dto.setShippedToLong(s.getShippedTo().getLocation().getLongitude());
            }
        }

        return result;
    }
    /**
     * @param s
     * @param currentTime
     * @param eta
     * @return
     */
    protected int getPercentageCompleted(final Shipment s,
            final Date currentTime, final Date eta) {
        int percentage;
        if (eta.before(currentTime)) {
            percentage = 100;
        } else {
            double d = currentTime.getTime() - s.getShipmentDate().getTime();
            d = Math.max(0., d / (eta.getTime() - s.getShipmentDate().getTime()));
            percentage = (int) Math.round(d);
        }
        return percentage;
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
            checkAccess(user, Role.NormalUser);

            final Shipment shipment = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, shipment);

            final ShipmentDto dto = new ShipmentDto(shipment);
            addInterimLocations(dto, shipment);
            addInterimStops(dto, shipment);

            final JsonObject json = getSerializer(user).toJson(dto);
            return createSuccessResponse(json);
        } catch (final Exception e) {
            log.error("Failed to get shipment " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private ShipmentSerializer getSerializer(final User user) {
        return new ShipmentSerializer(user);
    }
    @RequestMapping(value = "/deleteShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteShipment(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final Shipment s = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, s);

            shipmentDao.delete(s);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete shipment " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/suppressAlerts/{authToken}", method = RequestMethod.GET)
    public JsonObject suppressAlerts(@PathVariable final String authToken,
            @RequestParam final Long shipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Shipment s = shipmentDao.findOne(shipmentId);
            checkCompanyAccess(user, s);

            if (s != null) {
                ruleEngine.suppressNextAlerts(s);
            }
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete shipment " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getSingleShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject getSingleShipment(@PathVariable final String authToken,
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final ShipmentSerializer ser = getSerializer(user);

            final Shipment s;
            if (shipmentId != null) {
                s = shipmentDao.findOne(shipmentId);
            } else {
                s = shipmentDao.findBySnTrip(user.getCompany(), sn, trip);
            }

            checkCompanyAccess(user, s);
            if (s == null) {
                return createSuccessResponse(null);
            }

            final SingleShipmentDto dto = createDto(s, user);
            addLocationAlternatives(dto, s);

            //add interim stops
            final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
            final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

            addInterimStops(dto, s, isoFmt, prettyFmt);

            //add siblings
            final List<Shipment> siblings = siblingService.getSiblings(s);
            for (final Shipment sibling : siblings) {
                dto.getSiblings().add(createDto(sibling, user));
            }

            addDeviceGroups(dto);

            return createSuccessResponse(dto == null ? null : ser.toJson(dto));
        } catch (final Exception e) {
            log.error("Failed to get single shipment: " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param dto
     */
    private void addDeviceGroups(final SingleShipmentDto dto) {
        final Map<Long, SingleShipmentDto> shipments = new HashMap<>();

        //get all shipment ID's include siblings
        final Set<Long> ids = new HashSet<>();
        ids.add(dto.getShipmentId());
        shipments.put(dto.getShipmentId(), dto);

        for (final SingleShipmentDto sibling : dto.getSiblings()) {
            ids.add(sibling.getShipmentId());
            shipments.put(sibling.getShipmentId(), sibling);
        }

        final Map<Long, List<DeviceGroupDto>> shipmentGroups = deviceGroupDao.getShipmentGroups(ids);
        //assign device groups to shipments
        for (final Map.Entry<Long, SingleShipmentDto> e: shipments.entrySet()) {
            e.getValue().getDeviceGroups().addAll(shipmentGroups.get(e.getKey()));
        }
    }

    @RequestMapping(value = "/createNewAutoSthipment/{authToken}", method = RequestMethod.GET)
    public JsonElement createNewAutoSthipment(@PathVariable final String authToken,
            @RequestParam final String device) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            //get device
            final Device d = deviceDao.findByImei(device);
            checkCompanyAccess(user, d);

            if (d == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found device with IMEI '" + device + "'");
            }

            //get last reading
            final ShortTrackerEvent e = trackerEventDao.getLastEvent(d);
            if (e == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Not last event found for device '" + device + "'");
            }

            final Shipment s = autoStartService.autoStartNewShipment(d, e.getLatitude(), e.getLongitude(), new Date());
            return createIdResponse("shipmentId", s.getId());
        } catch (final Exception e) {
            log.error("Failed to get autostart templates", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param dto DTO.
     * @param s shipment.
     */
    private void addInterimStops(final SingleShipmentDto dto, final Shipment s,
            final DateFormat isoFormat, final DateFormat prettyFormat) {
        final List<InterimStop> stops = interimStopDao.getByShipment(s);
        for (final InterimStop stp : stops) {
            final SingleShipmentInterimStop in = new SingleShipmentInterimStop();
            in.setId(stp.getId());

            final LocationProfile l = stp.getLocation();
            in.setLatitude(l.getLocation().getLatitude());
            in.setLongitude(l.getLocation().getLongitude());
            in.setLocation(l);
            in.setTime(stp.getTime());
            in.setStopDate(prettyFormat.format(stp.getDate()));
            in.setStopDateIso(isoFormat.format(stp.getDate()));

            dto.getInterimStops().add(in);
        }
    }
    /**
     * @param dto
     * @param shipment
     */
    private void addInterimStops(final ShipmentDto dto, final Shipment shipment) {
        final List<InterimStop> stops = this.interimStopDao.getByShipment(shipment);

        final List<Long> list = new LinkedList<>();
        for (final InterimStop stp : stops) {
            list.add(stp.getId());
        }

        dto.setInterimStops(list);
    }
    /**
     * Possible delete some interim stops.
     * @param s shipment base.
     * @param ids interim stops.
     * @throws RestServiceException
     */
    protected void updateInterimStops(final Shipment s, final List<Long> ids) throws RestServiceException {
        if (ids == null) {
            return;
        }

        final Set<Long> set = new HashSet<>(ids);
        final List<InterimStop> stops = this.interimStopDao.getByShipment(s);

        //save interim locations
        for (final InterimStop stp : stops) {
            if (!set.contains(stp.getId())) {
                interimStopDao.delete(s, stp);
            }
        }
    }
    /**
     * @param dto single shipment DTO.
     * @param s shipment.
     */
    private void addLocationAlternatives(final SingleShipmentDto dto, final Shipment s) {
        final AlternativeLocations alt = alternativeLocationsDao.getBy(s);
        if (alt != null) {
            dto.getStartLocationAlternatives().addAll(alt.getFrom());
            dto.getEndLocationAlternatives().addAll(alt.getTo());
            dto.getInterimLocationAlternatives().addAll(alt.getInterim());
        }
    }
    /**
     * @param s
     * @param user
     * @param addAllReadings
     * @return
     */
    protected SingleShipmentDto createDto(final Shipment s, final User user) {
        //create best tracker event candidate/alert map
        final List<TrackerEvent> events = trackerEventDao.getEvents(s);

        final List<SingleShipmentTimeItem> items = new LinkedList<>();
        for (final TrackerEvent e : events) {
            final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
            item.setEvent(e);
            items.add(item);
        }

        final SingleShipmentDto dto = createSingleShipmentData(s, user);

        final Map<AlertType, Integer> alertSummary = new HashMap<>();
        if (events.size() > 0) {
            //add battery level.
            dto.setBatteryLevel(events.get(events.size() - 1).getBattery());

            //add alerts
            final List<Alert> alerts = alertDao.getAlerts(s);
            for (final Alert alert : alerts) {
                final SingleShipmentTimeItem item = getBestCandidate(items, alert);
                item.getAlerts().add(alert);
            }

            alertSummary.putAll(toSummaryMap(alerts));

            //add arrivals
            final List<Arrival> arrivals = arrivalDao.getArrivals(s);
            for (final Arrival arrival : arrivals) {
                final SingleShipmentTimeItem item = getBestCandidate(items, arrival);
                item.getArrivals().add(arrival);
            }
        }

        double minTemp = 1000.;
        double maxTemp = -273.;
        Double lastReadingTemperature = null;
        long timeOfFirstReading = System.currentTimeMillis();
        long timeOfLastReading = 0;

        for (final TrackerEvent e : events) {
            final double t = e.getTemperature();
            final long time = e.getTime().getTime();

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

        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        dto.setTimeOfFirstReading(isoFmt.format(new Date(timeOfFirstReading)));
        dto.setFirstReadingTime(prettyFmt.format(new Date(timeOfFirstReading)));
        //last readings
        if (lastReadingTemperature != null) {
            final Date lastReadingTime = new Date(timeOfLastReading);
            dto.setLastReadingTimeIso(isoFmt.format(lastReadingTime));
            dto.setLastReadingTime(prettyFmt.format(lastReadingTime));
            dto.setLastReadingTemperature(lastReadingTemperature.doubleValue());
        }

        dto.getAlertSummary().addAll(alertSummary.keySet());
        dto.setAlertYetToFire(alertsToOneString(ruleEngine.getAlertYetFoFire(s), user));
        dto.setAlertFired(alertsToOneString(ruleEngine.getAlertFired(s), user));

        final Arrival arrival = getArrival(items);
        if (arrival != null) {
            //"arrivalNotificationTimeISO": "2014-08-12 12:10",
            // NEW - ISO for actual time arrival notification sent out
            dto.setArrivalNotificationTimeIso(isoFmt.format(arrival.getDate()));
            dto.setArrivalNotificationTime(prettyFmt.format(arrival.getDate()));
        }
        if (s.getArrivalDate() != null) {
            dto.setArrivalTimeIso(isoFmt.format(s.getArrivalDate()));
            dto.setArrivalTime(prettyFmt.format(s.getArrivalDate()));
        }
        if (s.getDeviceShutdownTime() != null) {
            dto.setShutdownTimeIso(isoFmt.format(s.getDeviceShutdownTime()));
            dto.setShutdownTime(prettyFmt.format(s.getDeviceShutdownTime()));
        }

        int i = 0;
        for (final SingleShipmentTimeItem item : items) {
            dto.getLocations().addAll(createLocations(item, isoFmt, prettyFmt,
                    user, i == items.size() - 1));
            i++;
        }


        if (items.size() != 0) {
            final TrackerEvent currentEvent = items.get(0).getEvent();
            if (currentEvent.getLatitude() != null && currentEvent.getLongitude() != null) {
                dto.setCurrentLocation(locationService.getLocationDescription(
                        new Location(currentEvent.getLatitude(), currentEvent.getLongitude())));
            }
        }

        if (dto.getCurrentLocation() == null) {
            dto.setCurrentLocation("Not determined");
        }

        for (final Note n : noteDao.findByShipment(s)) {
            dto.getNotes().add(NoteController.creaetNoteDto(n, s, isoFmt));
        }

        final Date alertsSuppressedTime = this.ruleEngine.getAlertsSuppressionDate(s);
        boolean alertsSuppressed = true;
        if (alertsSuppressedTime == null) {
            alertsSuppressed = this.ruleEngine.isAlertsSuppressed(s);
        }

        if (alertsSuppressed) {
            dto.setAlertsSuppressed(true);
            dto.setAlertsSuppressionTime(prettyFmt.format(alertsSuppressedTime));
            dto.setAlertsSuppressionTimeIso(isoFmt.format(alertsSuppressedTime));
        }

        return dto;
    }
    /**
     * @param list
     * @return arrival if found
     */
    private Arrival getArrival(final List<SingleShipmentTimeItem> list) {
        final LinkedList<SingleShipmentTimeItem> items = new LinkedList<SingleShipmentTimeItem>(
                list);
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
     * @param rules alert profile.
     * @param user current user.
     * @return temperature alerts yet to fire so user can suppress future notifications.
     */
    private String alertsToOneString(final List<AlertRule> rules, final User user) {
        final List<String> list = new LinkedList<>();
        for (final AlertRule rule: rules) {
            list.add(ruleBundle.buildDescription(rule, user.getTemperatureUnits()));
        }
        return StringUtils.combine(list, ", ");
    }
    /**
     * @param dtoOld
     * @return
     */
    private SingleShipmentDto createSingleShipmentData(final Shipment shipment, final User user) {
        //"startTimeISO": "2014-08-12 12:10",
        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        final SingleShipmentDto dto = new SingleShipmentDto();
        dto.setAlertProfileId(shipment.getAlertProfile() == null ? null : shipment.getAlertProfile().getId());
        dto.setAlertProfileName(shipment.getAlertProfile() == null ? null : shipment.getAlertProfile().getName());

        dto.setAlertSuppressionMinutes(shipment.getAlertSuppressionMinutes());
        dto.setArrivalNotificationWithinKm(shipment.getArrivalNotificationWithinKm());
        dto.setAssetNum(shipment.getAssetNum());
        dto.setAssetType(shipment.getAssetType());
        dto.setCommentsForReceiver(shipment.getCommentsForReceiver());
        dto.setNoAlertsAfterArrivalMinutes(shipment.getNoAlertsAfterArrivalMinutes());
        dto.setNoAlertsAfterStartMinutes(shipment.getNoAlertsAfterStartMinutes());
        dto.setShutDownAfterStartMinutes(shipment.getShutDownAfterStartMinutes());
        dto.setDeviceColor(shipment.getDevice().getColor() == null ? null : shipment.getDevice().getColor().name());

        final Date startTime = shipment.getShipmentDate();

        dto.setDeviceName(shipment.getDevice().getName());
        dto.setDeviceSN(shipment.getDevice().getSn());
        dto.setShipmentType(shipment.isAutostart() ? "Autostart": "Manual");
        dto.setLatestShipment(shipment.getTripCount() == shipment.getDevice().getTripCount()
                && shipment.getStatus() != ShipmentStatus.Ended);

        if (shipment.getShippedTo() != null) {
            dto.setEndLocation(shipment.getShippedTo().getName());
            dto.setEndLocationForMap(shipment.getShippedTo().getLocation());
        }
        if (shipment.getEta() != null) {
            final Date eta = shipment.getEta();
            if (eta != null) {
                dto.setPercentageComplete(getPercentageCompleted(shipment, new Date(), eta));
                dto.setEtaIso(isoFmt.format(eta));
                dto.setEta(prettyFmt.format(eta));
            }
        }

        dto.setExcludeNotificationsIfNoAlerts(shipment.isExcludeNotificationsIfNoAlerts());
        dto.setPalletId(shipment.getPalletId());
        dto.setShipmentDescription(shipment.getShipmentDescription());
        dto.setShipmentId(shipment.getId());
        if (shipment.getAlertProfile() != null) {
            dto.setShutdownDeviceAfterMinutes(shipment.getShutdownDeviceAfterMinutes());
        }
        if (shipment.getShippedFrom() != null) {
            dto.setStartLocation(shipment.getShippedFrom().getName());
            dto.setStartLocationForMap(shipment.getShippedFrom().getLocation());
        }

        dto.setStartTimeISO(isoFmt.format(startTime));
        dto.setStartTime(prettyFmt.format(startTime));
        dto.setStatus(shipment.getStatus());
        dto.setTripCount(shipment.getTripCount());

        dto.getAlertsNotificationSchedules().addAll(toListItems(shipment.getAlertsNotificationSchedules()));
        dto.getArrivalNotificationSchedules().addAll(toListItems(shipment.getArrivalNotificationSchedules()));
        return dto;
    }
    /**
     * @param arrivalNotificationSchedules
     * @return
     */
    private List<ListNotificationScheduleItem> toListItems(final List<NotificationSchedule> entities) {
        final List<ListNotificationScheduleItem> items = new LinkedList<ListNotificationScheduleItem>();
        for (final NotificationSchedule s : entities) {
            items.add(new ListNotificationScheduleItem(s));
        }
        return items;
    }
    /**
     * @param item
     * @return
     */
    private List<SingleShipmentLocation> createLocations(
            final SingleShipmentTimeItem item,
            final DateFormat timeIsoFmt,
            final DateFormat prettyFmt,
            final User user,
            final boolean isLast) {
        final List<SingleShipmentLocation> list = new LinkedList<SingleShipmentLocation>();

        final TrackerEvent event = item.getEvent();

        //create location
        final SingleShipmentLocation lo = new SingleShipmentLocation();
        lo.setLatitude(event.getLatitude());
        lo.setLongitude(event.getLongitude());
        lo.setTemperature(event.getTemperature());
        lo.setTimeIso(timeIsoFmt.format(event.getTime()));
        lo.setTime(prettyFmt.format(event.getTime()));
        lo.setType(eventTypeToString(event.getType()));
        list.add(lo);

        //add tracker event
        if (isLast) {
            lo.getAlerts().add(createLastReadingAlert(event, user));
        }

        //add alerts
        for (final Alert a : item.getAlerts()) {
            lo.getAlerts().add(createSingleShipmentAlert(a, event, user));
        }
        //add arrivals
        for (final Arrival a: item.getArrivals()) {
            lo.getAlerts().add(createSingleShipmentAlert(a, event, user));
        }

        return list;
    }
    /**
     * @param type
     * @return
     */
    private String eventTypeToString(final TrackerEventType type) {
        switch(type) {
            case INIT:
                return "SwitchedOn";
            case AUT:
                return "Reading";
            case VIB:
                return "Moving";
            case STP:
                return "Stationary";
            case BRT:
                return "LightOn";
            case DRK:
                return "LightOff";
                default:
                    return null;
        }
    }
    /**
     * @param event event.
     * @param user user
     * @return
     */
    private SingleShipmentAlert createLastReadingAlert(final TrackerEvent event,
            final User user) {
        final String text = chartBundle.buildTrackerEventDescription(event,
                user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
        return createSingleShipmentAlert("LastReading", text);
    }
    /**
     * @param a arrival.
     * @param user user.
     * @return
     */
    private SingleShipmentAlert createSingleShipmentAlert(final Arrival a, final TrackerEvent trackerEvent,
            final User user) {
        final String text = chartBundle.buildDescription(a, trackerEvent, user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        return createSingleShipmentAlert("ArrivalNotice", text);
    }
    /**
     * @param a alert.
     * @param user user.
     * @return single shipment alert.
     */
    private SingleShipmentAlert createSingleShipmentAlert(
            final Alert a, final TrackerEvent trackerEvent, final User user) {
        final String text = chartBundle.buildDescription(a, trackerEvent,
                user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
        return createSingleShipmentAlert(a.getType().name(), text);
    }
    /**
     * @param type the type.
     * @param text the text.
     * @return single shipment alert.
     */
    protected SingleShipmentAlert createSingleShipmentAlert(final String type,
            final String text) {
        final SingleShipmentAlert alert = new SingleShipmentAlert();
        alert.setType(type);

        //set title and lines
        final String[] lines = text.split("\n");
        alert.setTitle(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            alert.getLines().add(lines[i]);
        }
        return alert;
    }
    /**
     * @param alerts
     * @return
     */
    public static  Map<AlertType, Integer> toSummaryMap(
            final List<Alert> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final Alert alert : alerts) {
            final AlertType type = alert.getType();
            if (type != AlertType.LightOff && type != AlertType.LightOn) {
                Integer numAlerts = map.get(alert.getType());
                if (numAlerts == null) {
                    numAlerts = 0;
                }
                numAlerts = numAlerts + 1;
                map.put(alert.getType(), numAlerts);
            }
        }

        return map;
    }
    /**
     * @param items tracker events.
     * @param issue notification issue.
     * @return
     */
    private SingleShipmentTimeItem getBestCandidate(final List<SingleShipmentTimeItem> items, final NotificationIssue issue) {
        //first of all attempt to found by tracker event ID.
        final Long trackerEventId = issue.getTrackerEventId();
        if (trackerEventId != null) {
            //for new alerts the tracker event ID should not be null.
            for (final SingleShipmentTimeItem i : items) {
                if (i.getEvent().getId().equals(trackerEventId)) {
                    return i;
                }
            }
        }

        //support of old version where tracker event ID is null.
        final long time = issue.getDate().getTime();
        long distance = Long.MAX_VALUE;

        SingleShipmentTimeItem best = null;
        for (final SingleShipmentTimeItem i : items) {
            final long currentDistance = Math.abs(i.getEvent().getTime().getTime() - time);
            if (currentDistance < distance) {
                distance = currentDistance;
                best = i;
            }
        }

        return best;
    }
}
