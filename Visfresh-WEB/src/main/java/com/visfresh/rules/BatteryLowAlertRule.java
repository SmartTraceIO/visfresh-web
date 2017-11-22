/**
 *
 */
package com.visfresh.rules;

import java.util.List;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BatteryLowAlertRule extends AbstractAlertRule {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(BatteryLowAlertRule.class);

    public static final int LOW_BATTERY_LIMIT = 3620;
    public static final String NAME = "BatteryLowAlert";
    private static ThreadLocal<Boolean> isNotificationSend = new ThreadLocal<>();
    @Autowired
    private EmailService email;
    @Autowired
    private NotificationBundle bundle;

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
     * @see com.visfresh.rules.AbstractAlertRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        boolean wasNotificationSent;
        boolean result;

        try {
            result = super.handle(context);
            wasNotificationSent = Boolean.TRUE.equals(isNotificationSend.get());
        } finally {
            isNotificationSend.set(null);
        }

        if (!wasNotificationSent) {
            final TrackerEvent event = context.getEvent();
            final Device device = event.getDevice();
            final Company c = device.getCompany();

            final String email = c.getEmail();
            if (email != null) {
                final Alert a = createAlert(event);

                final Language lang = c.getLanguage() == null ? Language.English : c.getLanguage();
                final TimeZone tz = c.getTimeZone() == null ? TimeZone.getDefault() : c.getTimeZone();
                final TemperatureUnits tu = TemperatureUnits.Celsius;

                log.debug("Sending email notification about low battery to default contact "
                        + email + " of company " + c.getId());
                try {
                    this.email.sendMessage(new String[]{email},
                            bundle.getEmailSubject(a, event, lang, tz, tu),
                            bundle.getEmailMessage(a, event, lang, tz, tu));
                } catch (final MessagingException exc) {
                    log.error("Failed to send email notification", exc);
                }
            } else {
                log.debug("Not notification was sent about low battery alert for "
                        + device.getImei() + ". And the default email is not configured for company " + c.getId());
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractNotificationRule#sendNotification(com.visfresh.entities.PersonSchedule, com.visfresh.entities.NotificationIssue, com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void sendNotification(final List<PersonSchedule> s, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        isNotificationSend.set(Boolean.TRUE);
        super.sendNotification(s, issue, trackerEvent);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Alert alert = createAlert(e);

        final ShipmentSession s = context.getSessionManager().getSession(e.getShipment());
        s.setBatteryLowProcessed(true);

        return new Alert[]{alert};
    }
    /**
     * @param e
     * @return
     */
    private Alert createAlert(final TrackerEvent e) {
        final Alert alert = new Alert();
        defaultAssign(e, alert);
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
