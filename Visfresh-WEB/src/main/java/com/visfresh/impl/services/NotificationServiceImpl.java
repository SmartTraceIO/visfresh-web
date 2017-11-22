/**
 *
 */
package com.visfresh.impl.services;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.dao.AlertDao;
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
import com.visfresh.reports.shipment.MapRenderingException;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;
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
public class NotificationServiceImpl implements NotificationService {
    /**
     *
     */
    private static final String ARRIVAL_REPORT_PREFIX = "arrReport-";
    private static final String USERS = "receivers";
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String SHIPMENT = "shipment";
    private static final String ALERT = "alert";
    private static final String EVENT = "event";

    @Autowired
    private SmsService smsService;
    @Autowired
    protected EmailService emailService;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    protected NotificationBundle bundle;
    @Autowired
    private ArrivalReportDispatcher arrivalDispatcher;
    @Autowired
    private AlertEmailDispatcher alertDispatcher;
    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private ShipmentReportDao shipmentReportDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;
    @Autowired
    private TrackerEventDao trackerEventDao;

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
    public void sendNotification(final List<PersonSchedule> schedules, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final List<User> emailedUsers = new LinkedList<>();

        for (final PersonSchedule s : schedules) {
            final User user = s.getUser();

            //send email
            if (s.isSendEmail()) {
                emailedUsers.add(user);
            }

            //send SMS
            if (s.isSendSms()) {
                sendSms(issue, user, trackerEvent);
            }

            final Notification n = new Notification();
            n.setIssue(issue);
            n.setType(issue instanceof Arrival? NotificationType.Arrival : NotificationType.Alert);
            n.setUser(user);
            n.setHidden(s.isSendApp());

            notificationDao.save(n);
        }

        if (emailedUsers.size() > 0) {
            sendEmailNotification(issue, emailedUsers, trackerEvent);
        }
    }

    /**
     * @param issue
     * @param user
     * @param trackerEvent
     */
    private void sendSms(final NotificationIssue issue, final User user, final TrackerEvent trackerEvent) {
        final String message = bundle.getSmsMessage(issue, trackerEvent,
                user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());

        //send SMS
        final String phone = user.getPhone();
        if (phone != null && phone.length() > 0) {
            smsService.sendMessage(new String[] {phone}, null, message);
        } else {
            log.warn("Phone number has not set for personal schedule for "
                    + getPersonDescription(user) + " , SMS can't be send");
        }
    }

    /**
     * @param issue
     * @param user
     * @param trackerEvent
     */
    @Override
    public void sendEmailNotification(final NotificationIssue issue,
            final List<User> users, final TrackerEvent trackerEvent) {
        if (issue instanceof Alert) {
            final JsonObject json = createSendAlertEmailMessage((Alert) issue, users, trackerEvent);
            arrivalDispatcher.sendSystemMessage(json.toString(), SystemMessageType.AlertEmail);
        } else {
            sendEmailArrivalNotification((Arrival) issue, users, trackerEvent);
        }
    }

    protected void sendEmailAlertNotification(final Alert alert,
            final List<User> users, final TrackerEvent trackerEvent) {
        final ShipmentReportBean report = createShipmentReport(alert.getShipment(), users);
        for (final User user : users) {
            if (user != null) {
                try {
                    final TimeZone tz = user.getTimeZone();
                    final Language lang = user.getLanguage();
                    final TemperatureUnits tu = user.getTemperatureUnits();

                    final String subject = bundle.getEmailSubject(alert, trackerEvent, lang, tz, tu);
                    final String message = bundle.getEmailMessage(alert, trackerEvent, lang, tz, tu);

                    final File attachment = createShipmenentReport(user, report);

                    try {
                        emailService.sendMessage(new String[]{user.getEmail()}, subject, message, attachment);
                        log.debug("Emailed alert to user " + user.getEmail() + " with attached shipment report");
                    } catch (final IOException e) {
                        log.error("Failed to send alert message with attachement to user "
                                + user.getEmail(), e);
                        emailService.sendMessage(new String[]{user.getEmail()}, subject, message);
                        log.debug("Emailed alert to user " + user.getEmail() + " withthout attachment");
                    } finally {
                        attachment.delete();
                    }
                } catch (final MessagingException | IOException e) {
                    log.error("Failed to send email message to " + user, e);
                }
            } else {
                log.warn("Email has not set for personal schedule for " + user + " , email can't be send");
            }
        }
    }
    private void sendEmailArrivalNotification(final Arrival arrival,
            final List<User> users, final TrackerEvent trackerEvent) {
        for (final User user : users) {
            if (user != null) {
                try {
                    final TimeZone tz = user.getTimeZone();
                    final Language lang = user.getLanguage();
                    final TemperatureUnits tu = user.getTemperatureUnits();

                    final String subject = bundle.getEmailSubject(arrival, trackerEvent, lang, tz, tu);

                    final List<TemperatureAlert> alertsFired = getTemperatureAlerts(arrival.getShipment());
                    final String message = bundle.getEmailMessage(arrival, trackerEvent, alertsFired, lang, tz, tu);

                    emailService.sendMessage(new String[] {user.getEmail()}, subject, message);
                } catch (final MessagingException e) {
                    log.error("Failed to send email message to " + user, e);
                }
            } else {
                log.warn("Email has not set for personal schedule for " + user + " , email can't be send");
            }
        }
    }
    /**
     * @param s personal schedule.
     * @return person description.
     */
    private String getPersonDescription(final User u) {
        return u.getFirstName() + " " + u.getLastName() + ", "+ u.getPosition() + " of "
                + u.getCompany().getName();
    }
    /**
     * @param shipment
     */
    @Override
    public void sendShipmentReport(final Shipment shipment, final List<User> users) {
        if (!users.isEmpty()) {
            final JsonObject json = createSendShipmentReportMessage(shipment, users);
            arrivalDispatcher.sendSystemMessage(json.toString(), SystemMessageType.ArrivalReport);
        } else {
            log.error("Empty user list for send report to " + shipment.getId());
        }
    }
    /**
     * @param shipment shipment.
     * @param users report recipients.
     * @return system message payload.
     */
    protected JsonObject createSendShipmentReportMessage(final Shipment shipment, final List<User> users) {
        final JsonObject json = new JsonObject();
        json.addProperty(SHIPMENT, shipment.getId());

        final JsonArray array = new JsonArray();
        for (final User u : users) {
            array.add(new JsonPrimitive(u.getId()));
        }

        json.add(USERS, array);
        return json;
    }
    /**
     * @param alert
     * @param users
     * @param trackerEvent
     * @return
     */
    protected JsonObject createSendAlertEmailMessage(final Alert alert, final List<User> users, final TrackerEvent trackerEvent) {
        final JsonObject json = new JsonObject();
        json.addProperty(ALERT, alert.getId());
        json.addProperty(EVENT, trackerEvent.getId());

        final JsonArray array = new JsonArray();
        for (final User u : users) {
            array.add(new JsonPrimitive(u.getId()));
        }

        json.add(USERS, array);
        return json;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    protected void handleArrivalReportSystemMessage(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();

        final long shipmentId = json.get(SHIPMENT).getAsLong();
        final Shipment s = shipmentDao.findOne(shipmentId);

        final List<User> users = new LinkedList<>();

        if (json.has(USERS)) {
            final JsonArray array = json.get(USERS).getAsJsonArray();
            for (final JsonElement el : array) {
                final User receiver = userDao.findOne(el.getAsLong());
                if (receiver != null) {
                    users.add(receiver);
                } else {
                    log.error("User not found: " + el.getAsLong());
                }
            }
        }

        if (s == null) {
            log.error("Failed to send shipment arrived report for " + shipmentId + ". Shipment not found");
        } else if (users.isEmpty()) {
            log.error("Empty user list for send arrived report for " + shipmentId);
        }else {
            ShipmentSession session = shipmentSessionDao.getSession(s);
            if (!isArrivalReportSent(session)) {
                //mark the session about the arrival report sent
                if (session == null) {
                    session = new ShipmentSession();
                    session.setShipmentId(s.getId());
                }
                setArrivalReportSent(session, new Date());
                shipmentSessionDao.saveSession(session);

                final ShipmentReportBean report = createShipmentReport(s, users);

                //send report to users.
                for (final User user : users) {
                    try {
                        sendReportToUser(user, s, report);
                    } catch (final IOException e) {
                        log.error("Failed to send arrival report for" + shipmentId, e);
                        throw new RetryableException(e);
                    }
                }
            } else {
                log.debug("Shipment (" + s.getId() + ") arrived report is already sent");
            }
        }
    }

    /**
     * @param s
     * @param users
     * @return
     */
    private ShipmentReportBean createShipmentReport(final Shipment s, final List<User> users) {
        //create report bean
        final ShipmentReportBean report = shipmentReportDao.createReport(s);

        //in this case the list of report receivers is fully determined
        report.getWhoReceivedReport().clear();
        for (final User u : users) {
            report.getWhoReceivedReport().add(ShipmentReportBuilder.createUserName(u));
        }
        return report;
    }

    /**
     * @param session
     * @param arrivalReportDate
     */
    public static void setArrivalReportSent(final ShipmentSession session, final Date arrivalReportDate) {
        session.setShipmentProperty(ARRIVAL_REPORT_PREFIX + arrivalReportDate, "true");
    }
    @Override
    public boolean isArrivalReportSent(final Shipment shipment) {
        return isArrivalReportSent(shipmentSessionDao.getSession(shipment));
    }
    public static boolean isArrivalReportSent(final ShipmentSession session) {
        if (session != null) {
            for (final String key : session.getShipmentKeys()) {
                if (key.startsWith(ARRIVAL_REPORT_PREFIX)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * @param user user.
     * @param s shipment.
     * @param usersReceivedReports users who received reports.
     */
    private void sendReportToUser(final User user, final Shipment s,
            final ShipmentReportBean report) throws IOException {
        final Language lang = user.getLanguage();
        final TimeZone tz = user.getTimeZone();
        final TemperatureUnits tu = user.getTemperatureUnits();

        final String subject = bundle.getArrivalReportEmailSubject(s, lang, tz, tu);
        final String message = bundle.getArrivalReportEmailMessage(s, getTemperatureAlerts(s), lang, tz, tu);

        final File attachment = createShipmenentReport(user, report);

        try {
            log.debug("Sending shipment arrived report for " + user.getEmail());
            emailService.sendMessage(new String[]{user.getEmail()}, subject, message, attachment);
        } catch (final Exception e) {
            log.error("Failed to send email with shipment report", e);
        } finally {
            attachment.delete();
        }
    }
    /**
     * @param s shipment.
     * @return list of temperature alerts for given shipment.
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
     * @param report report bean.
     * @param usersReceivedReports users who received report.
     * @return report file.
     */
    private File createShipmenentReport(final User user, final ShipmentReportBean report) throws IOException {
        final DateFormat fmt = DateTimeUtils.createDateFormat(
                "yyyyMMdd HH:mm", user.getLanguage(), user.getTimeZone());
        final String companyName = report.getCompanyName().replaceAll("[\\p{Punct}]", "");

        final String name = "-" + companyName + "-"
                + Device.getSerialNumber(report.getDevice())
                + "(" + report.getTripCount() + ")"
                + "-" + fmt.format(new Date()) + ".pdf";

        final File f = File.createTempFile("report-", name);

        try (final OutputStream out = new FileOutputStream(f)) {
            reportBuilder.createShipmentReport(report, user, out);
        } catch (final MapRenderingException exc) {
            //unwrap I/O exception
            if (exc.getCause() instanceof IOException) {
                throw (IOException) exc.getCause();
            }
            //forward exception next
            throw exc;
        }

        return f;
    }
    @PostConstruct
    public void init() {
        arrivalDispatcher.setSystemMessageHandler(SystemMessageType.ArrivalReport, new SystemMessageHandler() {
            /* (non-Javadoc)
             * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
             */
            @Override
            public void handle(final SystemMessage msg) throws RetryableException {
                handleArrivalReportSystemMessage(msg);
            }
        });
        alertDispatcher.setSystemMessageHandler(SystemMessageType.AlertEmail, new SystemMessageHandler() {
            /* (non-Javadoc)
             * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
             */
            @Override
            public void handle(final SystemMessage msg) throws RetryableException {
                handleAlertReportSystemMessage(msg);
            }
        });
    }
    /**
     * @param msg
     */
    protected void handleAlertReportSystemMessage(final SystemMessage msg) {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();

        final Alert alert = alertDao.findOne(json.get(ALERT).getAsLong());
        final TrackerEvent trackerEvent = trackerEventDao.findOne(json.get(EVENT).getAsLong());

        final List<User> users = new LinkedList<>();

        if (json.has(USERS)) {
            final JsonArray array = json.get(USERS).getAsJsonArray();
            for (final JsonElement el : array) {
                final User receiver = userDao.findOne(el.getAsLong());
                if (receiver != null) {
                    users.add(receiver);
                } else {
                    log.error("User not found: " + el.getAsLong());
                }
            }
        }

        if (alert != null && !users.isEmpty()) {
            sendEmailAlertNotification(alert, users, trackerEvent);
        }
    }

    @PreDestroy
    public void destroy() {
        arrivalDispatcher.setSystemMessageHandler(SystemMessageType.ArrivalReport, null);
        alertDispatcher.setSystemMessageHandler(SystemMessageType.AlertEmail, null);
    }
}
