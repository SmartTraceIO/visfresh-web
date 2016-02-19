/**
 *
 */
package com.visfresh.rules;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EnterBrightEnvironmentAlertRule extends AbstractAlertRule {
    public static final String NAME = "EnterBrightEnvironmentAlert";

    /**
     * Default constructor.
     */
    public EnterBrightEnvironmentAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext e) {
        return e.getEvent().getType() == TrackerEventType.BRT && super.accept(e)
                && e.getEvent().getShipment().getAlertProfile().isWatchEnterBrightEnvironment();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final Alert alert = new Alert();
        defaultAssign(context.getEvent(), alert);
        alert.setType(AlertType.LightOn);
        return new Alert[] {alert};
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractNotificationRule#sendNotification(com.visfresh.entities.PersonSchedule, com.visfresh.entities.NotificationIssue)
     */
    @Override
    protected void sendNotification(final PersonSchedule s, final NotificationIssue issue, TrackerEvent trackerEvent) {
        // disable notifications
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
}
