/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.NotificationService;
import com.visfresh.services.ShipmentShutdownService;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SetShipmentArrivedRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(SetShipmentArrivedRule.class);

    public static final String NAME = "SetShipmentArrived";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    protected ShipmentShutdownService shutdownService;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private NotificationService notificationService;

    private EnteringDetectionSupport enteringChecker = new EnteringDetectionSupport(
            2, NAME + "_enteringChecker");

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
                && (LeaveStartLocationRule.isSetLeaving(session) || shipment.getShippedFrom() == null)
                && (enteringChecker.isInControl(session)
                        || LocationUtils.isNearLocation(shipment.getShippedTo(),
                                new Location(event.getLatitude(), event.getLongitude())));

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

        log.debug("Process arrival rule for shipment " + shipment.getId());
        final ShipmentSession session = context.getSessionManager().getSession(shipment);

        if (!LocationUtils.isNearLocation(shipment.getShippedTo(),
                new Location(event.getLatitude(), event.getLongitude()))) {
            log.debug("Watching of InArrival state has cleaned for " + shipment.getId());
            enteringChecker.clearInControl(session);
            return false;
        }

        final TrackerEventType type = event.getType();
        boolean isArrival = (AutoDetectEndLocationRule.isAutodetected(context)
                || type == TrackerEventType.STP || type == TrackerEventType.BRT);
        if (!isArrival) {
            isArrival = enteringChecker.handleEntered(session);
        }

        if (isArrival) {
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

            //send shipment report. Given report can be sent before, but
            //the notification service will prevent duplicate sending of report.
            sendShipmentReport(shipment);
        }

        return false;
    }

    /**
     * @param shipment
     */
    private void sendShipmentReport(final Shipment shipment) {
        if (shipment.isSendArrivalReport()) {
            if (shipment.isSendArrivalReportOnlyIfAlerts() && engine.getAlertFired(shipment).isEmpty()) {
                log.debug("Shipment " + shipment.getId() + " is configured to send arrival "
                        + "report only if alerts but has not alerts. Report will not send");
                return;
            }

            final List<User> users = AbstractNotificationRule.getEmailingUsers(
                    shipment.getArrivalNotificationSchedules(), new Date());
            notificationService.sendShipmentReport(shipment, users);
        }
    }

    @PostConstruct
    public void init() {
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public void destroy() {
        engine.setRule(NAME, null);
    }
}
