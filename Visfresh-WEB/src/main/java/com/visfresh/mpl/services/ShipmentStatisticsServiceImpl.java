/**
 *
 */
package com.visfresh.mpl.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentStatistics;
import com.visfresh.services.ShipmentStatisticsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentStatisticsServiceImpl implements ShipmentStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(ShipmentStatisticsServiceImpl.class);
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private ShipmentStatisticsDao dao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsServiceImpl() {
        super();
    }

    /**
     * TODO remove after first launching
     */
    @PostConstruct
    public void initialize() {
        new Thread("Statistics calculation thread") {
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                createStatisticsIfNotYetCreated();
            };
        }.start();
    }

    private int createStatisticsIfNotYetCreated() {
        log.debug("Start of create statistics for shipments which not have it");

        //add sorting by company for better using of cache.
        final int limit = 1000;
        int numProcessed = 0;

        int size;
        do {
            final List<Long> shipments = getShipmentsWithoutStatistics(limit);
            log.debug("Fetch next " + shipments.size() + " shipments for calculate statistics");
            for (final Long id : shipments) {
                final Shipment s = shipmentDao.findOne(id);
                createStatisticsForShipment(s);
                numProcessed++;
            }

            size = shipments.size();
        } while (size >= limit);

        return numProcessed;
    }

    /**
     * @param limit
     * @return
     */
    private List<Long> getShipmentsWithoutStatistics(final int limit) {
        final String query = "select s.id as id, stats.shipment as statsId from shipments s"
                + " left outer join shipmentstats stats on stats.shipment = s.id"
                + " where not s.istemplate and stats.shipment is NULL"
                + " order by s.company, s.id limit " + limit;

        final List<Long> result = new LinkedList<>();
        for (final Map<String, Object> row : jdbc.queryForList(query, new HashMap<>())) {
            result.add(((Number) row.get("id")).longValue());
        }
        return result;
    }

    /**
     * @param s the shipment.
     */
    private void createStatisticsForShipment(final Shipment s) {
        final String companyName = s.getCompany() == null ? "company NULL" : s.getCompany().getName();
        log.debug("Calculate statistics for shipment " + s + " of " + companyName);

        final ShipmentStatistics stats = calculate(s);
        dao.saveStatistics(stats);

        log.debug("Statistics for shipment " + s + " has been calculated and saved");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentStatisticsService#calculate(com.visfresh.entities.Shipment)
     */
    @Override
    public ShipmentStatistics calculate(final Shipment s) {
        final ShipmentTemperatureStatsCollector collector = new ShipmentTemperatureStatsCollector();

        final List<TrackerEvent> events = getTrackerEvents(s);
        for (final TrackerEvent e : events) {
            collector.processEvent(e);
        }

        final ShipmentStatistics stats = new ShipmentStatistics(s.getId());
        stats.setCollector(collector);
        stats.synchronizeWithCollector();
        return stats;
    }

    /**
     * @param s shipment.
     * @return list of tracker events for given shipment.
     */
    protected List<TrackerEvent> getTrackerEvents(final Shipment s) {
        return trackerEventDao.getEvents(s);
    }
}
