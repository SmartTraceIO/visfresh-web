/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.ShipmentShutdownService;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SetShipmentArrivedRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(SetShipmentArrivedRule.class);

    public static final String NAME = "SetShipmentArrived";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    protected ShipmentShutdownService shutdownService;

    /**
     * Default constructor.
     */
    public SetShipmentArrivedRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final Shipment shipment = event.getShipment();

        final boolean accept = !req.isProcessed(this)
                && shipment != null
                && !shipment.hasFinalStatus()
                && isNearEndLocation(shipment, event.getLatitude(), event.getLongitude());

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
            return distance < 1.0;
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
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();

        context.setProcessed(this);

        shipment.setStatus(ShipmentStatus.Arrived);
        shipment.setArrivalDate(new Date());
        shipmentDao.save(shipment);
        log.debug("Shipment status for " + shipment.getId() + " has set to "+ ShipmentStatus.Arrived);

        if (shipment.getShutdownDeviceAfterMinutes() != null) {
            final long date = System.currentTimeMillis()
                    + shipment.getShutdownDeviceAfterMinutes() * 60 * 1000l;
            shutdownService.sendShipmentShutdown(shipment, new Date(date));
        }

        return false;
    }

    @PostConstruct
    public void listenSystemMessage() {
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public void destroy() {
        engine.setRule(NAME, null);
    }
}
