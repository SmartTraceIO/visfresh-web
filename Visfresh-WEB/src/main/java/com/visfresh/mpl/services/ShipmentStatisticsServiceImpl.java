/**
 *
 */
package com.visfresh.mpl.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsServiceImpl() {
        super();
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

        final ShipmentStatistics stats = new ShipmentStatistics();
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
