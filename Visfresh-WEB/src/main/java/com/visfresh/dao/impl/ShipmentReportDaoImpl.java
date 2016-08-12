/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.ChartBundle;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.reports.shipment.ArrivalBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.TimeWithLabel;
import com.visfresh.rules.TemperatureAlertRule;
import com.visfresh.rules.state.ShipmentSession;
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
    private ShipmentSessionDao shipmentSessionDao;
    @Autowired
    private NotificationDao notificationDao;

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
    public ShipmentReportBean createReport(final Shipment s, final User user) {
        final ShipmentReportBean bean = new ShipmentReportBean();
        if (s.getArrivalDate() != null) {
            addArrival(s, bean);
            bean.setDateArrived(s.getArrivalDate());
        }

        bean.setCompanyName(user.getCompany().getName());
        bean.setAssetNum(s.getAssetNum());
        bean.setComment(s.getCommentsForReceiver());
        bean.setDateShipped(s.getShipmentDate());
        bean.setDescription(s.getShipmentDescription());
        bean.setDevice(s.getDevice().getImei());
        bean.setNumberOfSiblings(s.getSiblingCount());
        bean.setPalletId(s.getPalletId());
        if (s.getShippedFrom() != null) {
            bean.setShippedFrom(s.getShippedFrom().getName());
        }
        if (s.getShippedTo() != null) {
            bean.setShippedTo(s.getShippedTo().getName());
        }
        bean.setStatus(s.getStatus());
        //add alert suppression
        final ShipmentSession session = shipmentSessionDao.getSession(s);
        bean.setSuppressFurtherAlerts(session == null ? false : session.isAlertsSuppressed());
        bean.setTripCount(s.getTripCount());

        //add readings
        final List<TrackerEvent> events = trackerEventDao.getEvents(s);
        for (final TrackerEvent e : events) {
            bean.getReadings().add(new ShortTrackerEvent(e));
        }

        //add temperature history
        addTemperatureHistory(s, events, bean, user);
        return bean;
    }
    /**
     * @param s shipment.
     * @param bean shipment report bean
     */
    private void addTemperatureHistory(final Shipment s,
            final List<TrackerEvent> events, final ShipmentReportBean bean, final User user) {
        final AlertProfile ap = s.getAlertProfile();
        if (ap == null) {
            return;
        }

        bean.setAlertProfile(ap.getName());

        //add fired alerts
        final ChartBundle chartBundle = new ChartBundle();
        final List<Alert> alerts = alertDao.getAlerts(s);
        for (final Alert a : alerts) {
            bean.getAlertsFired().add(chartBundle.buildDescription(
                    a, getTrackerEvent(events, a.getTrackerEventId()),
                    user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits()));
        }

        //create alert map
        final RuleBundle ruleBundle = new RuleBundle();
        final Map<TemperatureRule, Long> ruleMap = createRuleIncedentMap(s, events);

        for (final Map.Entry<TemperatureRule, Long> e : ruleMap.entrySet()) {
            if (e.getValue() > 0) {
                final TimeWithLabel tl = new TimeWithLabel();
                tl.setLabel(ruleBundle.buildDescription(e.getKey(), user.getTemperatureUnits()));
                tl.setTotalTime(e.getValue());
            }
        }

        final Map<Long, User> notifiedPersons = new HashMap<>();

        //add notified persons
        final List<Notification> notifs = notificationDao.getForIssues(EntityUtils.getIdList(alerts));
        for (final Notification n : notifs) {
            notifiedPersons.put(n.getUser().getId(), n.getUser());
        }

        for (final User u: notifiedPersons.values()) {
            bean.getWhoWasNotified().add(u.getEmail());
        }

        //alert schedules
        for(final NotificationSchedule sched: s.getAlertsNotificationSchedules()) {
            bean.getSchedules().add(sched.getName());
        }

        final int n = events.size();
        long startTime = Long.MAX_VALUE;
        long endTime = Long.MIN_VALUE;

        //average temperature
        double avg = 0.;
        for (final TrackerEvent e : events) {
            avg += e.getTemperature() / n;
        }

        //standard deviation
        double sd = 0.;
        if (n > 1) {
            for (final TrackerEvent e : events) {
                final double t = e.getTemperature();
                sd += (t - avg) * (t - avg) / (n - 1);

                startTime = Math.min(startTime, e.getTime().getTime());
                endTime = Math.max(startTime, e.getTime().getTime());
            }

            sd = Math.sqrt(sd);
        }

        if (endTime > startTime) {
            bean.setTotalTime(endTime - startTime);
            bean.setAvgTemperature(avg);
            bean.setStandardDevitation(sd);
        }
    }
    /**
     * @param s
     * @param events
     * @return
     */
    private Map<TemperatureRule, Long> createRuleIncedentMap(final Shipment s,
            final List<TrackerEvent> events) {
        final Map<TemperatureRule, Long> result = new HashMap<>();
        //initially populate map
        final List<TemperatureRule> rules = s.getAlertProfile().getAlertRules();
        for (final TemperatureRule r : rules) {
            result.put(r, 0l);
        }

        final Map<TemperatureRule, List<TrackerEvent>> incidents = new HashMap<>();
        for (final TrackerEvent e : events) {
            for (final TemperatureRule r : rules) {
                //process rule
                List<TrackerEvent> tre = incidents.get(r);
                if (TemperatureAlertRule.isMatches(r, e.getTemperature())) {
                    if (tre == null) {
                        //add matched event to incidents.
                        tre = new LinkedList<>();
                        incidents.put(r, tre);

                        tre.add(e);
                    }
                } else if (tre != null){
                    incidents.remove(r);

                    //add incident time
                    if (tre.size() > 0) {
                        final long dt = tre.get(tre.size() - 1).getTime().getTime() - tre.get(0).getTime().getTime();
                        result.put(r, result.get(r) + dt);
                    }
                }
            }
        }

        return result;
    }

    /**
     * @param events
     * @param id
     * @return
     */
    private TrackerEvent getTrackerEvent(final List<TrackerEvent> events, final Long id) {
        for (final TrackerEvent e : events) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }
    /**
     * @param s shipment.
     * @param bean shipment report bean.
     */
    private void addArrival(final Shipment s, final ShipmentReportBean bean) {
        final Arrival arrival = arrivalDao.getArrival(s);

        if (arrival != null) {
            final ArrivalBean ab = new ArrivalBean();
            ab.setNotifiedAt(arrival.getDate());
            ab.setNotifiedWhenKm(arrival.getNumberOfMettersOfArrival() / 1000);
            ab.setShutdownTime(s.getDeviceShutdownTime());
            ab.setTime(s.getArrivalDate());
            bean.setArrival(ab);
        }
    }
}
