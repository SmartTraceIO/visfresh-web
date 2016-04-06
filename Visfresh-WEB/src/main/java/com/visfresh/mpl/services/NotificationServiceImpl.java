/**
 *
 */
package com.visfresh.mpl.services;

import java.util.TimeZone;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Language;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.services.EmailService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.SmsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private SmsService smsService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    protected NotificationBundle bundle;

    /**
     * Default constructor.
     */
    public NotificationServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.NotificationService#sendNotification(com.visfresh.entities.PersonSchedule, com.visfresh.entities.NotificationIssue)
     */
    @Override
    public void sendNotification(final PersonSchedule s, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final User user = s.getUser();
        final Language lang = user.getLanguage();
        final TimeZone tz = user.getTimeZone();
        final TemperatureUnits tu = user.getTemperatureUnits();

        //send email
        if (s.isSendEmail()) {
            sendEmailNotification(issue, user.getEmail(), trackerEvent, lang, tz, tu);
        }

        final String person = getPersonDescription(s);
        if (s.isSendSms()) {
            final String message = bundle.getSmsMessage(issue, trackerEvent, lang, tz, tu);

            //send SMS
            final String phone = user.getPhone();
            if (phone != null && phone.length() > 0) {
                smsService.sendMessage(new String[] {phone}, null, message);
            } else {
                log.warn("Phone number has not set for personal schedule for " + person + " , SMS can't be send");
            }
        }

        if (s.isSendApp()) {
            final Notification n = new Notification();
            n.setIssue(issue);
            n.setType(issue instanceof Arrival? NotificationType.Arrival : NotificationType.Alert);
            n.setUser(user);

            notificationDao.save(n);
        }
    }

    /**
     * @param issue
     * @param email
     * @param trackerEvent
     * @param lang
     * @param tz
     * @param tu
     */
    @Override
    public void sendEmailNotification(final NotificationIssue issue,
            final String email, final TrackerEvent trackerEvent,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String subject = bundle.getEmailSubject(issue, trackerEvent, lang, tz, tu);
        final String message = bundle.getEmailMessage(issue, trackerEvent, lang, tz, tu);

        if (email != null && email.length() > 0) {
            try {
                emailService.sendMessage(new String[] {email}, subject, message);
            } catch (final MessagingException e) {
                log.error("Failed to send email message to " + email, e);
            }
        } else {
            log.warn("Email has not set for personal schedule for " + email + " , email can't be send");
        }
    }
    /**
     * @param s personal schedule.
     * @return person description.
     */
    private String getPersonDescription(final PersonSchedule s) {
        final User u = s.getUser();
        return u.getFirstName() + " " + u.getLastName() + ", "+ u.getPosition() + " of "
                + u.getCompany().getName();
    }
}
