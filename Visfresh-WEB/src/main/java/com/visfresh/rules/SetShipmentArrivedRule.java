/**
 *
 */
package com.visfresh.rules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentReportDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.mpl.services.MainSystemMessageDispatcher;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.ArrivalService;
import com.visfresh.services.EmailService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.ShipmentShutdownService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SetShipmentArrivedRule implements TrackerEventRule, SystemMessageHandler {
    /**
     *
     */
    private static final String SHIPMENT = "shipment";

    private static final Logger log = LoggerFactory.getLogger(SetShipmentArrivedRule.class);

    public static final String NAME = "SetShipmentArrived";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    protected ShipmentShutdownService shutdownService;
    @Autowired
    private ArrivalService arrivalService;
    @Autowired
    private MainSystemMessageDispatcher dispatcher;
    @Autowired
    private PdfReportBuilder reportBuilder;
    @Autowired
    private ShipmentReportDao shipmentReportDao;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ArrivalDao arrivalDao;
    @Autowired
    private NotificationBundle bundle;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public SetShipmentArrivedRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final Shipment shipment = event.getShipment();

        final ShipmentSession session = shipment == null
                ? null : req.getSessionManager().getSession(shipment);
        final boolean accept = !req.isProcessed(this)
                && shipment != null
                && event.getLatitude() != null
                && event.getLongitude() != null
                && shipment.getShippedTo() != null
                && !shipment.hasFinalStatus()
                && LeaveStartLocationRule.isSetLeaving(session)
                && arrivalService.isNearLocation(shipment.getShippedTo(),
                        new Location(event.getLatitude(), event.getLongitude()));

        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();

        context.setProcessed(this);

        if (arrivalService.handleNearLocation(
                shipment.getShippedTo(),
                event,
                context.getSessionManager().getSession(shipment))) {

            shipment.setStatus(ShipmentStatus.Arrived);
            shipment.setArrivalDate(event.getTime());
            shipmentDao.save(shipment);
            log.debug("Shipment status for " + shipment.getId()
                    + " has set to "+ ShipmentStatus.Arrived);

            if (shipment.getShutdownDeviceAfterMinutes() != null) {
                final long date = System.currentTimeMillis()
                        + shipment.getShutdownDeviceAfterMinutes() * 60 * 1000l;
                shutdownService.sendShipmentShutdown(shipment, new Date(date));
            }

            sendSendArrivalReportMessage(shipment);
        }

        return false;
    }

    /**
     * @param shipment
     */
    private void sendSendArrivalReportMessage(final Shipment shipment) {
        final JsonObject json = new JsonObject();
        json.addProperty(SHIPMENT, shipment.getId());

        dispatcher.sendSystemMessage(json.toString(), SystemMessageType.ArrivalReport);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();

        final long id = json.get(SHIPMENT).getAsLong();
        final Shipment s = shipmentDao.findOne(id);

        if (s == null) {
            log.error("Failed to send shipment arrived report for " + id + ". Shipment not found");
        } else {
            sendArrivalReport(s);
        }
    }

    /**
     * @param s shipment.
     */
    private void sendArrivalReport(final Shipment s) {
        final List<PersonSchedule> schedules = AbstractNotificationRule.getPersonalSchedules(
                s.getArrivalNotificationSchedules(), new Date());
        if (schedules.size() == 0) {
            return;
        }

        for (final PersonSchedule sched : schedules) {
            if (sched.isSendEmail()) {
                final User user = sched.getUser();
                final Language lang = user.getLanguage();
                final TimeZone tz = user.getTimeZone();
                final TemperatureUnits tu = user.getTemperatureUnits();

                final String subject = bundle.getArrivalReportEmailSubject(s, lang, tz, tu);
                final String message = bundle.getArrivalReportEmailMessage(s, lang, tz, tu);
                final File attachment = createShipmenentReport(user, s);

                try {
                    emailService.sendMessage(new String[]{user.getEmail()}, subject, message, attachment);
                } catch (final Exception e) {
                    log.error("Faile to send email with shipment reports", e);
                } finally {
                    attachment.delete();
                }
            }
        }
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
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public void destroy() {
        engine.setRule(NAME, null);
        dispatcher.setSystemMessageHandler(SystemMessageType.ArrivalReport, null);
    }
}
