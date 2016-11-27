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
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.EmailService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SmsService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationServiceImpl implements NotificationService, SystemMessageHandler {
    /**
     *
     */
    private static final String USER = "user";
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String SHIPMENT = "shipment";

    @Autowired
    private SmsService smsService;
    @Autowired
    protected EmailService emailService;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    protected NotificationBundle bundle;
    @Autowired
    private MainSystemMessageDispatcher dispatcher;
    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private ShipmentReportDao shipmentReportDao;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;

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
        if (user != null) {
            if (issue instanceof Arrival) {
                sendShipmentReport(issue.getShipment(), user);
            } else {
                try {
                    final String subject = bundle.getEmailSubject(issue, trackerEvent, lang, tz, tu);
                    final String message = bundle.getEmailMessage((Alert) issue, trackerEvent, lang, tz, tu);
                    emailService.sendMessage(new String[] {user.getEmail()}, subject, message);
                } catch (final MessagingException e) {
                    log.error("Failed to send email message to " + user, e);
                }
            }
        } else {
            log.warn("Email has not set for personal schedule for " + user + " , email can't be send");
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
    /**
     * @param shipment
     * @param user
     */
    @Override
    public void sendShipmentReport(final Shipment shipment, final User user) {
        final JsonObject json = new JsonObject();
        json.addProperty(SHIPMENT, shipment.getId());
        json.addProperty(USER, user.getId());

        dispatcher.sendSystemMessage(json.toString(), SystemMessageType.ArrivalReport);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();

        final long shipmentId = json.get(SHIPMENT).getAsLong();
        final Shipment s = shipmentDao.findOne(shipmentId);

        final Long userId = json.get(USER).getAsLong();
        final User user = userDao.findOne(userId);

        if (s == null) {
            log.error("Failed to send shipment arrived report for " + shipmentId + ". Shipment not found");
        } else if (user == null) {
            log.error("Failed to send shipment arrived report for " + userId + ". User not found");
        } else {
            final ShipmentSession session = shipmentSessionDao.getSession(s);
            final String key = "arrReport-" + user.getEmail();

            if (session.getShipmentProperty(key) == null) {
                session.setShipmentProperty(key, "true");
                shipmentSessionDao.saveSession(s, session);

                sendShipmentReportImmediately(s, user);
            } else {
                log.debug("Arrival have already sent to " + user.getEmail());
            }
        }
    }
    /**
     * @param s shipment.
     * @param user user.
     */
    private void sendShipmentReportImmediately(final Shipment s, final User user) {
        final Arrival arrival = arrivalDao.getArrival(s);

        final Language lang = user.getLanguage();
        final TimeZone tz = user.getTimeZone();
        final TemperatureUnits tu = user.getTemperatureUnits();

        final String subject;
        final String message;

        final List<TemperatureAlert> alertsFired = getTemperatureAlerts(s);

        if (arrival != null) {// shipment really arrived.
            TrackerEvent trackerEvent = null;
            if (arrival.getTrackerEventId() != null) {
                trackerEvent = trackerEventDao.findOne(arrival.getTrackerEventId());
            }

            subject = bundle.getEmailSubject(arrival, trackerEvent, lang, tz, tu);
            message = bundle.getEmailMessage(arrival, trackerEvent, alertsFired, lang, tz, tu);
        } else {
            subject = bundle.getArrivalReportEmailSubject(s, lang, tz, tu);
            message = bundle.getArrivalReportEmailMessage(s, alertsFired, lang, tz, tu);
        }

        final File attachment = createShipmenentReport(user, s);

        log.debug("Sending shipment arrived report for " + user.getEmail());
        try {
            emailService.sendMessage(new String[]{user.getEmail()}, subject, message, attachment);
        } catch (final Exception e) {
            log.error("Faile to send email with shipment reports", e);
        } finally {
            attachment.delete();
        }
    }
    /**
     * @param s
     * @return
     */
    private List<TemperatureAlert> getTemperatureAlerts(final Shipment s) {
        final List<Alert> alerts = alertDao.getAlerts(s);

        final List<TemperatureAlert> result = new LinkedList<>();
        for (final Alert a : alerts) {
            if (a instanceof TemperatureAlert) {
                result.add((TemperatureAlert) a);
            }
        }

        return result;
    }

    /**
     * @param user user.
     * @param shipment shipment.
     * @return report file.
     */
    private File createShipmenentReport(final User user, final Shipment shipment) {
        final ShipmentReportBean report = shipmentReportDao.createReport(shipment);

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
    @PostConstruct
    public void init() {
        dispatcher.setSystemMessageHandler(SystemMessageType.ArrivalReport, this);
    }
    @PreDestroy
    public void destroy() {
        dispatcher.setSystemMessageHandler(SystemMessageType.ArrivalReport, null);
    }
}
