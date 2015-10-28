/**
 *
 */
package com.visfresh.drools;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
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
public class LowTemperatureAlertRule extends AbstractAlertRule {
    /**
     * Rule name.
     */
    public static final String NAME = "LowTemperatureAlert";

    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public LowTemperatureAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final TrackerEventRequest req) {
        boolean accept = super.accept(req);
        if (accept) {
            accept = chekTemperatureIssue(req);
        }
        return accept;
    }

    /**
     * @param req
     * @param accept
     * @return
     */
    protected boolean chekTemperatureIssue(final TrackerEventRequest req) {
        final TrackerEvent e = req.getEvent();
        final AlertProfile profile = e.getShipment().getAlertProfile();
        if (profile.getLowTemperature() <= e.getTemperature()) {
            final long period = getLowTemperaturePeriod(e, profile.getHighTemperature());
            if (period > profile.getLowTemperatureForMoreThen()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param e last event.
     * @param maximalTemperature maximal critical temperature.
     * @return time frame of given temperature.
     */
    protected long getLowTemperaturePeriod(final TrackerEvent e, final double maximalTemperature) {
        final Date nearestFailure = getFirstColdOccurence(e, maximalTemperature);
        if (nearestFailure != null) {
            return (e.getTime().getTime() - nearestFailure.getTime()) / 60000l;
        }
        return 0;
    }

    /**
     * @param e current tracker event.
     * @param maximalTemperature maximal critical temperature.
     */
    protected Date getFirstColdOccurence(final TrackerEvent e,
            final double maximalTemperature) {
        return trackerEventDao.getFirstColdOccurence(e, maximalTemperature);
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
        final double criticalTemperature = event.getShipment().getAlertProfile().getLowTemperature();
        final int minutes = (int) getLowTemperaturePeriod(event, criticalTemperature);

        defaultAssign(event, alert);
        alert.setTemperature(event.getTemperature());
        alert.setType(AlertType.LowTemperature);
        alert.setDescription("Low Temperature less then " + criticalTemperature + "C during "
                + minutes + " minutes for " + event.getDevice().getId());
        alert.setMinutes(minutes);
        alert.setName("Low Temperature");

        return alert;
    }
}
