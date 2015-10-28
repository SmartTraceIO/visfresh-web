/**
 *
 */
package com.visfresh.drools;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EnterBrightEnvironmentAlertRule extends AbstractAlertRule {
    public static final String NAME = "EnterBrightEnvironmentAlert";

    /**
     * Default constructor.
     */
    public EnterBrightEnvironmentAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final TrackerEventRequest e) {
        return "BRT".equalsIgnoreCase(e.getEvent().getType()) && super.accept(e)
                && e.getEvent().getShipment().getAlertProfile().isWatchEnterBrightEnvironment();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert handleInternal(final TrackerEvent event) {
        final Alert alert = new Alert();
        defaultAssign(event, alert);
        alert.setDescription("Device " + event.getDevice().getId() + " entered Bright Environment");
        alert.setName("Enter Bright Environment");
        alert.setType(AlertType.EnterBrightEnvironment);
        return alert;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
}
