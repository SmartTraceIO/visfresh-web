/**
 *
 */
package com.visfresh.rules;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.ArrivalService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ClearShipmentArrivedStateRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(ClearShipmentArrivedStateRule.class);

    public static final String NAME = "ClearShipmentArrivedState";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    protected ArrivalService arrivalService;

    /**
     * Default constructor.
     */
    public ClearShipmentArrivedStateRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final Shipment shipment = event.getShipment();

        final ShipmentSession session = shipment == null
                ? null : req.getSessionManager().getSession(shipment);
        final boolean accept = !req.isProcessed(this)
                && shipment != null
                && event.getLatitude() != null
                && event.getLongitude() != null
                && arrivalService.hasEnteredLocations(session);

        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();
        final ShipmentSession session = context.getSessionManager().getSession(shipment);
        final Location l = new Location(event.getLatitude(), event.getLongitude());

        context.setProcessed(this);

        final List<LocationProfile> locs = arrivalService.getEnteredLocations(session);
        for (final LocationProfile loc : locs) {
            if (!arrivalService.isNearLocation(loc, l)) {
                arrivalService.clearLocationHistory(loc, session);
                log.debug("The shipment " + shipment.getId() + " is not near "
                        + loc.getName() + ". Arrived state cleared for it");
            }
        }

        return false;
    }

    @PostConstruct
    public void init() {
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public void destroy() {
        engine.setRule(NAME, null);
    }
}
