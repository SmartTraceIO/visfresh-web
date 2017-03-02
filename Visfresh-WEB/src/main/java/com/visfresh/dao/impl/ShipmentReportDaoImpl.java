/**
 *
 */
package com.visfresh.dao.impl;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
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
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.reports.shipment.ArrivalBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
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
        bean.setNumberOfSiblings(s.getSiblingCount());
        bean.setPalletId(s.getPalletId());
        bean.setShutdownTime(s.getDeviceShutdownTime());
//        excluding 2hr cooldown period
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        if (s.getDevice().getColor() != null) {
            bean.setDeviceColor(parseColor(s.getDevice().getColor()));
        }
        if (s.getShippedFrom() != null) {
            bean.setShippedFrom(s.getShippedFrom().getName());
            bean.setShippedFromLocation(s.getShippedFrom().getLocation());
        }
        if (s.getShippedTo() != null) {
            bean.setShippedTo(s.getShippedTo().getName());
            bean.setShippedToLocation(s.getShippedTo().getLocation());
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
        if (s.getAlertProfile() != null) {
            final AlertProfile ap = s.getAlertProfile();
            bean.setAlertProfile(ap.getName());
            bean.getTemperatureStats().setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
            bean.getTemperatureStats().setUpperTemperatureLimit(ap.getUpperTemperatureLimit());
        }

        //add fired alerts
        bean.getFiredAlertRules().addAll(ruleEngine.getAlertFired(s));

        //add notified persons
        final List<Alert> alerts = alertDao.getAlerts(s);
        bean.getAlerts().addAll(alerts);

        //get notifications
        //notified by alert
        bean.getWhoWasNotifiedByAlert().addAll(getNotified(alerts, NotificationType.Alert));

        //arrivals
        if (arrival != null) {
            final List<Arrival> arrivals = new LinkedList<>();
            arrivals.add(arrival);
            bean.getWhoWasNotifiedByArrival().addAll(getNotified(arrivals, NotificationType.Arrival));
        }

        if (s.getAlertProfile() != null) {
            bean.setTemperatureStats(createTemperatureStats(s.getAlertProfile(), events));
        }

        return bean;
    }
    /**
     * @param issues
     * @return
     */
    private List<String> getNotified(final List<? extends NotificationIssue> issues, final NotificationType type) {
        final Set<Long> ids = new HashSet<>();
        final List<String> list = new LinkedList<>();

        final List<Notification> notifs = notificationDao.getForIssues(EntityUtils.getIdList(issues), type);
        for (final Notification n : notifs) {
            final User u = n.getUser();
            if (!ids.contains(u.getId())) {
                ids.add(u.getId()); //avoid duplicates
                list.add(createUserName(u));
            }
        }

        return list;
    }

    /**
     * @param u user.
     * @return user name.
     */
    private String createUserName(final User u) {
        final StringBuilder sb = new StringBuilder();
        if (u.getFirstName() != null) {
            sb.append(u.getFirstName());
        }
        if (u.getLastName() != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(u.getLastName());
        }

        //add email instead name if empty
        if (sb.length() < 1) {
            sb.append(u.getEmail());
        }
        return sb.toString();
    }

    /**
     * @param bean
     * @param events
     */
    private TemperatureStats createTemperatureStats(final AlertProfile ap,
            final List<TrackerEvent> originEvents) {
        final AlertProfileTemperatureStatsCollector c = new AlertProfileTemperatureStatsCollector();
        for (final TrackerEvent e : originEvents) {
            c.processEvent(e);
        }

        final TemperatureStats stats = c.getStatistics();
        stats.setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
        stats.setUpperTemperatureLimit(ap.getUpperTemperatureLimit());
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
