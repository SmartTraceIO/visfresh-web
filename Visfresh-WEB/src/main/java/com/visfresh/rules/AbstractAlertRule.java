/**
 *
 */
package com.visfresh.rules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractAlertRule extends AbstractNotificationRule {
    @Autowired
    protected AlertDao alertDao;

    /**
     * Default constructor.
     */
    public AbstractAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final Shipment shipment = context.getEvent().getShipment();
        return shipment != null //please not remove even if is it handled in super.accept
            && shipment.getAlertProfile() != null //please not remove even if is it handled in super.accept
            && super.accept(context)
            && (shouldIgnoreAlertSuppression() || !isSuppressedAllerts(context));
    }
    /**
     * @param context rule context.
     * @return
     */
    protected boolean isSuppressedAllerts(final RuleContext context) {
        final Shipment shipment = context.getEvent().getShipment();
        final Date eventTime = context.getEvent().getTime();

        //check not alerts after arrival
        if (shipment.getStatus() == ShipmentStatus.Arrived && shipment.getNoAlertsAfterArrivalMinutes() != null) {
            final Date arrivalDate = shipment.getArrivalDate();
            if (arrivalDate != null && (eventTime.getTime() - arrivalDate.getTime()
                    > shipment.getNoAlertsAfterArrivalMinutes() * 60 * 1000l)) {
                return true;
            }
        }

        //alert suppression minutes
        if (eventTime.getTime() < shipment.getShipmentDate().getTime()
                + shipment.getAlertSuppressionMinutes() * 60 * 1000l) {
            return true;
        }

        //check not alert after start
        if (shipment.getNoAlertsAfterStartMinutes() != null
                && eventTime.getTime() > shipment.getShipmentDate().getTime()
                + shipment.getNoAlertsAfterStartMinutes() * 60 * 1000l) {
            return true;
        }

        return false;
    }

    protected static void defaultAssign(final TrackerEvent e, final Alert a) {
        a.setDate(e.getTime());
        a.setDevice(e.getDevice());
        a.setShipment(e.getShipment());
        a.setTrackerEventId(e.getId());
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final Alert[] alerts = handleInternal(context);
        context.setProcessed(this);

        for (final Alert alert : alerts) {
            saveAlert(alert);

            final Calendar date = new GregorianCalendar();
            //notify subscribers
            final List<PersonSchedule> schedules = getAllPersonalSchedules(
                    context.getEvent().getShipment());
            for (final PersonSchedule s : schedules) {
                if (matchesTimeFrame(s, date)) {
                    sendNotification(s, alert, context.getEvent());
                }
            }
        }

        return false;
    }

    /**
     * @param a alert to save.
     */
    protected void saveAlert(final Alert a) {
        alertDao.save(a);
    }
    /**
     * @param context rule context.
     * @return alert if created, null otherwise.
     */
    protected abstract Alert[] handleInternal(RuleContext context);

    /**
     * @param shipment shipment.
     * @return
     */
    protected List<PersonSchedule> getAllPersonalSchedules(final Shipment shipment) {
        final List<PersonSchedule> all = new LinkedList<PersonSchedule>();

        final List<NotificationSchedule> schedules = shipment.getAlertsNotificationSchedules();
        for (final NotificationSchedule schedule : schedules) {
            final List<PersonSchedule> personalSchedules = schedule.getSchedules();
            all.addAll(personalSchedules);
        }

        return all;
    }
}
