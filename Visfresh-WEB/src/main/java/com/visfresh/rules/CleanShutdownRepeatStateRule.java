/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CleanShutdownRepeatStateRule implements TrackerEventRule {
    public static final String NAME = "CleanShutdownRepeatState";

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public CleanShutdownRepeatStateRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final Shipment s = context.getEvent().getShipment();
        return s != null && s.getDeviceShutdownTime() == null
                && NoInitMessageAfterShutdownRule.getShutDownRepeatTime(context.getDeviceState()) != null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        NoInitMessageAfterShutdownRule.setShutDownRepeatTime(context.getDeviceState(), null);
        return false;
    }
}
