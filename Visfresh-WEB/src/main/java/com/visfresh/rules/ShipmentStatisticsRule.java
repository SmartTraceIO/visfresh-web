/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.rules.state.ShipmentStatistics;
import com.visfresh.services.ShipmentStatisticsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(ShipmentStatisticsRule.class);
    private static final String NAME = "ShipmentStatisticsRule";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentStatisticsDao dao;
    @Autowired
    private ShipmentStatisticsService service;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        //TODO uncomment after implement
//        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();

        //check possible should ignore
        final Shipment shipment = e.getShipment();
        if (shipment == null || shipment.getAlertProfile() == null) {
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
        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();

        log.debug("Update statistics for shipment: " + s);

        //process event
        ShipmentStatistics stats = dao.getStatistics(s);
        if (stats == null) {
            log.debug("Statustics for shipment " + s + " was not calculated before, will recalculated from "
                    + "start of shipment");
            stats = service.calculate(s);
        }

        stats.getCollector().processEvent(e);

        //update statistic
        final TemperatureStats ts = stats.getCollector().getStatistics();
        stats.set(ts);

        dao.saveStatistics(stats);
        return false;
    }
}
