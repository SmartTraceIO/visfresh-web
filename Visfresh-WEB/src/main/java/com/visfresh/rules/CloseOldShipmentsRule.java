/**
 *
 */
package com.visfresh.rules;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CloseOldShipmentsRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CloseOldShipmentsRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "CloseOldShipments";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AbstractRuleEngine engine;
    /**
     * Default constructor.
     */
    public CloseOldShipmentsRule() {
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
        return context.getEvent().getShipment() != null
                && !context.isProcessed(this)
                && !context.getState().isOldShipmentsClean();
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        log.debug("Old shipments clean rule has started");

        final Shipment shipment = context.getEvent().getShipment();
        final String imei = shipment.getDevice().getImei();

        final List<Shipment> active = findActiveShipments(imei);
        for (final Shipment s : active) {
            if (!s.getId().equals(shipment.getId())) {
                log.debug("Found and close redundand active shipment " + s.getId());
                s.setStatus(ShipmentStatus.Ended);
                saveShipment(s);
            }
        }

        context.setProcessed(this);
        context.getState().setOldShipmentsClean(true);
        return false;
    }

    /**
     * @param s shipment to save.
     */
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param imei device IMEI.
     * @return list of active shipments for given device.
     */
    protected List<Shipment> findActiveShipments(final String imei) {
        return shipmentDao.findActiveShipments(imei);
    }
}
