/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BatteryLowAlertRule extends AbstractAlertRule {
    public static final int LOW_BATTERY_LIMIT = 3620;
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
    public boolean accept(final RuleContext context) {
        final Shipment shipment = context.getEvent().getShipment();

        final boolean accept = context.getEvent().getBattery() < LOW_BATTERY_LIMIT && super.accept(context)
                && !shipment.hasFinalStatus()
                && shipment.getAlertProfile().isWatchBatteryLow();
        if (accept) {
            final ShipmentSession s = context.getSessionManager().getSession(shipment);
            if (s.isBatteryLowProcessed()) {
                return false;
            }
        }
        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final Alert alert = new Alert();
        defaultAssign(context.getEvent(), alert);
        alert.setType(AlertType.Battery);

        final ShipmentSession s = context.getSessionManager().getSession(context.getEvent().getShipment());
        s.setBatteryLowProcessed(true);

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
