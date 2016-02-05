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
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class StartShipmentTimeCorrectRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(StartShipmentTimeCorrectRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "CorrectStartShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private TrackerEventDao trackerEventDao;
    /**
     * Default constructor.
     */
    public StartShipmentTimeCorrectRule() {
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
        final DeviceState state = context.getState();
        if(state.getShipmentId() != null && state.getStartShipmentDate() == null) {
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final DeviceState state = context.getState();

        final Shipment shipment = shipmentDao.findOne(state.getShipmentId());
        final TrackerEvent firstEvent = trackerEventDao.getFirstEvent(shipment);

        if (firstEvent != null) {
            log.debug("Set up start shipment date for " + shipment.getId() + " to device settings.");
            state.setStartShipmentDate(firstEvent.getTime());
        }

        return false;
    }
}
