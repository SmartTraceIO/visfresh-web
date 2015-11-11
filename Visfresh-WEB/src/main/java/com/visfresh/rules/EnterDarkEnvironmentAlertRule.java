/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EnterDarkEnvironmentAlertRule extends AbstractAlertRule {
    public static final String NAME = "EnterDarkEnvironmentAlert";

    /**
     * Default constructor.
     */
    public EnterDarkEnvironmentAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext e) {
        return "DRK".equalsIgnoreCase(e.getEvent().getType()) && super.accept(e)
                && e.getEvent().getShipment().getAlertProfile().isWatchEnterDarkEnvironment();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final TrackerEvent event) {
        final Alert alert = new Alert();
        defaultAssign(event, alert);
        alert.setType(AlertType.LightOff);
        return new Alert[] {alert};
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
}
