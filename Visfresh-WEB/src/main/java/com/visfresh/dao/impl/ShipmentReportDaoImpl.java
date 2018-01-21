/**
 *
 */
package com.visfresh.dao.impl;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.reports.shipment.ArrivalBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;
import com.visfresh.services.RuleEngine;
import com.visfresh.utils.EntityUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentReportDaoImpl implements ShipmentReportDao {
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private AlternativeLocationsDao alternativeLocationsDao;
    @Autowired
    private InterimStopDao interimStopDao;
    @Autowired
    private RuleEngine ruleEngine;

    /**
     * Default constructor.
     */
    public ShipmentReportDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentReportDao#createReport(com.visfresh.entities.Shipment)
     */
    @Override
    public ShipmentReportBean createReport(final Shipment s) {
        final ShipmentReportBean bean = new ShipmentReportBean();
        final Arrival arrival = arrivalDao.getArrival(s);

        if (arrival != null) {
            final ArrivalBean ab = new ArrivalBean();
            ab.setNotifiedAt(arrival.getDate());
            ab.setNotifiedWhenKm(arrival.getNumberOfMettersOfArrival() / 1000);
            bean.setArrival(ab);
        }

        if (s.getArrivalDate() != null) {
            bean.setDateArrived(s.getArrivalDate());
        }

        bean.setCompanyName(s.getCompany().getName());
        bean.setAssetNum(s.getAssetNum());
        bean.setComment(s.getCommentsForReceiver());
        bean.setDateShipped(s.getShipmentDate());
        bean.setDescription(s.getShipmentDescription());
        bean.setDevice(s.getDevice().getImei());
        bean.setDeviceModel(s.getDevice().getModel());
        bean.setNumberOfSiblings(s.getSiblingCount());
        bean.setPalletId(s.getPalletId());
        bean.setShutdownTime(s.getDeviceShutdownTime());
//        excluding 2hr cooldown period
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        if (s.getDevice().getColor() != null) {
            bean.setDeviceColor(parseColor(s.getDevice().getColor()));
        }
        bean.setShippedFrom(s.getShippedFrom());
        bean.getInterimStops().addAll(interimStopDao.getByShipment(s));

        if (s.getShippedTo() != null) {
            bean.setShippedTo(s.getShippedTo());
        } else {
            final AlternativeLocations alts = alternativeLocationsDao.getBy(s);
            if (alts != null && !alts.getTo().isEmpty()) {
                final List<String> altNames = new LinkedList<>();
                for (final LocationProfile altLoc : alts.getTo()) {
                    altNames.add(altLoc.getName());
                }

                bean.setPossibleShippedTo(altNames);
            }
        }
        bean.setStatus(s.getStatus());
        bean.setTripCount(s.getTripCount());

        //add readings
        final List<TrackerEvent> events = trackerEventDao.getEvents(s);
        for (final TrackerEvent e : events) {
            bean.getReadings().add(new ShortTrackerEvent(e));
        }

        //add temperature history
        final AlertProfile ap = s.getAlertProfile();
        if (s.getAlertProfile() != null) {
            bean.setAlertProfile(ap.getName());
        }

        //add fired alerts
        bean.getFiredAlertRules().addAll(ruleEngine.getAlertFired(s));

        //add notified persons
        final List<Alert> alerts = alertDao.getAlerts(s);
        bean.getAlerts().addAll(alerts);

        //get notifications
        //notified by alert
        bean.getWhoWasNotifiedByAlert().addAll(
            toNameList(getNotifiedUsers(alerts, NotificationType.Alert)));

        List<User> usersReceivedReports = new LinkedList<>();
        if (s.getStatus() == ShipmentStatus.Arrived) {
            if (arrival != null) {
                //notified users
                final List<Arrival> arrivals = new LinkedList<>();
                arrivals.add(arrival);
                usersReceivedReports = getNotifiedUsers(arrivals, NotificationType.Arrival);
            }
        } else if (!s.hasFinalStatus()){
            //user to be notified map. Map - because need to avoid of duplicates.
            final Map<Long, User> all = new HashMap<>();
            //get not potentially notified users without filtering by time frames
            for (final NotificationSchedule schedule : s.getArrivalNotificationSchedules()) {
                final List<PersonSchedule> personalSchedules = schedule.getSchedules();
                for (final PersonSchedule ps : personalSchedules) {
                    all.put(ps.getUser().getId(), ps.getUser());
                }
            }
            usersReceivedReports = new LinkedList<>(all.values());
        }

        bean.getWhoReceivedReport().addAll(toNameList(usersReceivedReports));
        bean.setTemperatureStats(createTemperatureStats(s.getAlertProfile(), events));
        return bean;
    }
    /**
     * @param issues
     * @return
     */
    private List<User> getNotifiedUsers(final List<? extends NotificationIssue> issues, final NotificationType type) {
        final Set<Long> ids = new HashSet<>();
        final List<User> list = new LinkedList<>();

        final List<Notification> notifs = notificationDao.getForIssues(EntityUtils.getIdList(issues), type);
        for (final Notification n : notifs) {
            final User u = n.getUser();
            if (!ids.contains(u.getId())) {
                ids.add(u.getId()); //avoid duplicates
                list.add(u);
            }
        }

        return list;
    }
    private List<String> toNameList(final List<User> users) {
        final List<String> names = new LinkedList<>();
        for (final User u : users) {
            names.add(ShipmentReportBuilder.createUserName(u));
        }
        return names;
    }
    /**
     * @param bean
     * @param events
     */
    public static TemperatureStats createTemperatureStats(final AlertProfile ap,
            final List<TrackerEvent> originEvents) {
        final double lowerTemperatureLimit = ap == null ? AlertProfile.DEFAULT_LOWER_TEMPERATURE_LIMIT : ap.getLowerTemperatureLimit();
        final double upperTemperatureLimit = ap == null ? AlertProfile.DEFAULT_UPPER_TEMPERATURE_LIMIT : ap.getUpperTemperatureLimit();

        final TemperatureStats stats;
        if (!originEvents.isEmpty()) {
            final AlertProfileTemperatureStatsCollector c = new AlertProfileTemperatureStatsCollector();
            for (final TrackerEvent e : originEvents) {
                c.processEvent(e, lowerTemperatureLimit, upperTemperatureLimit);
            }

            stats = c.getStatistics();
        } else {
            stats = new TemperatureStats();
        }

        stats.setLowerTemperatureLimit(lowerTemperatureLimit);
        stats.setUpperTemperatureLimit(upperTemperatureLimit);
        return stats;
    }
    /**
     * @param color
     * @return
     */
    private Color parseColor(final com.visfresh.entities.Color color) {
        return Color.decode(color.getHtmlValue());
    }
}
