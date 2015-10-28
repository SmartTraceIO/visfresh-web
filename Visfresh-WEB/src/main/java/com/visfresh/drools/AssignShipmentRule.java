/**
 *
 */
package com.visfresh.drools;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

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
        return e.getEvent().getShipment() == null && e.getClientProperty(this) == null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final TrackerEventRequest req) {
        //mark request as processed.
        req.putClientProperty(this, Boolean.TRUE);

        final TrackerEvent event = req.getEvent();

        final Device device = event.getDevice();
        Shipment shipment = shipmentDao.findActiveShipment(device.getImei());

        if (shipment == null) {
            //create shipment.
            final Shipment s = new Shipment();
            s.setName("Default Shipment");
            s.setCompany(device.getCompany());
            s.setDevice(device);
            shipment = shipmentDao.save(s);
        }

        event.setShipment(shipment);
        return true;
    }
}
