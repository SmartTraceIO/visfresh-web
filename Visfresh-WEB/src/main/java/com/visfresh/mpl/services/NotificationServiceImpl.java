/**
 *
 */
package com.visfresh.mpl.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.services.EmailService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.SmsService;
import com.visfresh.utils.DateTimeUtils;

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
    protected EmailService emailService;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    protected NotificationBundle bundle;
    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private ShipmentReportDao shipmentReportDao;

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
            sendEmailNotification(issue, user, trackerEvent, lang, tz, tu);
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

        final Notification n = new Notification();
        n.setIssue(issue);
        n.setType(issue instanceof Arrival? NotificationType.Arrival : NotificationType.Alert);
        n.setUser(user);
        n.setHidden(s.isSendApp());

        notificationDao.save(n);
    }

    /**
     * @param issue
     * @param user
     * @param trackerEvent
     * @param lang
     * @param tz
     * @param tu
     */
    @Override
    public void sendEmailNotification(final NotificationIssue issue,
            final User user, final TrackerEvent trackerEvent,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String subject = bundle.getEmailSubject(issue, trackerEvent, lang, tz, tu);
        final String message = bundle.getEmailMessage(issue, trackerEvent, lang, tz, tu);

        if (user != null) {
            try {
                if (issue instanceof Arrival) {
                    final File attachment = createShipmenentReport(user, (Arrival) issue);

                    try {
                        emailService.sendMessage(new String[]{user.getEmail()}, subject, null, attachment);
                    } catch (final Exception e) {
                        log.error("Faile to send email with shipment reports", e);
                    } finally {
                        attachment.delete();
                    }
                } else {
                    emailService.sendMessage(new String[] {user.getEmail()}, subject, message);
                }
            } catch (final MessagingException e) {
                log.error("Failed to send email message to " + user, e);
            }
        } else {
            log.warn("Email has not set for personal schedule for " + user + " , email can't be send");
        }
    }
    /**
     * @param user
     * @param arrival
     * @return
     */
    private File createShipmenentReport(final User user, final Arrival arrival) {
        final ShipmentReportBean report = shipmentReportDao.createReport(arrival.getShipment());

        final DateFormat fmt = DateTimeUtils.createDateFormat(
                "yyyyMMdd HH:mm", user.getLanguage(), user.getTimeZone());
        final String companyName = report.getCompanyName().replaceAll("[\\p{Punct}]", "");

        final String name = "-" + companyName + "-"
                + Device.getSerialNumber(report.getDevice())
                + "(" + report.getTripCount() + ")"
                + "-" + fmt.format(new Date()) + ".pdf";

        try {
            final File f = File.createTempFile("report-", name);
            final OutputStream out = new FileOutputStream(f);
            try {
                reportBuilder.createShipmentReport(report, user, out);
            } finally {
                out.close();
            }

            return f;
        } catch (final IOException e) {
            log.error("Failed to send shipment report according arrival notification", e);
        }

        return null;
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
