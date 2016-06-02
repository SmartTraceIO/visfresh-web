/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractNoInitMessageRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(AbstractNoInitMessageRule.class);
    private static final String DESCRIPTION_PREFIX = "Unusual start - ";

    @Autowired
    private AutoStartShipmentService autoStartService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public AbstractNoInitMessageRule() {
        super();
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
     * @param device
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    protected final Shipment autoStartNewShipment(final Device device, final Double latitude,
            final Double longitude, final Date time) {
        final Shipment s = autoStartShipmentByService(device, latitude, longitude, time);
        s.setShipmentDescription(DESCRIPTION_PREFIX + s.getShipmentDescription());
        saveShipment(s);
        return s;
    }

    /**
     * @param device
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    protected Shipment autoStartShipmentByService(final Device device,
            final Double latitude, final Double longitude, final Date time) {
        return autoStartService.autoStartNewShipment(device, latitude, longitude, time);
    }
    /**
     * @param s
     */
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param evt
     */
    protected void saveEvent(final TrackerEvent evt) {
        trackerEventDao.save(evt);
    }
}
