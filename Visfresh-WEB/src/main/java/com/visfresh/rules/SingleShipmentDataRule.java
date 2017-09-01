/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.services.SingleShipmentService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentDataRule implements TrackerEventRule {
    /**
     * Rule name.
     */
    public static final String NAME = "SingleShipmentData";
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private SingleShipmentService service;
    /**
     * Default constructor.
     */
    public SingleShipmentDataRule() {
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
        return (context.getEvent().getShipment() != null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final Shipment s = context.getEvent().getShipment();
        service.rebuildShipmentData(s.getId());
        return false;
    }
}
