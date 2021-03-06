/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalNotificationRule extends AbstractNotificationRule {
    private static final Logger log = LoggerFactory.getLogger(ArrivalNotificationRule.class);

    public static final String NAME = "ArrivalNotification";

    @Autowired
    protected ArrivalDao arrivalDao;
    @Autowired
    protected AlertDao alertDao;
    @Autowired
    protected ShipmentDao shipmentDao;

    private EnteringDetectionSupport enteringChecker = new EnteringDetectionSupport(
            3, NAME + "_enteringChecker");

    /**
     * Default constructor.
     */
    public ArrivalNotificationRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        if (!super.accept(req)) {
            return false;
        }

        final Shipment shipment = event.getShipment();
        final ShipmentSession session = req.getSessionManager().getSession(shipment);

        final boolean accept = !session.isArrivalProcessed()
                && LeaveStartLocationRule.isLeavingStartLocation(shipment, session)
                && (enteringChecker.isInControl(session)
                        ||isNearEndLocation(shipment, event.getLatitude(), event.getLongitude()));
        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        final Arrival arrival = new Arrival();
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();

        log.debug("Process arrival notification rule for shipment " + shipment.getId());
        context.setProcessed(this);
        final ShipmentSession session = context.getSessionManager().getSession(shipment);

        if (!isNearEndLocation(shipment, event.getLatitude(), event.getLongitude())) {
            log.debug("Watching of arrival notification state has cleaned for " + shipment.getId());
            enteringChecker.clearInControl(session);
            return false;
        }

        if (enteringChecker.handleEntered(session)) {
            log.debug("Detected needs arrival notification for " + shipment.getId());

            session.setArrivalProcessed(true);

            arrival.setDate(event.getTime());
            arrival.setDevice(event.getDevice());
            arrival.setNumberOfMettersOfArrival(getNumberOfMetersForArrival(
                    event.getLatitude(), event.getLongitude(), shipment.getShippedTo()));
            arrival.setShipment(shipment);
            arrival.setTrackerEventId(event.getId());

            saveArrival(arrival);

            if (!shipment.isExcludeNotificationsIfNoAlerts() || hasTemperatureAlerts(shipment)) {
                //notify subscribers
                final List<PersonSchedule> schedules = getPersonalSchedules(
                        event.getShipment().getArrivalNotificationSchedules(), new Date());
                if (schedules.size() > 0) {
                    sendNotification(schedules, arrival, event);
                }
            }
        } else {
            log.debug("Arrival notification not yet needs for " + shipment.getId());
        }

        return false;
    }

    /**
     * @param shipment shipment.
     * @param latitude latitude of device location.
     * @param longitude longitude of device location.
     * @return
     */
    public static boolean isNearEndLocation(final Shipment shipment, final Double latitude,
            final Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

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
    /**
     * @param shipment
     * @return
     */
    private boolean hasTemperatureAlerts(final Shipment shipment) {
        final List<Alert> alerts = alertDao.getAlerts(shipment);
        for (final Alert a : alerts) {
            if (a instanceof TemperatureAlert) {
                return true;
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

    @Override
    public String getName() {
        return NAME;
    }
    /**
     * @return
     */
    protected static String createProcessedKey() {
        return NAME + "_processed";
    }
}
