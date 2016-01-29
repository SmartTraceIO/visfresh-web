/**
 *
 */
package com.visfresh.rules;

import java.util.Calendar;
import java.util.Date;
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
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.utils.LocationUtils;

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
    @Autowired
    private DeviceCommandService commandService;

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
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final boolean accept = super.accept(req) && isNearEndLocation(
                event.getShipment(), event.getLatitude(), event.getLongitude());
        if (accept) {
            log.debug("Arrival Rule matches for shipment "
                    + event.getShipment().getShipmentDescription()
                    + " tracker event. Notification will generated");
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
        if (shipment.getArrivalNotificationWithinKm() != null && endLocation != null) {
            final double distance = getNumberOfMetersForArrival(latitude, longitude, endLocation);
            return distance <= shipment.getArrivalNotificationWithinKm();
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
        double distance = LocationUtils.getDistanceMeters(latitude, longitude, end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - endLocation.getRadius());
        return (int) Math.round(distance);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        final Arrival arrival = new Arrival();
        final TrackerEvent event = context.getEvent();
        context.setProcessed(this);

        arrival.setDate(event.getTime());
        arrival.setDevice(event.getDevice());
        arrival.setNumberOfMettersOfArrival(getNumberOfMetersForArrival(
                event.getLatitude(), event.getLongitude(), event.getShipment().getShippedTo()));
        arrival.setShipment(event.getShipment());

        saveArrival(arrival);
        if (event.getShipment().getShutdownDeviceAfterMinutes() != null) {
            final long date = System.currentTimeMillis() + event.getShipment().getShutdownDeviceAfterMinutes() * 60 * 1000l;
            commandService.shutdownDevice(event.getDevice(), new Date(date));
        }

        final Calendar date = new GregorianCalendar();
        //notify subscribers
        final List<PersonSchedule> schedules = getAllPersonalSchedules(event.getShipment());
        for (final PersonSchedule s : schedules) {
            if (matchesTimeFrame(s, date)) {
                sendNotification(s, arrival);
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
    protected List<PersonSchedule> getAllPersonalSchedules(final Shipment shipment) {
        final List<PersonSchedule> all = new LinkedList<PersonSchedule>();

        final List<NotificationSchedule> schedules = shipment.getArrivalNotificationSchedules();
        for (final NotificationSchedule schedule : schedules) {
            final List<PersonSchedule> personalSchedules = schedule.getSchedules();
            all.addAll(personalSchedules);
        }

        return all;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
