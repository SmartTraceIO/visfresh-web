/**
 *
 */
package com.visfresh.drools;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.Shipment;
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
    public boolean accept(final TrackerEventRequest e) {
        return super.accept(e) && e.getEvent().getShipment().getAlertProfile() != null;
    }

    protected void defaultAssign(final TrackerEvent e, final Alert a) {
        a.setDate(e.getTime());
        a.setDevice(e.getDevice());
        a.setShipment(e.getShipment());
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final TrackerEventRequest e) {
        final Alert alert = handleInternal(e.getEvent());
        e.putClientProperty(this, Boolean.TRUE);
        saveAlert(alert);

        final Calendar date = new GregorianCalendar();
        //notify subscribers
        final List<PersonalSchedule> schedules = getAllPersonalSchedules(e.getEvent().getShipment());
        for (final PersonalSchedule s : schedules) {
            if (matchesTimeFrame(s, date)) {
                sendNotification(s, alert.getName(), alert.getDescription());
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
     * @param event event.
     * @return alert if created, null otherwise.
     */
    protected abstract Alert handleInternal(TrackerEvent event);

    /**
     * @param shipment shipment.
     * @return
     */
    @Override
    protected List<PersonalSchedule> getAllPersonalSchedules(final Shipment shipment) {
        final List<PersonalSchedule> all = new LinkedList<PersonalSchedule>();

        final List<NotificationSchedule> schedules = shipment.getAlertsNotificationSchedules();
        for (final NotificationSchedule schedule : schedules) {
            final List<PersonalSchedule> personalSchedules = schedule.getSchedules();
            all.addAll(personalSchedules);
        }

        return all;
    }
}