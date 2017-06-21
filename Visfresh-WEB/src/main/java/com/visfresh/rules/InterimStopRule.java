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

        if (super.accept(req)) {
            final InterimStopInfo stp = getInterimStop(session);
            if (stp == null) {
                return getBestLocation(shipment, event.getLatitude(), event.getLongitude()) != null;
            } else {
                return stp.getId() == null && !leaveInterimStop(event, shipment, stp);
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent event = context.getEvent();
        final ShipmentSession state = context.getSessionManager().getSession(event.getShipment());

        InterimStopInfo stop = getInterimStop(state);
        final List<LocationProfile> locs = getInterimLocations(event.getShipment());
        boolean shouldCreateStop = false;

        if (stop == null) {
            stop = new InterimStopInfo();
            stop.setStartTime(event.getTime().getTime());

            //if STP message type, then not need to wait next reading inside of location
            //just need save interim stop
            if (event.getType() == TrackerEventType.STP) {
                shouldCreateStop = true;
            }
        } else if (stop.getId() == null){
            shouldCreateStop = true;
        }

        stop.setLatitude(event.getLatitude());
        stop.setLongitude(event.getLongitude());

        final LocationProfile bestLocation = getBestLocation(locs, event.getLatitude(), event.getLongitude());
        if (shouldCreateStop) {
            //update stop time
            final InterimStop s = new InterimStop();
            s.setLocation(bestLocation);
            s.setDate(new Date(stop.getStartTime()));
            s.setTime((int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE));

            log.debug("Interim stop detected near location " + s.getLocation().getId()
                    + " (" + s.getLocation().getName() + ")");
            final Long id = save(event.getShipment(), s);
            stop.setId(id);
        } else {
            log.debug("The shipment " + event.getShipment().getId()
                    + " just entering location ("
                    + bestLocation.getName()
                    + ") start to watch a possible interim stop");
        }

        setInterimStopState(state, stop);

        return false;
    }

    public String getName() {
        return NAME;
    }
}
