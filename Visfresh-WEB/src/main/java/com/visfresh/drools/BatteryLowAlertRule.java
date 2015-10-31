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
    public boolean accept(final TrackerEventRequest e) {
        return e.getEvent().getBattery() < LOW_BATTERY_LIMIT && super.accept(e)
                && e.getEvent().getShipment().getAlertProfile().isWatchBatteryLow();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert handleInternal(final TrackerEvent event) {
        final Alert alert = new Alert();
        defaultAssign(event, alert);
        alert.setDescription("Battery is low: " + event.getBattery() + " for " + event.getDevice().getId());
        alert.setName("BatteryLow");
        alert.setType(AlertType.Battery);
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
