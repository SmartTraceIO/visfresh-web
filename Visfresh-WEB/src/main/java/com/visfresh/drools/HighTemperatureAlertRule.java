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
public class HighTemperatureAlertRule extends AbstractAlertRule {
    /**
     * Rule name.
     */
    public static final String NAME = "HighTemperatureAlert";

    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public HighTemperatureAlertRule() {
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
        if (profile.getHighTemperature() <= e.getTemperature()) {
            final long period = getHighTemperaturePeriod(e, profile.getHighTemperature());
            if (period > profile.getHighTemperatureForMoreThen()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param e last event.
     * @param minimalTemperature minimal critical temperature.
     * @return time frame of given temperature.
     */
    protected long getHighTemperaturePeriod(final TrackerEvent e, final double minimalTemperature) {
        final Date nearestFailure = getFirstHotOccurence(e, minimalTemperature);
        if (nearestFailure != null) {
            return (e.getTime().getTime() - nearestFailure.getTime()) / 60000l;
        }
        return 0;
    }

    /**
     * @param e current tracker event.
     * @param minimalTemperature minimal critical temperature.
     */
    protected Date getFirstHotOccurence(final TrackerEvent e,
            final double minimalTemperature) {
        return trackerEventDao.getFirstHotOccurence(e, minimalTemperature);
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
        final double criticalTemperature = event.getShipment().getAlertProfile().getHighTemperature();
        final int minutes = (int) getHighTemperaturePeriod(event, criticalTemperature);

        defaultAssign(event, alert);
        alert.setTemperature(event.getTemperature());
        alert.setType(AlertType.Hot);
        alert.setMinutes(minutes);

        return alert;
    }
}
