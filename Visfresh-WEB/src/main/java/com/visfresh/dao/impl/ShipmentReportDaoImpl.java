/**
 *
 */
package com.visfresh.dao.impl;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
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
    public interface TemperatureIncedentDetector {
        boolean haveIncedent(double temperature);
    }

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
        if (s.getArrivalDate() != null) {
            addArrival(s, bean);
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
//        excluding 2hr cooldown period
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        if (s.getDevice().getColor() != null) {
            bean.setDeviceColor(parseColor(s.getDevice().getColor()));
        }
        if (s.getShippedFrom() != null) {
            bean.setShippedFrom(s.getShippedFrom().getName());
        }
        if (s.getShippedTo() != null) {
            bean.setShippedTo(s.getShippedTo().getName());
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
        addTemperatureHistory(s, events, bean);
        return bean;
    }
    /**
     * @param color
     * @return
     */
    private Color parseColor(final com.visfresh.entities.Color color) {
        return Color.decode(color.getHtmlValue());
    }

    /**
     * @param s shipment.
     * @param bean shipment report bean
     */
    private void addTemperatureHistory(final Shipment s,
            final List<TrackerEvent> originEvents, final ShipmentReportBean bean) {
        if (s.getAlertProfile() != null) {
            final AlertProfile ap = s.getAlertProfile();
            bean.setAlertProfile(ap.getName());
            bean.setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
            bean.setUpperTemperatureLimit(ap.getUpperTemperatureLimit());
        }

        //add fired alerts
        bean.getFiredAlertRules().addAll(ruleEngine.getAlertFired(s));

        //create alert map
        final Map<Long, User> notifiedPersons = new HashMap<>();

        //add notified persons
        final List<Alert> alerts = alertDao.getAlerts(s);
        bean.getAlerts().addAll(alerts);

        final List<Notification> notifs = notificationDao.getForIssues(EntityUtils.getIdList(alerts));
        for (final Notification n : notifs) {
            notifiedPersons.put(n.getUser().getId(), n.getUser());
        }

        for (final User u: notifiedPersons.values()) {
            bean.getWhoWasNotified().add(u.getEmail());
        }


        final List<TrackerEvent> events = new LinkedList<>(originEvents);
        if (bean.getAlertSuppressionMinutes() > 0) {
            //exclude events when alerts suppressed
            final long t0 = bean.getAlertSuppressionMinutes() * 60 * 1000l + bean.getDateShipped().getTime();
            while (events.size() > 0) {
                final TrackerEvent e = events.get(0);
                if (e.getTime().getTime() < t0) {
                    events.remove(0);
                } else {
                    break;
                }
            }
        }

        final int n = events.size();
        if (n > 0) {
            long startTime = Long.MAX_VALUE;
            long endTime = Long.MIN_VALUE;

            //min/max/average temperature
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double avg = 0.;

            for (final TrackerEvent e : events) {
                final double t = e.getTemperature();
                min = Math.min(min, t);
                max = Math.max(max, t);
                avg += t / n;
            }

            bean.setAvgTemperature(avg);
            bean.setMinimumTemperature(min);
            bean.setMaximumTemperature(max);

            //standard deviation
            double sd = 0.;
            for (final TrackerEvent e : events) {
                final double t = e.getTemperature();
                final double dt = (t - avg);
                sd += dt * dt;

                startTime = Math.min(startTime, e.getTime().getTime());
                endTime = Math.max(endTime, e.getTime().getTime());
            }

            if (n > 1) {
                sd = Math.sqrt(sd / (n - 1));
            } else {
                sd = 0;
            }

            bean.setTotalTime(endTime - startTime);
            bean.setStandardDevitation(sd);

            bean.setTimeAboveUpperLimit(getInsedentTime(events, new TemperatureIncedentDetector() {
                @Override
                public boolean haveIncedent(final double temperature) {
                    return temperature > bean.getUpperTemperatureLimit();
                }
            }));
            bean.setTimeBelowLowerLimit(getInsedentTime(events, new TemperatureIncedentDetector() {
                @Override
                public boolean haveIncedent(final double temperature) {
                    return temperature < bean.getLowerTemperatureLimit();
                }
            }));
        }
    }

    protected long getInsedentTime(final List<TrackerEvent> events, final TemperatureIncedentDetector d) {
        long totalTime = 0;
        TrackerEvent tre = null;

        for (final TrackerEvent e : events) {
            if (tre != null){
                //add incident time
                totalTime += e.getTime().getTime() - tre.getTime().getTime();
            }

            if (d.haveIncedent(e.getTemperature())) {
                //add matched event to incidents.
                tre = e;
            } else {
                tre = null;
            }
        }

        return totalTime;
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
