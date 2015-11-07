/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CriticalHighTemperatureAlertRule extends HighTemperatureAlertRule {
    /**
     * Rule name.
     */
    public static final String NAME = "CriticalHighTemperatureAlert";

    /**
     * Default constructor.
     */
    public CriticalHighTemperatureAlertRule() {
        super();
    }

    /**
     * @param req request.
     * @return true if has temperature issue.
     */
    @Override
    protected boolean chekTemperatureIssue(final RuleContext req) {
        final TrackerEvent e = req.getEvent();
        final AlertProfile profile = e.getShipment().getAlertProfile();
        if (profile.getCriticalHighTemperature() <= e.getTemperature()) {
            final long period = getHighTemperaturePeriod(e, profile.getCriticalHighTemperature());
            if (period > profile.getCriticalHighTemperatureForMoreThen()) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert handleInternal(final TrackerEvent event) {
        final TemperatureAlert alert = new TemperatureAlert();
        final double criticalTemperature = event.getShipment().getAlertProfile().getCriticalHighTemperature();
        final int minutes = (int) getHighTemperaturePeriod(event, criticalTemperature);

        defaultAssign(event, alert);
        alert.setTemperature(event.getTemperature());
        alert.setType(AlertType.CriticalHot);
        alert.setMinutes(minutes);

        return alert;
    }
}
