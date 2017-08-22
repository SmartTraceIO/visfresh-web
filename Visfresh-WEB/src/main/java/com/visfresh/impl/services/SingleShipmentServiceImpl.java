/**
 *
 */
package com.visfresh.impl.services;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Note;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.services.LocationService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.SingleShipmentService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentServiceImpl implements SingleShipmentService {
    /**
     * Shipment dao.
     */
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AlternativeLocationsDao alternativeLocationsDao;
    @Autowired
    private InterimStopDao interimStopDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private DeviceGroupDao deviceGroupDao;

    /**
     * Default constructor.
     */
    public SingleShipmentServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#createLiteData()
     */
    @Override
    public SingleShipmentBean createLiteData(final Long shipmentId, final String sn, final Integer tripCount) {
        final Shipment s = findShipment(shipmentId, sn, tripCount);
        if (s == null) {
            return null;
        }

        final SingleShipmentBean bean = new SingleShipmentBean();
        addShipmentData(bean, s);
        return bean;
    }
    @Override
    public void addReadingsInfo(final SingleShipmentBean bean, final boolean includeReadings) {
        if (bean == null) {
            return;
        }

        final long shipmentId = bean.getShipmentId();
        final List<TrackerEventDto> events = getReadings(shipmentId).get(shipmentId);
        if (events != null) {
            final TrackerEventDto firstReading = events.get(0);
            //first reading data
            bean.setFirstReadingTime(firstReading.getTime());
            bean.setTimeOfFirstReading(firstReading.getTime()); //??? seems like duplicate

            double maxTemp = firstReading.getTemperature();
            double minTemp = maxTemp;

            for (final TrackerEventDto e : events) {
                final double t = e.getTemperature();
                if (t < minTemp) {
                    minTemp = t;
                } else if (t > maxTemp) {
                    maxTemp = t;
                }
            }

            bean.setMaxTemp(maxTemp);
            bean.setMinTemp(minTemp);
            //last reading data
            final TrackerEventDto lastReading = events.get(events.size() - 1);
            bean.setBatteryLevel(lastReading.getBattery());
            bean.setCurrentLocation(new Location(lastReading.getLatitude(), lastReading.getLongitude()));
            bean.setCurrentLocationDescription(getLocationService().getLocationDescription(
                    bean.getCurrentLocation()));
            bean.setLastReadingTemperature(lastReading.getTemperature());
            bean.setLastReadingTime(lastReading.getTime());
        }
    }

    /**
     * @param shipmentId
     * @return
     */
    protected Map<Long, List<TrackerEventDto>> getReadings(final long shipmentId) {
        return trackerEventDao.getEventsForShipmentIds(
                Collections.singleton(shipmentId));
    }

    /**
     * @return
     */
    protected RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    /**
     * @param bean
     * @param s
     */
    private void addShipmentData(final SingleShipmentBean bean, final Shipment s) {
        if (s.getAlertProfile() != null) {
            bean.setAlertProfile(new AlertProfileDto(s.getAlertProfile()));
        }

        final RuleEngine engine = getRuleEngine();
        final Date alertsSuppressedTime = engine.getAlertsSuppressionDate(s);
        if (alertsSuppressedTime != null || engine.isAlertsSuppressed(s)) {
            bean.setAlertsSuppressed(true);
            bean.setAlertsSuppressionTime(alertsSuppressedTime);
        }
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        final Arrival arrival = getArrival(s);
        if (arrival != null) {
            bean.setArrivalNotificationTime(arrival.getDate());
        }

        bean.setArrivalNotificationWithinKm(s.getArrivalNotificationWithinKm());
        bean.setArrivalReportSent(getNotificationService().isArrivalReportSent(s));
        bean.setArrivalTime(s.getArrivalDate());
        bean.setAssetNum(s.getAssetNum());
        bean.setAssetType(s.getAssetType());

        bean.setCommentsForReceiver(s.getCommentsForReceiver());
        bean.setCompanyId(s.getCompany().getId());

        bean.setDevice(s.getDevice().getImei());
        if (s.getDevice().getColor() != null) {
            bean.setDeviceColor(s.getDevice().getColor().name());
        }
        bean.setDeviceName(s.getDevice().getName());
        bean.setEndLocation(s.getShippedTo());
        bean.setExcludeNotificationsIfNoAlerts(s.isExcludeNotificationsIfNoAlerts());

        bean.setLatestShipment(s.getTripCount() >= s.getDevice().getTripCount()
                && s.getStatus() != ShipmentStatus.Ended);
        bean.setNoAlertsAfterArrivalMinutes(s.getNoAlertsAfterArrivalMinutes());
        bean.setNoAlertsAfterStartMinutes(s.getNoAlertsAfterStartMinutes());
        bean.setPalletId(s.getPalletId());

        if (s.getEta() != null) {
            bean.setPercentageComplete(getPercentageCompleted(s, new Date(), s.getEta()));
            bean.setEta(s.getEta());
        }

        bean.setSendArrivalReport(s.isSendArrivalReport());
        bean.setSendArrivalReportOnlyIfAlerts(s.isSendArrivalReportOnlyIfAlerts());
        bean.setShipmentDescription(s.getShipmentDescription());
        bean.setShipmentId(s.getId());
        bean.setShipmentType(s.isAutostart() ? "Autostart": "Manual");
        bean.setShutDownAfterStartMinutes(s.getShutDownAfterStartMinutes());
        bean.setShutdownDeviceAfterMinutes(s.getShutdownDeviceAfterMinutes());
        if (s.getShippedFrom() != null) {
            bean.setStartLocation(new LocationProfileBean(s.getShippedFrom()));
        }
        bean.setStartTime(s.getStartDate());
        bean.setStatus(s.getStatus());
        bean.setTripCount(s.getTripCount());

        //arrival notifications
        for (final NotificationSchedule sched: s.getArrivalNotificationSchedules()) {
            bean.getArrivalNotificationSchedules().add(new ListNotificationScheduleItem(sched));
        }
        //alert notification schedule
        for (final NotificationSchedule sched: s.getAlertsNotificationSchedules()) {
            bean.getAlertsNotificationSchedules().add(new ListNotificationScheduleItem(sched));
        }

        //location alternatives
        final AlternativeLocations alt = getAlternativeLocations(s);
        if (alt != null) {
            bean.getStartLocationAlternatives().addAll(toBeans(alt.getFrom()));
            bean.getEndLocationAlternatives().addAll(toBeans(alt.getTo()));
            bean.getInterimLocationAlternatives().addAll(toBeans(alt.getInterim()));
        }

        //interim stops.
        for (final InterimStop stp : getInterimStops(s)) {
            bean.getInterimStops().add(new InterimStopBean(stp));
        }

        //notes
        for (final Note n : getNotes(s)) {
            bean.getNotes().add(new NoteBean(n));
        }

        //device group DAO
        final List<DeviceGroupDto> deviceGroups = getShipmentGroups(s);
        for (final DeviceGroupDto grp : deviceGroups) {
            bean.getDeviceGroups().add(grp);
        }

        //user access
        for (final User u : s.getUserAccess()) {
            bean.getUserAccess().add(new ShipmentUserDto(u));
        }
        //company access
        for (final Company c : s.getCompanyAccess()) {
            bean.getCompanyAccess().add(new ShipmentCompanyDto(c));
        }

//        //alerts
//        final List<Alert> alerts = alertDao.getAlerts(s);
//        for (final Alert alert : alerts) {
//            final AlertBean a = new AlertBean(alert);
//            final AlertRule rule = getAlertWithCorrectiveAction(alert);
//            if (rule != null) {
//                if (rule instanceof TemperatureRule) {
//                    a.setCorrectiveActionListId(((TemperatureRule) rule).getCorrectiveActions().getId());
//                }
//            }
//            a.setType(alert.getType());
//            a.setId(alert.getId());
//            a.setTime(prettyFmt.format(alert.getDate()));
//            a.setTimeISO(isoFmt.format(alert.getDate()));
//            a.setDescription(ruleBundle.buildDescription(rule, user.getTemperatureUnits()));
//
//            dto.getAlertsWithCorrectiveActions().add(a);
//        }
    }

    /**
     * @param s shipment.
     * @return list of device groups.
     */
    private List<DeviceGroupDto> getShipmentGroups(final Shipment s) {
        final List<DeviceGroupDto> groups = deviceGroupDao.getShipmentGroups(
                Collections.singleton(s.getId())).get(s.getId());
        return groups == null ? new LinkedList<>() : groups;
    }

    /**
     * @param s
     * @return
     */
    protected List<Note> getNotes(final Shipment s) {
        return noteDao.findByShipment(s);
    }

    /**
     * @param s
     * @return
     */
    protected List<InterimStop> getInterimStops(final Shipment s) {
        return interimStopDao.getByShipment(s);
    }

    /**
     * @param s shipment.
     * @return alternative locations.
     */
    private AlternativeLocations getAlternativeLocations(final Shipment s) {
        return alternativeLocationsDao.getBy(s);
    }

    /**
     * @param locs location profiles.
     * @return list of location profile beans.
     */
    private List<LocationProfileBean> toBeans(final List<LocationProfile> locs) {
        final List<LocationProfileBean> list = new LinkedList<>();
        for (final LocationProfile l : locs) {
            list.add(new LocationProfileBean(l));
        }
        return list;
    }

    /**
     * @param s
     * @param currentTime
     * @param eta
     * @return
     */
    public static int getPercentageCompleted(final Shipment s,
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
     * @return
     */
    protected LocationService getLocationService() {
        return locationService;
    }

    /**
     * @return notification service.
     */
    protected NotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * @param s shipment.
     * @return arrival.
     */
    private Arrival getArrival(final Shipment s) {
        return arrivalDao.getArrival(s);
    }

    /**
     * @param shipmentId
     * @param sn
     * @param tripCount
     * @return
     */
    protected Shipment findShipment(final Long shipmentId, final String sn, final Integer tripCount) {
        if (shipmentId != null) {
            return shipmentDao.findOne(shipmentId);
        } else if (sn != null && tripCount != null) {
            return shipmentDao.findBySnTrip(sn, tripCount);
        }
        return null;
    }

}
