/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileTemperatureStatsCollector extends AbstractTemperatureStatsCollector {
    private Map<Long, TrackerEvent> lastEvents = new HashMap<>();
    private Map<Long, TimeRanges> timeRanges = new HashMap<>();

    /**
     *
     */
    public AlertProfileTemperatureStatsCollector() {
        super();
    }

    /**
     * @param e tracer event.
     */
    @Override
    protected void saveAsLastEvent(final TrackerEvent e) {
        lastEvents.put(e.getShipment().getId(), e);
    }
    /**
     * @param e tacker event.
     * @return previous event for given tracker event.
     */
    @Override
    protected TrackerEvent getPreviousEvent(final TrackerEvent e) {
        return lastEvents.get(e.getShipment().getId());
    }
    /**
     * @param e tracker event.
     * @return time ranges.
     */
    @Override
    protected TimeRanges getTimeRanges(final TrackerEvent e) {
        final Long shipmentId = e.getShipment().getId();

        TimeRanges tr = timeRanges.get(shipmentId);
        if (tr == null) {
            tr = new TimeRanges();
            timeRanges.put(shipmentId, tr);
        }
        return tr;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AbstractStatsCollector#getCollectedTimeRanges()
     */
    @Override
    protected Collection<TimeRanges> getCollectedTimeRanges() {
        return timeRanges.values();
    }

    public Set<Long> getDetectedShipments() {
        return new HashSet<>(lastEvents.keySet());
    }
}
