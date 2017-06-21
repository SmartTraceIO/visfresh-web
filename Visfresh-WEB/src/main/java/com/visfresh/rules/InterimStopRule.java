/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class InterimStopRule extends AbstractInterimStopRule {
    private static final Logger log = LoggerFactory.getLogger(InterimStopRule.class);

    public static final String NAME = "InterimStop";

    private static final long MINUTE = 60 * 1000l;

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public InterimStopRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(getName(), this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final Shipment shipment = event.getShipment();
        final ShipmentSession session = shipment == null ? null : req.getSessionManager().getSession(shipment);

        final boolean accept = shipment != null
                && !req.isProcessed(this)
                && !shipment.hasFinalStatus()
                && LeaveStartLocationRule.isLeavingStartLocation(shipment, session)
                && getInterimLocations(shipment) != null;

        if (accept) {
            if (isInInterimStop(session)) {
                return true;
            }

            if(isNearInterimStop(shipment, session, event.getLatitude(), event.getLongitude())) {
                //only one interim stop can be used in given version of API.
                return !hasInterimStops(shipment);
            } else {
                return false;
            }
        }

        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent event = context.getEvent();
        final ShipmentSession state = context.getSessionManager().getSession(event.getShipment());
        if (isNearInterimStop(event.getShipment(), state, event.getLatitude(), event.getLongitude())) {
            InterimStopInfo stop = getInterimStop(state);
            final List<LocationProfile> locs = getInterimLocations(event.getShipment());
            boolean shouldCreateStop = false;

            if (stop == null) {
                stop = new InterimStopInfo();
                stop.setLatitude(event.getLatitude());
                stop.setLongitude(event.getLongitude());
                stop.setStartTime(event.getTime().getTime());

                //if STP message type, then not need to wait next reading inside of location
                //just need save interim stop
                if (event.getType() == TrackerEventType.STP) {
                    shouldCreateStop = true;
                }
            } else if (stop.getId() == null){
                shouldCreateStop = true;
            } else {
                //update stop time in DB
                final int minutes = (int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE);
                updateStopTime(stop, minutes);
                log.debug("Stop time for shipment " + event.getShipment().getId()
                        + " has update to " + minutes + " min");
            }

            if (shouldCreateStop) {
                //update stop time
                final InterimStop s = new InterimStop();
                s.setLocation(getBestLocation(locs, event.getLatitude(), event.getLongitude()));
                s.setDate(new Date(stop.getStartTime()));
                s.setTime((int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE));

                log.debug("Interim stop detected near location " + s.getLocation().getId()
                        + " (" + s.getLocation().getName() + ")");
                final Long id = save(event.getShipment(), s);
                stop.setId(id);
            }

            setInterimStopState(state, stop);
        } else if (isInInterimStop(state)) {
            //remove interim stop
            log.debug("Interim stop of shipment " + event.getShipment().getId() + " has finished");
            state.setShipmentProperty(createInterimStopKey(), null);
        }

        return false;
    }

    public String getName() {
        return NAME;
    }
    /**
     * @return
     */
    private static String createInterimStopKey() {
        return NAME + "-stop";
    }
}
