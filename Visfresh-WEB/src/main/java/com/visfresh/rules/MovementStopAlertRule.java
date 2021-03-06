/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;

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
    public boolean accept(final RuleContext e) {
        //Not handled now. Not fully understandeable how to check the shock.
//        return super.accept(e) && e.getEvent().getShipment().getAlertProfile().isWatchMovementStop()
//                && false;
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final Alert alert = new Alert();
        defaultAssign(context.getEvent(), alert);
//        alert.setType(AlertType.MovementStop);
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
