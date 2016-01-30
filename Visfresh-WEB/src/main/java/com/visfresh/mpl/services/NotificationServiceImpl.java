/**
 *
 */
package com.visfresh.mpl.services;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.User;
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
    protected AlertDescriptionBuilder descriptionBuilder;

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
    public void sendNotification(final PersonSchedule s, final NotificationIssue issue) {
        if (issue instanceof Alert) {
            sendAlertNotification(s, (Alert) issue);
        } else if (issue instanceof Arrival) {
            sendArrivalNotification(s, (Arrival) issue);
        }
    }

    /**
     * @param s person schedule.
     * @param arrival arrival.
     */
    private void sendArrivalNotification(final PersonSchedule s, final Arrival arrival) {
        sendNotification(s, "Arrival Notification",
                descriptionBuilder.buildDescription(arrival, s.getUser()), arrival);
    }
    /**
     * @param s person schedule.
     * @param alert alert.
     */
    private void sendAlertNotification(final PersonSchedule s, final Alert alert) {
        sendNotification(s, alert.getType().toString(),
                descriptionBuilder.buildDescription(alert, s.getUser()), alert);
    }

    /**
     * @param s personal schedule.
     * @param subject message subject.
     * @param message message body.
     */
    protected void sendNotification(final PersonSchedule s, final String subject,
            final String message, final NotificationIssue issue) {
        final User u = s.getUser();
        final String email = u.getEmail();
        final String person = getPersonDescription(s);

        //send email
        if (s.isSendEmail()) {
            if (email != null && email.length() > 0) {
                try {
                    emailService.sendMessage(new String[] {email}, subject, message);
                } catch (final MessagingException e) {
                    log.error("Failed to send email message to " + email, e);
                }
            } else {
                log.warn("Email has not set for personal schedule for " + person + " , email can't be send");
            }
        }

        if (s.isSendSms()) {
            //send SMS
            final String phone = u.getPhone();
            if (phone != null && phone.length() > 0) {
                smsService.sendMessage(new String[] {phone}, subject, message);
            } else {
                log.warn("Phone number has not set for personal schedule for " + person + " , SMS can't be send");
            }
        }

        if (s.isSendApp()) {
            final Notification n = new Notification();
            n.setIssue(issue);
            n.setType(issue instanceof Arrival? NotificationType.Arrival : NotificationType.Alert);
            n.setUser(u);

            notificationDao.save(n);
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
