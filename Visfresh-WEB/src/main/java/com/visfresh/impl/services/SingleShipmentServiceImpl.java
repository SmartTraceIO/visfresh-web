/**
 *
 */
package com.visfresh.impl.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.SingleShipmentBeanDao;
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
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
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
    private static final Logger log = LoggerFactory.getLogger(SingleShipmentServiceImpl.class);
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
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private SingleShipmentBeanDao shipmentBeanDao;

    /**
     * Default constructor.
     */
    public SingleShipmentServiceImpl() {
        super();
    }

    /**
     * @param shipmentId shipment ID.
     * @return bean without reading based calculations.
     */
    private SingleShipmentBean createLiteBean(final long shipmentId) {
        final Shipment s = getShipment(shipmentId);
        if (s == null) {
            return null;
        }

        final SingleShipmentBean bean = new SingleShipmentBean();
        addShipmentData(bean, s);
        return bean;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#rebuildShipmentData(long)
     */
    @Override
    public void rebuildShipmentData(final long shipmentId) {
        final SingleShipmentBean s = createLiteBean(shipmentId);
        processReadings(s, null);
        saveBean(s);
        log.debug("Single shipment data has rebuilt for shipment " + shipmentId);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#getShipmentData(java.lang.Long)
     */
    @Override
    public SingleShipmentData getShipmentData(final long shipmentId) {
        final Map<Long, SingleShipmentBean> fromDb = asMap(getBeanIncludeSiblings(shipmentId));
        SingleShipmentBean mainShipment = fromDb.get(shipmentId);

        boolean shouldSave = false;
        if (mainShipment == null) {
            mainShipment = createLiteBean(shipmentId);
            shouldSave = true;
        }

        final SingleShipmentData data = new SingleShipmentData();
        processReadings(mainShipment, r -> data.getLocations().add(r));
        data.setBean(mainShipment);

        //add siblings
        for (final Long id : mainShipment.getSiblings()) {
            SingleShipmentBean sibling = fromDb.get(id);
            if (sibling == null) {
                sibling = createLiteBean(id);
                processReadings(sibling, null);
                saveBean(sibling);
            }

            data.getSiblings().add(sibling);
        }

        if (shouldSave) {
            saveBean(mainShipment);
        }
        return data;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SingleShipmentService#getShipmentData(java.lang.String, java.lang.Integer)
     */
    @Override
    public SingleShipmentData getShipmentData(final String sn, final int tripCount) {
        final Long id = getShipmentId(sn, tripCount);
        return id == null ? null : getShipmentData(id);
    }

    /**
     * @param beans beans.
     * @return map of bean ID's to beans.
     */
    private Map<Long, SingleShipmentBean> asMap(final List<SingleShipmentBean> beans) {
        final Map<Long, SingleShipmentBean> map = new HashMap<>();
        for (final SingleShipmentBean bean : beans) {
            map.put(bean.getShipmentId(), bean);
        }
        return map;
    }

    /**
     * @param shipmentId Shipment ID.
     * @return map of shipment beans include main bean and its siblings.
     */
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final long shipmentId) {
        return shipmentBeanDao.getShipmentBeanIncludeSiblings(shipmentId);
    }
    /**
     * @param sn serial number.
     * @param tripCount trip count.
     * @return map of shipment beans include main bean and its siblings.
     */
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final String sn, final int tripCount) {
        return shipmentBeanDao.getShipmentBeanIncludeSiblings(sn, tripCount);
    }

    /**
     * @param bean shipment bean.
     */
    protected void saveBean(final SingleShipmentBean bean) {
        shipmentBeanDao.saveShipmentBean(bean);
    }
    /**
     * @param shipmentId shipment ID.
     * @param sn serial number.
     * @param tripCount trip count.
     * @return shipment.
     */
    protected Shipment getShipment(final long shipmentId) {
        return shipmentDao.findOne(shipmentId);
    }
    /**
     * @param sn serial number.
     * @param tripCount trip count.
     * @return shipment.
     */
    protected Long getShipmentId(final String sn, final int tripCount) {
        return shipmentDao.getShipmentId(sn, tripCount);
    }
    private void processReadings(final SingleShipmentBean bean, final Consumer<SingleShipmentLocationBean> c) {
        final List<AlertBean> alerts = new LinkedList<>(bean.getSentAlerts());

        final long shipmentId = bean.getShipmentId();
        double minTemp = 1000.;
        double maxTemp = -273.;

        final List<TrackerEventDto> events = getReadings(shipmentId);
        if (events != null && events.size() > 0) {
            Location currentLocation = null;
            final TrackerEventDto firstReading = events.get(0);
            //first reading data
            bean.setFirstReadingTime(firstReading.getTime());

            for (final TrackerEventDto e : events) {
                final double t = e.getTemperature();
                if (t < minTemp) {
                    minTemp = t;
                }
                if (t > maxTemp) {
                    maxTemp = t;
                }

                if (c != null) {
                    c.accept(createLocationBean(e, alerts));
                }
                if (e.getLatitude() != null && e.getLongitude() != null) {
                    currentLocation = new Location(e.getLatitude(), e.getLongitude());
                }
            }

            //last reading data
            final TrackerEventDto lastReading = events.get(events.size() - 1);
            bean.setBatteryLevel(lastReading.getBattery());
            bean.setCurrentLocation(currentLocation);
            bean.setCurrentLocationDescription(getLocationDescription(bean.getCurrentLocation()));
            bean.setLastReadingTemperature(lastReading.getTemperature());
            bean.setLastReadingTime(lastReading.getTime());
        }

        bean.setMaxTemp(maxTemp);
        bean.setMinTemp(minTemp);
    }
    /**
     * @param e
     * @param alerts
     * @return
     */
    private SingleShipmentLocationBean createLocationBean(final TrackerEventDto e, final List<AlertBean> alerts) {
        final SingleShipmentLocationBean bean = new SingleShipmentLocationBean(e);

        // add alerts
        final Iterator<AlertBean> iter = alerts.iterator();
        while (iter.hasNext()) {
            final AlertBean a = iter.next();
            if (e.getId().equals(a.getTrackerEventId())) {
                bean.getAlerts().add(a);
                iter.remove();
            }
        }

        return bean;
    }
    /**
     * @param bean
     * @param s
     */
    private void addShipmentData(final SingleShipmentBean bean, final Shipment s) {
        if (s.getAlertProfile() != null) {
            bean.setAlertProfile(new AlertProfileBean(s.getAlertProfile()));
        }

        final Date alertsSuppressedTime = getAlertsSuppressionDate(s);
        if (alertsSuppressedTime != null || isAlertsSuppressed(s)) {
            bean.setAlertsSuppressed(true);
            bean.setAlertsSuppressionTime(alertsSuppressedTime);
        }
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        final Arrival arrival = getArrival(s);
        if (arrival != null) {
            bean.setArrival(new ArrivalBean(arrival));
        }

        bean.setArrivalNotificationWithinKm(s.getArrivalNotificationWithinKm());
        bean.setArrivalReportSent(isArrivalReportSent(s));
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
        bean.setStartTime(s.getShipmentDate());
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

        //alerts
        final List<Alert> alerts = getAlerts(s);
        for (final Alert alert : alerts) {
            final AlertBean ab = alert instanceof TemperatureAlert ?
                    new TemperatureAlertBean((TemperatureAlert) alert)
                    : new AlertBean(alert);
            bean.getSentAlerts().add(ab);
        }

        bean.getAlertFired().addAll(getAlertFired(s));
        bean.getAlertYetToFire().addAll(getAlertYetFoFire(s));
    }
    /**
     * @param s shipment.
     * @return list of alert rules.
     */
    private List<AlertRuleBean> getAlertYetFoFire(final Shipment s) {
        final List<AlertRuleBean> list = new LinkedList<>();
        for (final AlertRule r : getAlertYetFoFireImpl(s)) {
            list.add(createAlertRuleBean(r));
        }
        return list;
    }
    /**
     * @param s shipment.
     * @return list of alert rules.
     */
    private List<AlertRuleBean> getAlertFired(final Shipment s) {
        final List<AlertRuleBean> list = new LinkedList<>();
        for (final AlertRule r : getAlertFiredImpl(s)) {
            list.add(createAlertRuleBean(r));
        }
        return list;
    }

    /**
     * @param r
     * @return
     */
    protected AlertRuleBean createAlertRuleBean(final AlertRule r) {
        if (r instanceof TemperatureRule) {
            return new TemperatureRuleBean((TemperatureRule) r);
        }
        return new AlertRuleBean(r);
    }
    /**
     * @param alert.
     * @return ID of corrective action list if presented.
     */
    public static Long getCorrectiveActionListId(final Alert alert) {
        final AlertProfile alertProfile = alert.getShipment().getAlertProfile();

        if (alert instanceof TemperatureAlert) {
            final Long ruleId = ((TemperatureAlert) alert).getRuleId();
            for (final TemperatureRule rule : alertProfile.getAlertRules()) {
                if (rule.getCorrectiveActions() != null && rule.getId().equals(ruleId)) {
                    return rule.getCorrectiveActions().getId();
                }
            }
        } else {
            final AlertType type = alert.getType();
            if (type == AlertType.Battery && alertProfile.getBatteryLowCorrectiveActions() != null) {
                return alertProfile.getBatteryLowCorrectiveActions().getId();
            }
            if(type == AlertType.LightOn && alertProfile.getLightOnCorrectiveActions() != null) {
                return alertProfile.getLightOnCorrectiveActions().getId();
            }
        }

        return null;
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
    /**
     * @param s shipment.
     * @return list of device groups.
     */
    protected List<DeviceGroupDto> getShipmentGroups(final Shipment s) {
        final List<DeviceGroupDto> groups = deviceGroupDao.getShipmentGroups(
                Collections.singleton(s.getId())).get(s.getId());
        return groups == null ? new LinkedList<>() : groups;
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
     * @param shipmentId
     * @return
     */
    protected List<TrackerEventDto> getReadings(final long shipmentId) {
        final Map<Long, List<TrackerEventDto>> readings = trackerEventDao.getEventsForShipmentIds(
                Collections.singleton(shipmentId));
        return readings.get(shipmentId);
    }
    /**
     * @param loc
     * @return
     */
    protected String getLocationDescription(final Location loc) {
        final String notDeterminet = "Not determined";
        if (loc == null) {
            return notDeterminet;
        }

        final String desc = locationService.getLocationDescription(loc);
        return desc == null ? notDeterminet : desc;
    }
    /**
     * @param s
     * @return
     */
    protected boolean isArrivalReportSent(final Shipment s) {
        return notificationService.isArrivalReportSent(s);
    }
    /**
     * @param s shipment.
     * @return true if alerts suppressed.
     */
    protected boolean isAlertsSuppressed(final Shipment s) {
        return ruleEngine.isAlertsSuppressed(s);
    }
    /**
     * @param s shipment.
     * @return alerts suppression date.
     */
    protected Date getAlertsSuppressionDate(final Shipment s) {
        return ruleEngine.getAlertsSuppressionDate(s);
    }
    /**
     * @param s
     * @return
     */
    protected List<AlertRule> getAlertYetFoFireImpl(final Shipment s) {
        return ruleEngine.getAlertYetFoFire(s);
    }
    /**
     * @param s
     * @return
     */
    protected List<AlertRule> getAlertFiredImpl(final Shipment s) {
        return ruleEngine.getAlertFired(s);
    }

    /**
     * @param s shipment.
     * @return list of alerts.
     */
    protected List<Alert> getAlerts(final Shipment s) {
        return alertDao.getAlerts(s);
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
    protected AlternativeLocations getAlternativeLocations(final Shipment s) {
        return alternativeLocationsDao.getBy(s);
    }
    /**
     * @param s shipment.
     * @return arrival.
     */
    protected Arrival getArrival(final Shipment s) {
        return arrivalDao.getArrival(s);
    }
}
