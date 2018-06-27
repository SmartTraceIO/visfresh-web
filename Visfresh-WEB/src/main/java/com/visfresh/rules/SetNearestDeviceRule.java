/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SetNearestDeviceRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(SetNearestDeviceRule.class);

    public static final String NAME = "SetNearestDevice";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public SetNearestDeviceRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        if (req.isProcessed(this) || event.getShipment() == null) {
            return false;
        }
        final Shipment shipment = event.getShipment();
        return event.getGateway() != null
                && !event.getGateway().equals(shipment.getNearestTracker())
                && shipment.getDevice().getModel().isUseGateway();
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent e = context.getEvent();
        final Shipment shipment = e.getShipment();
        final Device gateway = findDevice(e.getGateway());
        if (gateway != null) {
            if (shipment.getCompanyId().equals(gateway.getCompanyId())) {
                shipment.setNearestTracker(gateway.getImei());
                saveNearestTrackerFor(shipment, gateway);
            } else {
                log.warn("Attempted to assign nearest device " + gateway.getImei()
                    + " from different company " + gateway.getCompanyId()
                    + " to shipment " + shipment.getId());
            }
        } else {
            log.error("Gateway device " + e.getGateway() + " not found ");
        }

        return false;
    }

    /**
     * @param device device IMEI.
     * @return
     */
    protected Device findDevice(final String device) {
        return deviceDao.findOne(device);
    }
    /**
     * @param device
     */
    protected void saveNearestTrackerFor(final Shipment device, final Device gateway) {
        shipmentDao.setNearestTracker(device, gateway);
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
