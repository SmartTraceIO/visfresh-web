/**
 *
 */
package com.visfresh.drools;

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
public class CriticalLowTemperatureAlertRule extends LowTemperatureAlertRule {
    /**
     * Rule name.
     */
    public static final String NAME = "CriticalLowTemperatureAlert";

    /**
     * Default constructor.
     */
    public CriticalLowTemperatureAlertRule() {
        super();
    }

    /**
     * @param req request.
     * @return true if has temperature issue.
     */
    @Override
    protected boolean chekTemperatureIssue(final TrackerEventRequest req) {
        final TrackerEvent e = req.getEvent();
        final AlertProfile profile = e.getShipment().getAlertProfile();
        if (profile.getCriticalLowTemperature() <= e.getTemperature()) {
            final long period = getLowTemperaturePeriod(e, e.getTemperature());
            if (period > profile.getCriticalLowTemperatureForMoreThen()) {
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
        final double criticalTemperature = event.getShipment().getAlertProfile().getCriticalLowTemperature();
        final int minutes = (int) getLowTemperaturePeriod(event, criticalTemperature);

        defaultAssign(event, alert);
        alert.setTemperature(event.getTemperature());
        alert.setType(AlertType.CriticalLowTemperature);
        alert.setDescription("Critical Low Temperature more then " + criticalTemperature + "C during "
                + minutes + " minutes");
        alert.setMinutes(minutes);
        alert.setName("Critical Low Temperature");

        return alert;
    }
}
