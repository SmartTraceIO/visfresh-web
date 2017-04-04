/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentStatistics;
import com.visfresh.services.ShipmentStatisticsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsInitRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(ShipmentStatisticsInitRule.class);
    private static final String NAME = "ShipmentStatisticsInitializeRule";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentStatisticsDao dao;
    @Autowired
    private ShipmentStatisticsService service;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsInitRule() {
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
        if (context.isProcessed(this) || e.getShipment() == null) {
            return false;
        }
        return getStatistics(e.getShipment()) == null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();

        log.debug("Initialize statistics for shipment: " + s);

        //process event
        ShipmentStatistics stats = getStatistics(s);
        if (stats == null) {
            log.debug("Statustics for shipment " + s
                    + " was not calculated before, will calculated from "
                    + "start of shipment");
            stats = calculateStatistics(s);
            saveStatistics(stats);
        }

        return false;
    }

    /**
     * @param s
     * @return
     */
    protected ShipmentStatistics calculateStatistics(final Shipment s) {
        return service.calculate(s);
    }
    /**
     * @param stats statistics.
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
