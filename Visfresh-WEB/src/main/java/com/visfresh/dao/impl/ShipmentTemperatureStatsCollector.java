/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemperatureStatsCollector extends AbstractTemperatureStatsCollector {
    private TimeRanges timeRanges;
    private TrackerEvent lastEvent;

    /**
     * Default constructor.
     */
    public ShipmentTemperatureStatsCollector() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractTemperatureStatsCollector#saveAsLastEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void saveAsLastEvent(final TrackerEvent e) {
        this.lastEvent = e;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractTemperatureStatsCollector#getPreviousEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected TrackerEvent getPreviousEvent(final TrackerEvent e) {
        return this.lastEvent;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractTemperatureStatsCollector#getSavedTimeRanges(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected TimeRanges getTimeRanges(final TrackerEvent e) {
        if (timeRanges == null) {
            timeRanges = new TimeRanges();
        }
        return this.timeRanges;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractTemperatureStatsCollector#getCollectedTimeRanges()
     */
    @Override
    protected Collection<TimeRanges> getCollectedTimeRanges() {
        if (timeRanges == null) {
            return new LinkedList<TimeRanges>();
        }
        return Collections.singletonList(timeRanges);
    }
}
