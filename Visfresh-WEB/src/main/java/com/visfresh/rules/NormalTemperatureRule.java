/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.AlertProfile;
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

        //check normal temperature.
        final AlertProfile a = shipment.getAlertProfile();
        final double t = e.getTemperature();

        if (a.getCriticalHighTemperature() != null && a.getCriticalHighTemperature() <= t) {
            return false;
        }
        if (a.getCriticalHighTemperature2() != null && a.getCriticalHighTemperature2() <= t) {
            return false;
        }
        if (a.getHighTemperature() != null && a.getHighTemperature() <= t) {
            return false;
        }
        if (a.getHighTemperature2() != null && a.getHighTemperature2() <= t) {
            return false;
        }
        if (a.getCriticalLowTemperature() != null && a.getCriticalLowTemperature() >= t) {
            return false;
        }
        if (a.getCriticalLowTemperature2() != null && a.getCriticalLowTemperature2() >= t) {
            return false;
        }
        if (a.getLowTemperature() != null && a.getLowTemperature() >= t) {
            return false;
        }
        if (a.getLowTemperature2() != null && a.getLowTemperature2() >= t) {
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
