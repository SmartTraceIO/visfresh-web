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

import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentStatisticsRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(ShipmentStatisticsRule.class);
    private static final String NAME = "ShipmentStatisticsRule";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentStatisticsDao dao;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();

        //check possible should ignore
        final Shipment shipment = e.getShipment();
        if (context.isProcessed(this) || shipment == null
                || shipment.getAlertProfile() == null || shipment.hasFinalStatus()) {
            return false;
        }

        //check alert suppressed.
        if (shipment.getAlertSuppressionMinutes() > 0
                && e.getTime().before(new Date(shipment.getShipmentDate().getTime()
                + 60 * 1000l * shipment.getAlertSuppressionMinutes()))) {
            return false;
        }

        if (shipment.getArrivalDate() != null && e.getTime().after(shipment.getArrivalDate())) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();

        log.debug("Update statistics for shipment: " + s);

        //process event
        final ShipmentStatistics stats = getStatistics(s);
        stats.getCollector().processEvent(e);
        stats.synchronizeWithCollector();

        saveStatistics(stats);
        return false;
    }

    /**
     * @param stats shipment statistics.
     */
    protected void saveStatistics(final ShipmentStatistics stats) {
        dao.saveStatistics(stats);
    }
    /**
     * @param s shipment.
     * @return shipment statistics.
     */
    protected ShipmentStatistics getStatistics(final Shipment s) {
        return dao.getStatistics(s);
    }
}
