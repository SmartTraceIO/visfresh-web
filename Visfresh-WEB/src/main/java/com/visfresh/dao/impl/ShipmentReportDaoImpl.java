/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Notification;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.ChartBundle;
import com.visfresh.reports.shipment.AlertBean;
import com.visfresh.reports.shipment.ArrivalBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
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
        bean.setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
        bean.setUpperTemperatureLimit(ap.getUpperTemperatureLimit());

        //add fired alerts
        final ChartBundle chartBundle = new ChartBundle();
        final List<Alert> alerts = alertDao.getAlerts(s);
        for (final Alert a : alerts) {
            bean.getAlertsFired().add(new AlertBean(a.getType(), chartBundle.buildDescription(
                    a, EntityUtils.getEntity(events, a.getTrackerEventId()),
                    user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits())));
        }

        //create alert map
        final Map<Long, User> notifiedPersons = new HashMap<>();

        //add notified persons
        final List<Notification> notifs = notificationDao.getForIssues(EntityUtils.getIdList(alerts));
        for (final Notification n : notifs) {
            notifiedPersons.put(n.getUser().getId(), n.getUser());
        }

        for (final User u: notifiedPersons.values()) {
            bean.getWhoWasNotified().add(u.getEmail());
        }

        final int n = events.size();
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

        //standard deviation
        double sd = 0.;
        if (n > 1) {
            for (final TrackerEvent e : events) {
                final double t = e.getTemperature();
                sd += (t - avg) * (t - avg) / (n - 1);

                startTime = Math.min(startTime, e.getTime().getTime());
                endTime = Math.max(endTime, e.getTime().getTime());
            }

            sd = Math.sqrt(sd);
        }

        if (endTime > startTime) {
            bean.setTotalTime(endTime - startTime);
            bean.setMinimumTemperature(min);
            bean.setMaximumTemperature(max);
            bean.setAvgTemperature(avg);
            bean.setStandardDevitation(sd);
        }

        bean.setTimeAboveUpperLimit(getInsedentTime(events, new TemperatureIncedentDetector() {
            @Override
            public boolean haveIncedent(final double temperature) {
                return temperature > ap.getUpperTemperatureLimit();
            }
        }));
        bean.setTimeBelowLowerLimit(getInsedentTime(events, new TemperatureIncedentDetector() {
            @Override
            public boolean haveIncedent(final double temperature) {
                return temperature < ap.getLowerTemperatureLimit();
            }
        }));
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
