/**
 *
 */
package com.visfresh.controllers;


import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Note;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.impl.singleshipment.MainShipmentDataBuilder;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.json.SingleShipmentBeanSerializer;
import com.visfresh.io.json.SingleShipmentSerializer;
import com.visfresh.io.shipment.AlertDto;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.io.shipment.SingleShipmentTimeItem;
import com.visfresh.l12n.ChartBundle;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.LocationService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.ShipmentAuditService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ShipmentOld")
@RequestMapping("/rest")
public class ShipmentControllerOld extends AbstractShipmentBaseController implements ShipmentConstants {
    public static final String GET_SINGLE_SHIPMENT_OLD = "getSingleShipmentOld";
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentControllerOld.class);

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private ChartBundle chartBundle;
    @Autowired
    private LocationService locationService;
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
    private DeviceGroupDao deviceGroupDao;
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ShipmentAuditService auditService;

    /**
     * Default constructor.
     */
    public ShipmentControllerOld() {
        super();
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
     * @param shipmentDate
     * @param currentTime
     * @param eta
     * @return
     */
    private int getPercentageCompleted(final Date shipmentDate, final Date currentTime, final Date eta) {
        return MainShipmentDataBuilder.getPercentageCompleted(shipmentDate, currentTime, eta);
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
    @RequestMapping(value = "/" + GET_SINGLE_SHIPMENT_OLD + "", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getSingleShipmentOld(
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
            final User user = getLoggedInUser();
            final Shipment s;
            if (shipmentId != null) {
                s = shipmentDao.findOne(shipmentId);
            } else {
                s = shipmentDao.findBySnTrip(sn, trip);
            }

            if (s == null) {
                return createSuccessResponse(null);
            }

            if (!hasViewSingleShipmentAccess(user, s)) {
                throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
            }

            final SingleShipmentDto dto = createSingleShipmentDto(s, user);
            addRelevantData(user, s, dto);

            if (dto != null) {
                auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.Viewed, null);
            }

            final SingleShipmentSerializer ser = getSingleShipmentSerializer(user);
            return createSuccessResponse(dto == null ? null : ser.exportToViewData(dto));
        } catch (final Exception e) {
            log.error("Failed to get single shipment: " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @param s
     * @param dto
     */
    protected void addRelevantData(final User user, final Shipment s,
            final SingleShipmentDto dto) {
        addLocationAlternatives(dto, s);

        //add interim stops
        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        addInterimStops(dto, s, isoFmt, prettyFmt);

        //add siblings
        final List<Shipment> siblings = getSiblings(s);
        for (final Shipment sibling : siblings) {
            if (hasViewSingleShipmentAccess(user, sibling)) {
                dto.getSiblings().add(createSingleShipmentDto(sibling, user));
            }
        }

        addDeviceGroups(dto);
    }
    /**
     * @param s shipment.
     * @return sibling list
     */
    private List<Shipment> getSiblings(final Shipment s) {
        return shipmentDao.findAll(s.getSiblings());
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
            in.setStopDate(stp.getDate());

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
    private SingleShipmentDto createSingleShipmentDto(final Shipment s, final User user) {
        //create best tracker event candidate/alert map
        final List<TrackerEvent> events = trackerEventDao.getEvents(s);

        final List<SingleShipmentTimeItem> items = new LinkedList<>();
        for (final TrackerEvent e : events) {
            final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
            item.setEvent(e);
            items.add(item);
        }

        final SingleShipmentDto dto = createSingleShipmentData(s, user);

        final DateFormat isoFmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final DateFormat prettyFmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        final Map<AlertType, Integer> alertSummary = new HashMap<>();
        if (events.size() > 0) {
            //add battery level.
            dto.setBatteryLevel(events.get(events.size() - 1).getBattery());

            //add alerts
            final List<Alert> alerts = alertDao.getAlerts(s);
            for (final Alert alert : alerts) {
                final SingleShipmentTimeItem item = getBestCandidate(items, alert);
                item.getAlerts().add(alert);

                final AlertRule rule = getRuleWithCorrectiveAction(alert);
                if (rule != null) {
                    final AlertDto a = new AlertDto();
                    Long corrListId = null;
                    if (rule instanceof TemperatureRule) {
                        corrListId = ((TemperatureRule) rule).getCorrectiveActions().getId();
                    } else if (rule.getType() == AlertType.LightOn) {
                        corrListId = s.getAlertProfile().getLightOnCorrectiveActions() == null
                                ? null : s.getAlertProfile().getLightOnCorrectiveActions().getId();
                    } else if (rule.getType() == AlertType.Battery) {
                        corrListId = s.getAlertProfile().getBatteryLowCorrectiveActions() == null
                                ? null : s.getAlertProfile().getBatteryLowCorrectiveActions().getId();
                    }
                    a.setCorrectiveActionListId(corrListId);
                    a.setType(alert.getType());
                    a.setId(alert.getId());
                    a.setTime(prettyFmt.format(alert.getDate()));
                    a.setTimeISO(isoFmt.format(alert.getDate()));
                    a.setDescription(ruleBundle.buildDescription(rule, user.getTemperatureUnits()));

                    dto.getAlertsWithCorrectiveActions().add(a);
                }
            }

            //sort sent alerts
            Collections.sort(dto.getAlertsWithCorrectiveActions(), (a1, a2) -> a1.getTimeISO().compareTo(a2.getTimeISO()));

            alertSummary.putAll(SingleShipmentBeanSerializer.toSummaryMap(alerts));

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
        if (events.size() > 0) {
            long timeOfFirstReading = System.currentTimeMillis() + 100000;
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

            dto.setFirstReadingTimeIso(isoFmt.format(new Date(timeOfFirstReading)));
            dto.setFirstReadingTime(prettyFmt.format(new Date(timeOfFirstReading)));
            final Date lastReadingTime = new Date(timeOfLastReading);
            dto.setLastReadingTimeIso(isoFmt.format(lastReadingTime));
            dto.setLastReadingTime(prettyFmt.format(lastReadingTime));
        }

        dto.setMinTemp(minTemp);
        dto.setMaxTemp(maxTemp);

        //last readings
        if (lastReadingTemperature != null) {
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
                final Location currentLocation = new Location(currentEvent.getLatitude(), currentEvent.getLongitude());
                dto.setCurrentLocation(locationService.getLocationDescription(
                        currentLocation));
                dto.setCurrentLocationForMap(currentLocation);
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
        dto.setCompanyId(shipment.getCompany().getId());

        if (shipment.getAlertProfile() != null) {
            dto.setAlertProfileId(shipment.getAlertProfile().getId());
            dto.setAlertProfileName(shipment.getAlertProfile().getName());
            dto.setAlertProfile(new AlertProfileDto(shipment.getAlertProfile()));
        }

        dto.setAlertSuppressionMinutes(shipment.getAlertSuppressionMinutes());
        dto.setArrivalNotificationWithinKm(shipment.getArrivalNotificationWithinKm());
        dto.setAssetNum(shipment.getAssetNum());
        dto.setAssetType(shipment.getAssetType());
        dto.setCommentsForReceiver(shipment.getCommentsForReceiver());
        dto.setNoAlertsAfterArrivalMinutes(shipment.getNoAlertsAfterArrivalMinutes());
        dto.setNoAlertsAfterStartMinutes(shipment.getNoAlertsAfterStartMinutes());
        dto.setShutDownAfterStartMinutes(shipment.getShutDownAfterStartMinutes());
        dto.setDeviceColor(shipment.getDevice().getColor() == null ? null : shipment.getDevice().getColor().name());
        dto.setSendArrivalReport(shipment.isSendArrivalReport());
        dto.setSendArrivalReportOnlyIfAlerts(shipment.isSendArrivalReportOnlyIfAlerts());
        dto.setArrivalReportSent(notificationService.isArrivalReportSent(shipment));

        final Date startTime = shipment.getShipmentDate();

        dto.setDeviceName(shipment.getDevice().getName());
        dto.setDeviceSN(shipment.getDevice().getSn());
        dto.setShipmentType(shipment.isAutostart() ? "Autostart": "Manual");
        dto.setLatestShipment(shipment.getTripCount() >= shipment.getDevice().getTripCount()
                && shipment.getStatus() != ShipmentStatus.Ended);

        if (shipment.getShippedTo() != null) {
            dto.setEndLocation(shipment.getShippedTo().getName());
            dto.setEndLocationForMap(shipment.getShippedTo().getLocation());
        }
        if (shipment.getEta() != null) {
            final Date eta = shipment.getEta();
            dto.setPercentageComplete(getPercentageCompleted(shipment.getShipmentDate(), new Date(), eta));
            dto.setEtaIso(isoFmt.format(eta));
            dto.setEta(prettyFmt.format(eta));
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

        for (final User u : shipment.getUserAccess()) {
            dto.getUserAccess().add(new ShipmentUserDto(u));
        }
        for (final Company c : shipment.getCompanyAccess()) {
            dto.getCompanyAccess().add(new ShipmentCompanyDto(c));
        }
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
    /**
     * @param user user
     * @param s shipment.
     * @throws RestServiceException
     */
    public static boolean hasViewSingleShipmentAccess(final User user, final Shipment s) {
        if (Role.SmartTraceAdmin.hasRole(user)) {
            return true;
        }

        final Company usersCompany = user.getCompany();
        if (s == null || s.getCompany() == null) {
            return false;
        }

        if (s.getCompany().getId().equals(usersCompany.getId())) {
            return true;
        }

        //check user access
        for (final User u : s.getUserAccess()) {
            if (u.getId().equals(user.getId())) {
                return true;
            }
        }

        //check company access
        for (final Company c : s.getCompanyAccess()) {
            if (c.getId().equals(usersCompany.getId())) {
                return true;
            }
        }

        return false;
    }
    /**
     * @param user
     * @return
     */
    private SingleShipmentSerializer getSingleShipmentSerializer(final User user) {
        return new SingleShipmentSerializer(user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
    }
    /**
     * @param alert
     * @return
     */
    public static AlertRule getRuleWithCorrectiveAction(final Alert alert) {
        final AlertProfile alertProfile = alert.getShipment().getAlertProfile();

        if (alert instanceof TemperatureAlert) {
            final Long ruleId = ((TemperatureAlert) alert).getRuleId();
            for (final TemperatureRule rule : alertProfile.getAlertRules()) {
                if (rule.getCorrectiveActions() != null && rule.getId().equals(ruleId)) {
                    return rule;
                }
            }
        } else {
            final AlertType type = alert.getType();
            if (type == AlertType.Battery && alertProfile.getBatteryLowCorrectiveActions() != null
                    || type == AlertType.LightOn && alertProfile.getLightOnCorrectiveActions() != null) {
                return new AlertRule(alert.getType());
            }
        }

        return null;
    }
}
