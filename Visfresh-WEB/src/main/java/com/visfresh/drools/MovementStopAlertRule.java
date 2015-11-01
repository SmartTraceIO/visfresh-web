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
public class MovementStopAlertRule extends AbstractAlertRule {
    public static final String NAME = "MovementStopAlert";

    /**
     * Default constructor.
     */
    public MovementStopAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final TrackerEventRequest e) {
        //Not handled now. Not fully understandeable how to check the shock.
        return super.accept(e) && e.getEvent().getShipment().getAlertProfile().isWatchMovementStop()
                && false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert handleInternal(final TrackerEvent event) {
        final Alert alert = new Alert();
        defaultAssign(event, alert);
        alert.setDescription("Device " + alert.getDevice().getId() + " movement stoped");
        alert.setName("Movement Stop");
        alert.setType(AlertType.MovementStop);
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