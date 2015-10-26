/**
 *
 */
package com.visfresh.drools;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ArrivalDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalRule extends AbstractNotificationRule {
    private static final Logger log = LoggerFactory.getLogger(ArrivalRule.class);

    public static final String NAME = "Arrival";

    @Autowired
    protected ArrivalDao arrivalDao;

    /**
     * Default constructor.
     */
    public ArrivalRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final TrackerEventRequest req) {
        final TrackerEvent event = req.getEvent();
        final boolean accept = super.accept(req) && isNearEndLocation(
                event.getShipment(), event.getLatitude(), event.getLongitude());
        if (accept) {
            log.debug("Arrival Rule matches for shipment "
                    + event.getShipment().getName() + " tracker event. Notification will generated");
        }
        return accept;
    }
    /**
     * @param shipment shipment.
     * @param latitude latitude of device location.
     * @param longitude longitude of device location.
     * @return
     */
    public static boolean isNearEndLocation(final Shipment shipment, final double latitude,
            final double longitude) {
        final LocationProfile endLocation = shipment.getShippedTo();
        if (endLocation != null) {
            final double distance = getNumberOfMetersForArrival(latitude, longitude, endLocation);
            return distance < shipment.getArrivalNotificationWithIn();
        }
        return false;
    }

    /**
     * @param latitude
     * @param longitude
     * @param endLocation
     * @return
     */
    protected static int getNumberOfMetersForArrival(final double latitude,
            final double longitude, final LocationProfile endLocation) {
        final Location end = endLocation.getLocation();
        final double distance = distFrom(latitude, longitude, end.getLatitude(), end.getLongitude());
        return (int) Math.round(distance);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final TrackerEventRequest req) {
        final Arrival arrival = new Arrival();
        final TrackerEvent event = req.getEvent();
        req.putClientProperty(this, Boolean.TRUE);

        arrival.setDate(event.getTime());
        arrival.setDevice(event.getDevice());
        arrival.setNumberOfMettersOfArrival(getNumberOfMetersForArrival(
                event.getLatitude(), event.getLongitude(), event.getShipment().getShippedTo()));
        arrival.setShipment(event.getShipment());

        saveArrival(arrival);

        final Calendar date = new GregorianCalendar();
        final String message = "Device is in " + arrival.getNumberOfMettersOfArrival() + " meters for arrival";
        //notify subscribers
        final List<PersonalSchedule> schedules = getAllPersonalSchedules(event.getShipment());
        for (final PersonalSchedule s : schedules) {
            if (matchesTimeFrame(s, date)) {
                sendNotification(s, "Arrival Notification", message);
            }
        }

        return false;
    }

    /**
     * @param a alert to save.
     */
    protected void saveArrival(final Arrival a) {
        arrivalDao.save(a);
    }

    /**
     * @param shipment shipment.
     * @return
     */
    @Override
    protected List<PersonalSchedule> getAllPersonalSchedules(final Shipment shipment) {
        final List<PersonalSchedule> all = new LinkedList<PersonalSchedule>();

        final List<NotificationSchedule> schedules = shipment.getArrivalNotificationSchedules();
        for (final NotificationSchedule schedule : schedules) {
            final List<PersonalSchedule> personalSchedules = schedule.getSchedules();
            all.addAll(personalSchedules);
        }

        return all;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static double distFrom(final double lat1, final double lng1, final double lat2, final double lng2) {
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLng = Math.toRadians(lng2 - lng1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        final double earthRadius = 6371000; // meters
        return earthRadius * c;
    }
}
