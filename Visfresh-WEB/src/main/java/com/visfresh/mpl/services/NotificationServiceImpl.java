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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.ShipmentSessionDao;
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
public class NotificationServiceImpl implements NotificationService, SystemMessageHandler {
    /**
     *
     */
    private static final String USER = "user";
    private static final String USERS = "receivers";
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
    private ArrivalReportDispatcher dispatcher;
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
            try {
                final String subject = bundle.getEmailSubject(issue, trackerEvent, lang, tz, tu);
                final String message;
                if (issue instanceof Alert) {
                    message = bundle.getEmailMessage((Alert) issue, trackerEvent, lang, tz, tu);
                } else {
                    final Arrival arrival = (Arrival) issue;
                    final List<TemperatureAlert> alertsFired = getTemperatureAlerts(arrival.getShipment());

                    message = bundle.getEmailMessage(arrival, trackerEvent, alertsFired, lang, tz, tu);
                }

                emailService.sendMessage(new String[] {user.getEmail()}, subject, message);
            } catch (final MessagingException e) {
                log.error("Failed to send email message to " + user, e);
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
     */
    @Override
    public void sendShipmentReport(final Shipment shipment, final List<User> users) {
        if (!users.isEmpty()) {
            final JsonObject json = new JsonObject();
            json.addProperty(SHIPMENT, shipment.getId());

            final JsonArray array = new JsonArray();
            for (final User u : users) {
                array.add(new JsonPrimitive(u.getId()));
            }

            json.add(USERS, array);

            dispatcher.sendSystemMessage(json.toString(), SystemMessageType.ArrivalReport);
        } else {
            log.error("Empty user list for send report to " + shipment.getId());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();

        final long shipmentId = json.get(SHIPMENT).getAsLong();
        final Shipment s = shipmentDao.findOne(shipmentId);

        final List<User> users = new LinkedList<>();
        if (json.has(USER)) { //TODO old schema. Should remove after one day time out
            final Long userId = json.get(USER).getAsLong();
            final User user = userDao.findOne(userId);
            if (user != null) {
                users.add(user);
            } else {
                log.error("User not found: " + userId);
            }
        } else if (json.has(USERS)) {
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
            //create report bean
            final ShipmentReportBean report = shipmentReportDao.createReport(s);

            //in this case the list of report receivers is fully determined
            report.getWhoReceivedReport().clear();
            for (final User u : users) {
                report.getWhoReceivedReport().add(ShipmentReportBuilder.createUserName(u));
            }

            final ShipmentSession session = shipmentSessionDao.getSession(s);

            for (final User user : users) {
                final String key = "arrReport-" + user.getEmail();

                if (session == null || session.getShipmentProperty(key) == null) {
                    session.setShipmentProperty(key, "true");
                    shipmentSessionDao.saveSession(session);

                    try {
                        sendShipmentReportImmediately(s, user, report);
                    } catch (final IOException e) {
                        log.error("Failed to send arrival report for" + shipmentId, e);
                        throw new RetryableException(e);
                    }
                } else {
                    log.debug("Shipment arrived report is already sent to " + user.getEmail());
                }
            }
        }
    }
    /**
     * @param s shipment.
     * @param user user.
     * @param usersReceivedReports users who received reports.
     */
    private void sendShipmentReportImmediately(final Shipment s, final User user,
            final ShipmentReportBean report) throws IOException {
        final Language lang = user.getLanguage();
        final TimeZone tz = user.getTimeZone();
        final TemperatureUnits tu = user.getTemperatureUnits();

        final String subject;
        final String message;

        final List<TemperatureAlert> alertsFired = getTemperatureAlerts(s);

        subject = bundle.getArrivalReportEmailSubject(s, lang, tz, tu);
        message = bundle.getArrivalReportEmailMessage(s, alertsFired, lang, tz, tu);

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
        }

        return f;
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
