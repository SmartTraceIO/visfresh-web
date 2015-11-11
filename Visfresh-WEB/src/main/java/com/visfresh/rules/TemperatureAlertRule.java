/**
 *
 */
package com.visfresh.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.TemperatureIssue;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TemperatureAlertRule extends AbstractAlertRule {
    /**
     * Rule name.
     */
    public static final String NAME = "TemperatureAlert";

    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public TemperatureAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final boolean accept = super.accept(req);
        if (accept) {
            final double t = req.getEvent().getTemperature();

            final AlertProfile p = req.getEvent().getShipment().getAlertProfile();
            for (final TemperatureIssue issue : p.getTemperatureIssues()) {
                if (isMatches(issue, t)) {
                    return true;
                }
            }
        }
        return accept;
    }

    /**
     * @param issue
     * @param t
     * @return
     */
    private boolean isMatches(final TemperatureIssue issue, final double t) {
        switch (issue.getType()) {
            case Cold:
            case CriticalCold:
                if (t <= issue.getTemperature()) {
                    return true;
                }
                break;
            case Hot:
            case CriticalHot:
                if (t >= issue.getTemperature()) {
                    return true;
                }
                break;
                default:
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
     * @see com.visfresh.rules.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final TrackerEvent event) {
        return new Alert[0];
    }
}
