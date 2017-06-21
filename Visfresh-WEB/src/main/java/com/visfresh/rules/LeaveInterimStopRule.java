/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LeaveInterimStopRule extends AbstractInterimStopRule {
    private static final Logger log = LoggerFactory.getLogger(LeaveInterimStopRule.class);

    public static final String NAME = "LeaveInterimStop";

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public LeaveInterimStopRule() {
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
        if (super.accept(req)) {
            final TrackerEvent event = req.getEvent();
            final Shipment shipment = event.getShipment();
            final ShipmentSession session = shipment == null ? null : req.getSessionManager().getSession(shipment);

            final InterimStopInfo info = getInterimStop(session);
            if (info != null) {
                return leaveInterimStop(event, shipment, info);
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
        final ShipmentSession session = context.getSessionManager().getSession(event.getShipment());

        //remove interim stop
        log.debug("Interim stop of shipment " + event.getShipment().getId() + " has finished");
        session.setShipmentProperty(createInterimStopKey(), null);

        return true;
    }

    public String getName() {
        return NAME;
    }
}
