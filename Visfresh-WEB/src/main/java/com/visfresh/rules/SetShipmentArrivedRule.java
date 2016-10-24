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
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.ArrivalService;
import com.visfresh.services.NotificationService;
import com.visfresh.services.ShipmentShutdownService;

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
    private ArrivalService arrivalService;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private NotificationService notificationService;

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

            //if shipment report is not sent, do it now.
            if (!ArrivalRule.isArrivalNotificationSent(context.getSessionManager().getSession(shipment))) {
                sendShipmentReport(shipment);
            }
        }

        return false;
    }

    /**
     * @param shipment
     */
    private void sendShipmentReport(final Shipment shipment) {
        final List<PersonSchedule> schedules = AbstractNotificationRule.getPersonalSchedules(
                shipment.getArrivalNotificationSchedules(), new Date());

        for (final PersonSchedule sched : schedules) {
            final User user = sched.getUser();
            if (sched.isSendEmail()) {
                notificationService.sendShipmentReport(shipment, user);
            } else {
                log.debug("Pesonal scheule for user " + user.getEmail()
                        + " is not configured for email. Shipment Arrived report will not sent");
            }
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
