/**
 *
 */
package com.visfresh.drools;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.EmailService;
import com.visfresh.services.SmsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractNotificationRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(AbstractNotificationRule.class);

    @Autowired
    protected SmsService smsService;
    @Autowired
    protected EmailService emailService;
    @Autowired
    private DroolsRuleEngine engine;

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
    public boolean accept(final TrackerEventRequest e) {
        final TrackerEvent event = e.getEvent();
        final Shipment shipment = event.getShipment();

        final boolean accept = e.getClientProperty(this) == null
                && shipment != null
                && shipment.getStatus() != ShipmentStatus.Complete;

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
     * @param s personal schedule.
     * @param subject message subject.
     * @param message message body.
     */
    protected void sendNotification(final PersonSchedule s, final String subject,
            final String message) {
        final String email = s.getEmailNotification();
        final String person = getPersonDescription(s);

        boolean sent = false;
        //send email
        if (email != null && email.length() > 0) {
            try {
                emailService.sendMessage(email, subject, message);
                sent = true;
            } catch (final MessagingException e) {
                log.error("Failed to send email message to " + email, e);
            }
        } else {
            log.warn("Email has not set for personal schedule for " + person + " , email can't be send");
        }

        //send SMS
        final String phone = s.getSmsNotification();
        if (phone != null && phone.length() > 0) {
            sent = true;
            smsService.sendMessage(phone, subject, message);
        } else {
            log.warn("Phone number has not set for personal schedule for " + person + " , SMS can't be send");
        }

        if (!sent) {
            log.error("Invalid phone and SMS (" + phone
                    + "/" + email + ") configuratio for " + person + ". Notification was not send");
        }

        //TODO send application scope notification.
    }

    /**
     * @param s personal schedule.
     * @return person description.
     */
    private String getPersonDescription(final PersonSchedule s) {
        return s.getFirstName() + " " + s.getLastName() + ", "+ s.getPosition() + " of " + s.getCompany();
    }

    /**
     * @param shipment shipment.
     * @return
     */
    protected abstract List<PersonSchedule> getAllPersonalSchedules(final Shipment shipment);

    public abstract String getName();
}
