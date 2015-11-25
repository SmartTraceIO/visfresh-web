/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BatteryLowAlertRule extends AbstractAlertRule {
    private static final int LOW_BATTERY_LIMIT = 2;
    public static final String NAME = "BatteryLowAlert";

    /**
     * Default constructor.
     */
    public BatteryLowAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext e) {
        return e.getEvent().getBattery() < LOW_BATTERY_LIMIT && super.accept(e)
                && e.getEvent().getShipment().getAlertProfile().isWatchBatteryLow();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final Alert alert = new Alert();
        defaultAssign(context.getEvent(), alert);
        alert.setType(AlertType.Battery);
        return new Alert[]{alert};
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
}
