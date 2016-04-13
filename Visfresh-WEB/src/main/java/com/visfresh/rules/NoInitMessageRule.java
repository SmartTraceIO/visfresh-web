/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NoInitMessageRule implements TrackerEventRule {
    private static final String DESCRIPTION_PREFIX = "Unusual start - ";
    public static final String NAME = "NoInitMessage";
    private static final Logger log = LoggerFactory.getLogger(NoInitMessageRule.class);

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AutoStartShipmentService autoStartService;
    @Autowired
    private EmailService emailService;

    /**
     * Default constructor.
     */
    public NoInitMessageRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        return e.getType() != TrackerEventType.INIT && e.getShipment() == null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Device device = e.getDevice();

        final Shipment last = findLastShipment(device.getImei());
        if (last != null && last.hasFinalStatus() && last.getDeviceShutdownTime() != null) {
            final Shipment s = autoStartNewShipment(
                    device, e.getLatitude(), e.getLongitude(), e.getTime());
            s.setShipmentDescription(DESCRIPTION_PREFIX + s.getShipmentDescription());
            e.setShipment(s);
            saveShipment(s);

            final String msg = "Not INIT message found for device "
                    + device.getImei()
                    + ". But last shipment " + last.getId()
                    + " was shutdown. New shipment " + s.getId() + " has autostarted";
            log.debug(msg);
            sendMessageToSupport("Unusual start", msg);
            return true;
        }

        return false;
    }
    /**
     * @param subject
     * @param msg
     */
    protected void sendMessageToSupport(final String subject, final String msg) {
        try {
            emailService.sendMessageToSupport(subject, msg);
        } catch (final MessagingException exc) {
            log.error("Failed to send email message to suppoert", exc);
        }
    }

    /**
     * @param s
     */
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param device
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    protected Shipment autoStartNewShipment(final Device device, final double latitude,
            final double longitude, final Date time) {
        return autoStartService.autoStartNewShipment(device, latitude, longitude, time);
    }
    /**
     * @param imei device IMEI.
     * @return shipment.
     */
    protected Shipment findLastShipment(final String imei) {
        return shipmentDao.findLastShipment(imei);
    }
}
