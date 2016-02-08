/**
 *
 */
package com.visfresh.rules;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.NotificationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractNotificationRule implements TrackerEventRule {
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private NotificationService notificationService;

    /**
     * Default constructor.
     */
    public AbstractNotificationRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(getName(), this);
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext e) {
        final TrackerEvent event = e.getEvent();
        final Shipment shipment = event.getShipment();

        final boolean accept = !e.isProcessed(this)
                && shipment != null
                && shipment.getStatus() != ShipmentStatus.Ended;

        if (accept) {
            final List<PersonSchedule> schedules = getAllPersonalSchedules(shipment);
            for (final PersonSchedule s : schedules) {
                if (matchesTimeFrame(s)) {
                    return true;
                }
            }
        }

        return accept;
    }

    /**
     * @param s personal schedule.
     * @return true if the situation (time and other) matches the schedule.
     */
    private boolean matchesTimeFrame(final PersonSchedule s) {
        final Calendar c = new GregorianCalendar();
        return matchesTimeFrame(s, c);
    }

    /**
     * This method is made as protected for unit test purposes.
     * @param s personal notification schedule.
     * @param c the time to match as calendar object.
     * @return true if matches the schedule time frames, false otherwise.
     */
    protected boolean matchesTimeFrame(final PersonSchedule s, final Calendar c) {
        boolean matches = false;

        //check matches week days.
        final boolean[] weekDays = s.getWeekDays();
        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                matches = weekDays[6];
                break;
            case Calendar.MONDAY:
                matches = weekDays[0];
                break;
            case Calendar.TUESDAY:
                matches = weekDays[1];
                break;
            case Calendar.WEDNESDAY:
                matches = weekDays[2];
                break;
            case Calendar.THURSDAY:
                matches = weekDays[3];
                break;
            case Calendar.FRIDAY:
                matches = weekDays[4];
                break;
            case Calendar.SATURDAY:
                matches = weekDays[5];
                break;

            default:
                //nothing
            break;
        }

        if (matches) {
            final int hour = c.get(Calendar.HOUR_OF_DAY);
            final int min = hour * 60 + c.get(Calendar.MINUTE);

            //check out of time frame of day.
            if (s.getFromTime() > min || s.getToTime() < min) {
                matches = false;
            }
        }

        return matches;
    }

    /**
     * @param shipment shipment.
     * @return
     */
    protected abstract List<PersonSchedule> getAllPersonalSchedules(final Shipment shipment);

    public abstract String getName();

    /**
     * @param s notification schedule.
     * @param issue notification issue.
     */
    protected void sendNotification(final PersonSchedule s, final NotificationIssue issue) {
        notificationService.sendNotification(s, issue);
    }
}
