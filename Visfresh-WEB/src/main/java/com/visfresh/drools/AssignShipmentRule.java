/**
 *
 */
package com.visfresh.drools;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AssignShipmentRule implements TrackerEventRule {
    /**
     * Rule name.
     */
    public static final String NAME = "AssignShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DroolsRuleEngine engine;
    /**
     * Default constructor.
     */
    public AssignShipmentRule() {
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
    public boolean accept(final TrackerEventRequest e) {
        if(e.getEvent().getShipment() == null && e.getClientProperty(this) == null) {
            final Shipment s = shipmentDao.findActiveShipment(e.getEvent().getDevice().getImei());
            if (s != null) {
                //cache shipment to request.
                e.putClientProperty(this, s);
                return true;
            }
        }
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final TrackerEventRequest req) {
        final Shipment shipment = (Shipment) req.getClientProperty(this);
        req.getEvent().setShipment(shipment);
        return true;
    }
}
