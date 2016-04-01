/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BatteryRechargedRule implements TrackerEventRule {
    public static final int LOW_RECHARGED_LIMIT = 3500;

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BatteryRechargedRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "BatteryRecharged";

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public BatteryRechargedRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext context) {
        if (context.isProcessed(this)) {
            return false;
        }

        //check init message.
        final TrackerEvent e = context.getEvent();
        if(e.getShipment() == null || e.getBattery() < LOW_RECHARGED_LIMIT) {
            return false;
        }

        final ShipmentSession session = context.getSessionManager().getSession(e.getShipment());
        return session.isBatteryLowProcessed();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        log.debug("Battery recharged");
        final ShipmentSession session = context.getSessionManager().getSession(context.getEvent().getShipment());
        session.setBatteryLowProcessed(false);
        return false;
    }
}
