/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NormalTemperatureRule implements TrackerEventRule {
    public static final String NAME = "NormalTemperature";

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public NormalTemperatureRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment shipment = e.getShipment();
        if (context.isProcessed(this) || shipment == null || shipment.getAlertProfile() == null) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);
        context.getState().getTemperatureAlerts().clear();
        return false;
    }
}
