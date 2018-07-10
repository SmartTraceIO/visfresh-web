/**
 *
 */
package com.visfresh.dao.impl;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemperatureStatsCollector extends AbstractTemperatureStatsCollector {
    protected TimeRanges timeRanges = new TimeRanges();
    protected TrackerEvent lastEvent;

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
    protected TrackerEvent getPreviousEventFor(final TrackerEvent e) {
        return this.lastEvent;
    }
    /**
     * @param e
     * @param eventTime
     */
    @Override
    protected void updateTotalTime(final TrackerEvent e) {
        timeRanges.addTime(getTime(e));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractTemperatureStatsCollector#getTotalTime()
     */
    @Override
    protected long getTotalTime() {
        return timeRanges.getTotalTime();
    }
    /**
     * @return the lastEvent
     */
    public TrackerEvent getLastEvent() {
        return lastEvent;
    }
    /**
     * @return the timeRanges
     */
    public TimeRanges getTimeRanges() {
        return timeRanges;
    }
}
