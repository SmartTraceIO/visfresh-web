/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartShipmentRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AutoStartShipmentRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "AutoStartShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public AutoStartShipmentRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext context) {
        if (context.isProcessed(this)) {
            return false;
        }

        //check init message.
        final TrackerEvent e = context.getEvent();
        if(e.getShipment() == null || e.getType() == TrackerEventType.INIT) {
            return true;
        }

        //check shipment is null

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        log.debug("New INIT event occurred");

        final TrackerEvent event = context.getEvent();
        final Device device = event.getDevice();

        if (event.getShipment() != null) {
            log.debug("Close old shipment for device " + device.getImei());
            closeOldShipment(event.getShipment());
        }

        log.debug("Create new shipment for device " + device.getImei());
        final Shipment shipment = startNewShipment(device);
        event.setShipment(shipment);

        context.getState().possibleNewShipment(shipment);
        trackerEventDao.save(event);

        return true;
    }

    /**
     * @param shipment
     */
    private void closeOldShipment(final Shipment shipment) {
        shipment.setStatus(ShipmentStatus.Complete);
        shipmentDao.save(shipment);
    }

    /**
     * @param device
     */
    private Shipment startNewShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setDevice(device);
        s.setShipmentDescription("Created by autostart shipment rule");
        s.setShipmentDate(new Date());

        shipmentDao.save(s);
        return s;
    }
}
