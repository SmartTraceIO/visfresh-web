/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.SiblingDetectionService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SiblingDetectionRule implements TrackerEventRule {
    private static final String NAME = "SiblingDetectionRule";
    private static final long DETECTION_PAUSE = 5 * 60 * 1000l;
    @Autowired
    private SiblingDetectionService service;
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * @param env spring environment.
     */
    public SiblingDetectionRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public final void destroy() {
        engine.setRule(NAME, null);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();

        return shipment != null
                && !context.isProcessed(this)
                && !shipment.hasFinalStatus();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);
        final Shipment s = context.getEvent().getShipment();
        if (!s.getDevice().getModel().isUseGateway()) {
            scheduleSiblingDetection(s, new Date(System.currentTimeMillis() + DETECTION_PAUSE));
        } else {
            //is beacon then need to clean all siblings
            s.getSiblings().clear();
            s.setSiblingCount(0);
            clearSiblings(s);
        }
        return false;
    }

    /**
     * @param s shipment to update siblings.
     */
    protected void clearSiblings(final Shipment s) {
        shipmentDao.updateSiblingInfo(s.getId(), new HashSet<>());
    }
    /**
     * @param s
     * @param scheduleDate
     */
    protected void scheduleSiblingDetection(final Shipment s, final Date scheduleDate) {
        service.scheduleSiblingDetection(s, scheduleDate);
    }
}
