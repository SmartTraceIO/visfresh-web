/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AssignShipmentRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AssignShipmentRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "AssignShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private TrackerEventDao trackerEventDao;
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
    public boolean accept(final RuleContext context) {
        if(context.getEvent().getShipment() == null && !context.isProcessed(this)) {
            final Shipment s = shipmentDao.findLastShipment(context.getEvent().getDevice().getImei());
            if (s != null && s.getStatus() != ShipmentStatus.Ended) {
                //cache shipment to request.
                context.putClientProperty(this, s);
                return true;
            }
        }
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final Shipment shipment = (Shipment) context.getClientProperty(this);

        log.debug("Assign shipment " + shipment.getId() + " to event " + context.getEvent());
        context.getEvent().setShipment(shipment);
        context.getState().possibleNewShipment(shipment);
        trackerEventDao.save(context.getEvent());
        return true;
    }
}
