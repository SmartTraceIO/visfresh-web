/**
 *
 */
package com.visfresh.controllers;


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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.impl.json.SingleShipmentBeanSerializer;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.impl.singleshipment.MainShipmentDataBuilder;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.KeyLocation;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.SortColumn;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.json.GetShipmentsRequestParser;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.io.json.fastxml.JsonSerializerFactory;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.l12n.NotificationIssueBeanBundle;
import com.visfresh.lists.ListResult;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.ShipmentAuditService;
import com.visfresh.services.SingleShipmentService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Shipment")
@RequestMapping("/rest")
public class ShipmentController extends AbstractShipmentBaseController implements ShipmentConstants {
    public static final String GET_SINGLE_SHIPMENT = "getSingleShipment";
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
    private TrackerEventDao trackerEventDao;
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private InterimStopDao interimStopDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private AutoStartShipmentService autoStartService;
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;
    @Autowired
    private ShipmentAuditService auditService;
    @Autowired
    private SingleShipmentService singleShipmentService;
    @Autowired
    private CompanyDao companyDao;

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
     * @throws Exception
     */
    @RequestMapping(value = "/saveShipment", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveShipment(final @RequestBody JsonObject jsonRequest) throws Exception {
        final User user = getLoggedInUser();
        final ShipmentSerializer serializer = getSerializer(user);
        Long id = serializer.getShipmentIdFromSaveRequest(jsonRequest);

        ShipmentDto oldShipmentDto = null;
        Shipment oldShipment = null;

        final boolean isNew = id == null;
        if (!isNew) {
            //merge the shipment from request by existing shipment
            //it is required to avoid the set to null the fields
            //which are absent in save request.
            oldShipment = shipmentDao.findOne(id);
            if (oldShipment != null) {
                checkCompanyAccess(user, oldShipment);
                oldShipmentDto = createShipmentDto(oldShipment);

                final JsonObject shipmentFromReqest = serializer.getShipmentFromRequest(
                        jsonRequest);
                final JsonObject merged = SerializerUtils.merge(
                        shipmentFromReqest,
                        serializer.toJson(oldShipmentDto).getAsJsonObject());
                //correct shipment to save in request
                serializer.setShipmentToRequest(jsonRequest, merged);
            } else {
                throw new Exception("Shipment with ID " + id + " not found");
            }
        }

        final SaveShipmentRequest req = serializer.parseSaveShipmentRequest(jsonRequest);

        //save shipment
        final Shipment newShipment = createShipment(req.getShipment(), oldShipment);
        resolveReferences(user, req.getShipment(), newShipment);

        newShipment.setCompany(user.getCompanyId());
        newShipment.setCreatedBy(user.getEmail());

        if (id != null) {
            if (!canChangeStatus(oldShipmentDto.getStatus(), newShipment.getStatus())) {
                log.debug("Is not allowed to change status from " + oldShipmentDto.getStatus()
                    + " to " + newShipment.getStatus() + " for shipment " + newShipment);
                newShipment.setStatus(oldShipmentDto.getStatus());
            }
            shipmentDao.save(newShipment);

            //check
            if (oldShipmentDto.getStatus() != newShipment.getStatus()) {
                handleStatusChanged(newShipment, oldShipmentDto.getStatus(), newShipment.getStatus());
            }
        } else {
            id = saveNewShipment(newShipment, !Boolean.FALSE.equals(req.isIncludePreviousData()));
        }

        saveAlternativeAndInterimLoations(user, newShipment,
                req.getShipment().getInterimLocations(),
                req.getShipment().getEndLocationAlternatives());
        updateInterimStops(newShipment, req.getShipment().getInterimStops());

        //build response
        final SaveShipmentResponse resp = new SaveShipmentResponse();
        resp.setShipmentId(id);

        if (req.isSaveAsNewTemplate()) {
            final Long tplId = createShipmentTemplate(
                    user.getCompanyId(), newShipment, req.getTemplateName());
            resp.setTemplateId(tplId);
        }

        //audit
        if (isNew) {
            auditService.handleShipmentAction(newShipment.getId(), user, ShipmentAuditAction.ManuallyCreated, null);
        } else {
            final Map<String, String> details = new HashMap<>();
            final JsonObject diff = SerializerUtils.diff(
                    serializer.toJson(oldShipmentDto),
                    serializer.toJson(createShipmentDto(newShipment)));
            details.put("diff", diff == null ? null : diff.toString());
            auditService.handleShipmentAction(newShipment.getId(), user, ShipmentAuditAction.Updated, details);
        }

        return createSuccessResponse(serializer.toJson(resp));
    }
    /**
     * @param oldStatus
     * @param newStatus
     * @return
     */
    private boolean canChangeStatus(final ShipmentStatus oldStatus, final ShipmentStatus newStatus) {
        if (oldStatus == ShipmentStatus.Default) {
            return newStatus == ShipmentStatus.Ended || newStatus == ShipmentStatus.Arrived
                    || newStatus == ShipmentStatus.Pending;
        }
        if (oldStatus == ShipmentStatus.InProgress) {
            return newStatus == ShipmentStatus.Ended || newStatus == ShipmentStatus.Arrived
                    || newStatus == ShipmentStatus.Pending;
        }
        if (oldStatus == ShipmentStatus.Arrived) {
            return false;
        }
        if (oldStatus == ShipmentStatus.Ended) {
            return newStatus == ShipmentStatus.Arrived;
        }

        return true;
    }
    /**
     * @param s the shipment.
     * @param oldStatus old shipment status.
     * @param newStatus new shipment status.
     */
    private void handleStatusChanged(final Shipment s,
            final ShipmentStatus oldStatus, final ShipmentStatus newStatus) {
        Date startDate;
        if (s.getLastEventDate() != null) {
            startDate = s.getLastEventDate();
        } else {
            startDate = s.getShipmentDate();
        }

        boolean foundAssigned = false;
        Date endDate = new Date(startDate.getTime() + 60 * 60 * 1000l);

        while (!foundAssigned && !startDate.after(new Date())) {
            final List<ShortTrackerEvent> events = trackerEventDao.findBy(s.getDevice().getImei(), startDate, endDate);
            for (final ShortTrackerEvent e : events) {
                if (e.getShipmentId() == null) {
                    trackerEventDao.assignShipment(e.getId(), s);
                } else if (e.getShipmentId().equals(s.getId())) {
                    //ignore
                } else {
                    foundAssigned = true;
                    break;
                }
            }

            startDate = endDate;
            endDate = new Date(startDate.getTime() + 60 * 60 * 1000l);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.AbstractShipmentBaseController#saveAlternativeAndInterimLoations(com.visfresh.entities.ShipmentBase, java.util.Collection, java.util.Collection)
     */
    @Override
    protected void saveAlternativeAndInterimLoations(final ShipmentBase s, final Collection<LocationProfile> interims,
            final Collection<LocationProfile> alternativeEnds) {
        super.saveAlternativeAndInterimLoations(s, interims, alternativeEnds);
        if (interims != null || alternativeEnds != null) {
            ShipmentSession session = shipmentSessionDao.getSession((Shipment) s);
            if (session == null) {
                session = new ShipmentSession(s.getId());
            }

            if (interims != null) {
                ruleEngine.updateInterimLocations(session, new LinkedList<>(interims));
            }
            if (alternativeEnds != null) {
                ruleEngine.updateAutodetectingEndLocations(session, new LinkedList<>(alternativeEnds));
            }

            shipmentSessionDao.saveSession(session);
        }
    }
    /**
     * @param dto
     * @param old
     * @return
     */
    private Shipment createShipment(final ShipmentDto dto, final Shipment old) {
        final boolean isNew = old == null;
        final Shipment s = isNew ? new Shipment() : old;
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
                companyDao.findOne(newShipment.getCompanyId()),
                newShipment.getShipmentDate());
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

    private Long createShipmentTemplate(final Long company, final Shipment shipment, final String templateName) {
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
     * @throws ParseException
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getShipments", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipments(@RequestBody final JsonObject request) throws ParseException, RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final GetShipmentsRequestParser ser = new GetShipmentsRequestParser(user.getTimeZone());
        final GetFilteredShipmentsRequest req = ser.parseGetFilteredShipmentsRequest(request);

        final Integer pageIndex = req.getPageIndex();
        final Integer pageSize = req.getPageSize();
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        final Filter filter = createFilter(req);
        final ListResult<ListShipmentItem> shipments = getShipments(
                user.getCompanyId(),
                createSortingShipments(req),
                filter,
                page, user);

        //add events data
        final Map<ListShipmentItem, List<KeyLocation>> keyLocs = createKeyLocations(
                shipments.getItems(), user);

        final ShipmentSerializer shs = new ShipmentSerializer(user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        final JsonArray array = new JsonArray();
        for (final ListShipmentItem s : shipments.getItems()) {
            array.add(shs.toJson(s, keyLocs.get(s)));
        }

        return createListSuccessResponse(array, shipments.getTotalCount());
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     * @throws ParseException
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getShipmentsNearby", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipmentsNearby(
            @RequestParam(value = "lat") final String latStr,
            @RequestParam(value = "lon") final String lonStr,
            @RequestParam final int radius,
            @RequestParam(required = false, value = "from") final String fromStr) throws ParseException, RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();

        //parse request parameters.
        final double lat = Double.parseDouble(latStr);
        final double lon = Double.parseDouble(lonStr);
        final Date startDate;
        if (fromStr == null) {
            startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000l);
        } else {
            startDate = DateTimeUtils.createDateFormat("yyyy-MM-dd'T'HH-mm-ss",
                    user.getLanguage(), user.getTimeZone()).parse(fromStr);
        }

        //request shipments from DB
        int page = 1;
        final int limit = 100;
        final Sorting sorting = new Sorting(SHIPMENT_ID);

        final List<ListShipmentItem> shipments = new LinkedList<>();
        ListResult<ListShipmentItem> part;
        do {
            part = getShipments(user.getCompanyId(), sorting, null, new Page(page, limit), user);
            for (final ListShipmentItem item : part.getItems()) {
                //Check date and location nearby
                final Double itemLat = item.getLastReadingLat();
                final Double itemLon = item.getLastReadingLong();

                if (itemLat != null && itemLon != null) {
                    final double dinst = LocationUtils.getDistanceMeters(
                            lat, lon, itemLat, itemLon);
                    if (dinst <= radius && item.getLastReadingTime() != null) {
                        final Date lastDate = item.getLastReadingTime();
                        //check the last reading time
                        if (!lastDate.before(startDate)) {
                            shipments.add(item);
                        }
                    }
                }
            }

            page++;
        } while (part.getItems().size() >= limit);

        //add events data
        final Map<ListShipmentItem, List<KeyLocation>> keyLocs = createKeyLocations(shipments, user);

        final ShipmentSerializer shs = new ShipmentSerializer(user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        final JsonArray array = new JsonArray();
        for (final ListShipmentItem s : shipments) {
            array.add(shs.toJson(s, keyLocs.get(s)));
        }

        return createListSuccessResponse(array, shipments.size());
    }
    /**
     * @param shipments
     * @throws ParseException
     */
    private Map<ListShipmentItem, List<KeyLocation>> createKeyLocations(
            final List<ListShipmentItem> shipments, final User user) throws ParseException {
        final Map<ListShipmentItem, List<KeyLocation>> result = new HashMap<>();

        final Collection<Long> shipmentIds = EntityUtils.getIdList(shipments);
        final Map<Long, List<TrackerEventDto>> eventMap = trackerEventDao.getEventsForShipmentIds(
                shipmentIds);
        final NotificationIssueBeanBundle bundle = new NotificationIssueBeanBundle(
                user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());

        //events
        for (final ListShipmentItem s : shipments) {
            final List<TrackerEventDto> events = eventMap.get(s.getId());

            final List<KeyLocation> keyLocs = buildKeyLocations(events);
            addInterimStopKeyLocations(keyLocs, s.getInterimStops());

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
                for (final AlertBean alert : s.getSentAlerts()) {
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

                    final KeyLocation loc = buildKeyLocation(alert, events, s, bundle);
                    if (loc != null) {
                        insertKeyLocation(loc, keyLocs);
                    }
                }
            }

            result.put(s, keyLocs);
        }

        return result;
    }

    /**
     * @param alert
     * @param events
     * @return
     */
    private KeyLocation buildKeyLocation(final AlertBean alert,
            final List<TrackerEventDto> events,
            final ListShipmentItem s,
            final NotificationIssueBeanBundle bundle) {
        final Long eventId = alert.getTrackerEventId();

        if (eventId != null) {
            for (final TrackerEventDto e : events) {
                if (e.getId().equals(eventId)) {
                    final KeyLocation loc = new KeyLocation();
                    loc.setKey(alert.getType().name() + "Alert");
                    loc.setLatitude(e.getLatitude());
                    loc.setLongitude(e.getLongitude());
                    loc.setTime(e.getTime().getTime());
                    loc.setDescription(bundle.buildDescription(alert,
                            new SingleShipmentLocationBean(e), s));
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
            final List<InterimStopBean> interimStops) throws ParseException {
        for (final InterimStopBean stp : interimStops) {
            final KeyLocation loc = createKeyLocation(stp);
            loc.setTime(stp.getStopDate().getTime());
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
    private KeyLocation createKeyLocation(final InterimStopBean stp) {
        final KeyLocation loc = new KeyLocation();
        loc.setKey("interimStop");
        loc.setDescription(stp.getLocation().getName());
        loc.setLatitude(stp.getLocation().getLocation().getLatitude());
        loc.setLongitude(stp.getLocation().getLocation().getLongitude());
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
     * @param req
     * @return
     */
    protected Sorting createSortingShipments(final GetFilteredShipmentsRequest req) {
        if (req.getSortColumns().size() > 0) { //new way with multiple sort columns.
            final Sorting sort = new Sorting();
            for (final SortColumn sc : req.getSortColumns()) {
                sort.addSortProperty(sc.getName(), sc.isAscent());
            }
            return sort;
        }

        //else old way
        return createSortingShipments(
                req.getSortColumn(),
                req.getSortOrder(),
                getDefaultListShipmentsSortingOrder(), 2);
    }
    private Sorting createSortingShipments(final String sc, final String so,
            final String[] defaultSortOrder, final int maxNumOfSortColumns) {
        final String sortColumn = fixSortShipmentColumnName(sc);
        return super.createSorting(sortColumn, so, defaultSortOrder, maxNumOfSortColumns);
    }
    /**
     * @param sc column name.
     * @return fixed column name.
     */
    private String fixSortShipmentColumnName(final String sc) {
        String sortColumn;
        if (SHIPPED_FROM.equals(sc)) {
            sortColumn = SHIPPED_FROM_LOCATION_NAME;
        } else if (SHIPPED_TO.equals(sc)) {
            sortColumn = SHIPPED_TO_LOCATION_NAME;
        } else {
            sortColumn = sc;
        }
        return sortColumn;
    }
    /**
     * @return
     */
    public static String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            SHIPMENT_ID,
            SHIPMENT_DATE,
            STATUS,
            SHIPPED_FROM_LOCATION_NAME,
            SHIPPED_TO_LOCATION_NAME,
            DEVICE_SN, //should add trip count next
            ARRIVAL_DATE,
            COMMENTS_FOR_RECEIVER,
            NEAREST_TRACKER,
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

    public static Filter createFilter(final GetFilteredShipmentsRequest req) {
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
        if (req.getDeviceSn() != null) {
            f.addFilter(ShipmentConstants.DEVICE_SN, req.getDeviceSn());
        }
        return f;
    }
    /**
     * @param company
     * @param user the user.
     * @return
     */
    private ListResult<ListShipmentItem> getShipments(final Long company,
            final Sorting sorting,
            final Filter filter,
            final Page page, final User user) {
        final ListResult<ListShipmentItem> shipments = shipmentDao.getCompanyShipments(
                company, sorting, page, filter);

        final Date currentTime = new Date();

        //add alerts to each shipment.
        for (final ListShipmentItem dto : shipments.getItems()) {
            //percentage complete.
            if (dto.getStatus().isFinal()) {
                dto.setPercentageComplete(100);
            } else {
                final Date eta = dto.getEta();
                if (eta != null) {
                    dto.setPercentageComplete(getPercentageCompleted(dto.getShipmentDate(), currentTime, eta));
                }
            }

            if (dto.getStatus() == ShipmentStatus.Default || dto.getStatus() == ShipmentStatus.Ended) {
                dto.setEta(null);
                dto.setActualArrivalDate(null);
                if (dto.getStatus() == ShipmentStatus.Default) {
                    dto.setShippedTo(null);
                }
            }
        }

        return shipments;
    }

    /**
     * @param shipmentDate
     * @param currentTime
     * @param eta
     * @return
     */
    private int getPercentageCompleted(final Date shipmentDate, final Date currentTime, final Date eta) {
        return MainShipmentDataBuilder.getPercentageCompleted(shipmentDate, currentTime, eta);
    }
    /**
     * @param authToken authentication token.
     * @param shipmentId shipment ID.
     * @return shipment.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getShipment", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipment(@RequestParam final Long shipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Shipment shipment = shipmentDao.findOne(shipmentId);
        checkCompanyAccess(user, shipment);

        final ShipmentDto dto = createShipmentDto(shipment);
        final JsonObject json = getSerializer(user).toJson(dto);

        auditService.handleShipmentAction(shipment.getId(), user, ShipmentAuditAction.LoadedForEdit, null);
        return createSuccessResponse(json);
    }
    /**
     * @param shipment
     * @return
     */
    protected ShipmentDto createShipmentDto(final Shipment shipment) {
        final ShipmentDto dto = new ShipmentDto(shipment);
        addInterimLocations(dto, shipment);
        addInterimStops(dto, shipment);
        return dto;
    }
    /**
     * @param user
     * @return
     */
    private ShipmentSerializer getSerializer(final User user) {
        return new ShipmentSerializer(user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
    }
    @RequestMapping(value = "/deleteShipment", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteShipment(@RequestParam final Long shipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Shipment s = shipmentDao.findOne(shipmentId);
        checkCompanyAccess(user, s);

        shipmentDao.delete(s);
        return createSuccessResponse(null);
    }
    @RequestMapping(value = "/suppressAlerts", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject suppressAlerts(@RequestParam final Long shipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Shipment s = shipmentDao.findOne(shipmentId);
        checkCompanyAccess(user, s);

        if (s != null) {
            ruleEngine.suppressNextAlerts(s);
        }

        auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.SuppressedAlerts, null);
        return createSuccessResponse(null);
    }
    @RequestMapping(value = "/" + GET_SINGLE_SHIPMENT + "", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getSingleShipment(
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) throws RestServiceException {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        //check logged in.
        final User user = getLoggedInUser();
        final SingleShipmentData s;
        if (shipmentId != null) {
            s = singleShipmentService.getShipmentData(shipmentId);
        } else {
            s = singleShipmentService.getShipmentData(sn, trip);
        }

        if (s == null) {
            return createSuccessResponse(null);
        }

        if (!hasViewSingleShipmentAccess(user, s.getBean())) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }

        auditService.handleShipmentAction(s.getBean().getShipmentId(), user, ShipmentAuditAction.Viewed, null);

        final SingleShipmentBeanSerializer ser = new SingleShipmentBeanSerializer(
                user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
        return createSuccessResponse(s == null ? null : ser.exportToViewData(s));
    }
    @RequestMapping(value = "/getSingleShipmentLite", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getSingleShipmentLite(
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) throws RestServiceException {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        //check logged in.
        final User user = getLoggedInUser();
        final SingleShipmentData s;
        if (shipmentId != null) {
            s = singleShipmentService.getShipmentData(shipmentId);
        } else {
            s = singleShipmentService.getShipmentData(sn, trip);
        }

        if (s == null) {
            return createSuccessResponse(null);
        }

        if (!hasViewSingleShipmentAccess(user, s.getBean())) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }

        final SingleShipmentFilter300 filter = new SingleShipmentFilter300();
        filter.filter(s.getLocations(), s.getBean().getNotes());

        auditService.handleShipmentAction(s.getBean().getShipmentId(), user, ShipmentAuditAction.ViewedLite, null);

        final SingleShipmentBeanSerializer ser = new SingleShipmentBeanSerializer(
                user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
        return createSuccessResponse(s == null ? null : ser.exportToViewData(s));
    }
    @RequestMapping(value = "/createNewAutoSthipment", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement createNewAutoSthipment(
            @RequestParam final String device,
            @RequestParam(required = false) final String beacon) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        //get device
        final Device d = deviceDao.findByImei(device);
        checkCompanyAccess(user, d);

        if (d == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found device with IMEI '" + device + "'");
        }
        if (!allowBt4Autostart(d)) {
            throw new RestServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Overhead 2 mins autostart limit for BT04 device '" + d.getImei() + "'");
        }

        //get last reading
        final ShortTrackerEvent e = trackerEventDao.getLastEvent(d);
        Double latitude = null;
        Double longitude = null;
        if (e != null ) {
            latitude = e.getLatitude();
            longitude = e.getLongitude();
        }

        final Shipment s = autoStartService.autoStartNewShipment(d,
                latitude, longitude, new Date());
        if (s != null) {
            auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.ManuallyCreatedFromAutostart, null);
        }
        return createIdResponse("shipmentId", s.getId());
    }
    /**
     * @param d
     * @return
     */
    private boolean allowBt4Autostart(final Device d) {
        if (d.getModel() != DeviceModel.BT04) {
            return true;
        }

        final Shipment s = shipmentDao.findLastShipment(d.getImei());
        return s == null
                || System.currentTimeMillis() - s.getShipmentDate().getTime() > 2 * 60 * 1000l;
    }

    @RequestMapping(value = "/getShipmentData", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getShipmentData(
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) throws RestServiceException, JsonProcessingException {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        //check logged in.
        final User user = getLoggedInUser();
        final SingleShipmentData s;
        if (shipmentId != null) {
            s = singleShipmentService.getShipmentData(shipmentId);
        } else {
            s = singleShipmentService.getShipmentData(sn, trip);
        }

        if (s == null) {
            return createSuccessResponse(null);
        }

        if (!hasViewSingleShipmentAccess(user, s.getBean())) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }

        auditService.handleShipmentAction(s.getBean().getShipmentId(), user, ShipmentAuditAction.Viewed, null);
        return SerializerUtils.parseJson(JsonSerializerFactory.FACTORY.createDefaultMapper().writeValueAsString(s)).getAsJsonObject();
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
     * @param user
     * @param s
     * @return
     */
    private boolean hasViewSingleShipmentAccess(final User user, final SingleShipmentBean s) {
        if (Role.SmartTraceAdmin.hasRole(user)) {
            return true;
        }

        if (s == null || s.getCompanyId() == null) {
            return false;
        }

        final Long usersCompany = user.getCompanyId();
        if (s.getCompanyId().equals(usersCompany)) {
            return true;
        }

        //check user access
        for (final ShipmentUserDto u : s.getUserAccess()) {
            if (u.getId().equals(user.getId())) {
                return true;
            }
        }

        //check company access
        for (final ShipmentCompanyDto c : s.getCompanyAccess()) {
            if (c.getId().equals(usersCompany)) {
                return true;
            }
        }

        return false;
    }
}
