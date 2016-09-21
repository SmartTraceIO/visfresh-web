/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureStatsCollector {
    private int n;

    private Map<Long, TrackerEvent> lastEvents = new HashMap<>();

    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;
    private double summt2 = 0;
    private double summt = 0;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private long hotTime;
    private long coldTime;

    private TemperatureStats stats = new TemperatureStats();

    /**
     *
     */
    public TemperatureStatsCollector() {
        super();
    }

    public void processEvent(final TrackerEvent e) {
        //check possible should ignore
        final Shipment shipment = e.getShipment();
        if (shipment == null || shipment.getAlertProfile() == null) {
            return;
        }

        //check alert suppressed.
        if (shipment.getAlertSuppressionMinutes() > 0
                && e.getTime().before(new Date(shipment.getShipmentDate().getTime()
                + 60 * 1000l * shipment.getAlertSuppressionMinutes()))) {
            return;
        }

        n++;
        final double t = e.getTemperature();
        min = Math.min(min, t);
        max = Math.max(max, t);
        summt += t;
        summt2 += t * t;

        final long eventTime = e.getTime().getTime();
        startTime = Math.min(startTime, eventTime);
        endTime = Math.max(endTime, eventTime);

        final Long shipmentId = shipment.getId();
        final TrackerEvent last = lastEvents.get(shipmentId);
        if (last != null) {
            if (last.getTemperature() > shipment.getAlertProfile().getUpperTemperatureLimit()) {
                this.hotTime += eventTime - last.getTime().getTime();
            } else if (last.getTemperature() < shipment.getAlertProfile().getLowerTemperatureLimit()) {
                this.coldTime += eventTime - last.getTime().getTime();
            }
        }

        lastEvents.put(shipmentId, e);
    }

    public TemperatureStats applyStatistics() {
        if (n > 0) {
            final double avg = summt / n;
            stats.setAvgTemperature(avg);
            stats.setMaximumTemperature(max);
            stats.setMinimumTemperature(min);

            if (n > 1) {
                stats.setStandardDevitation(Math.sqrt((summt2 - 2 * avg * summt + n * avg * avg) / (n - 1)));
            } else {
                stats.setStandardDevitation(0.);
            }

            stats.setTimeAboveUpperLimit(hotTime);
            stats.setTimeBelowLowerLimit(coldTime);
            stats.setTotalTime(endTime - startTime);
        } else {
            stats.setAvgTemperature(0.);
            stats.setMaximumTemperature(0.);
            stats.setMinimumTemperature(0.);
            stats.setStandardDevitation(0.);
            stats.setTimeAboveUpperLimit(0);
            stats.setTimeBelowLowerLimit(0);
            stats.setTotalTime(0);
        }

        return stats;
    }
    /**
     * @return the stats
     */
    public TemperatureStats getStats() {
        return stats;
    }
    /**
     * @param stats the stats to set
     */
    public void setStats(final TemperatureStats stats) {
        this.stats = stats;
    }
    public Set<Long> getDetectedShipments() {
        return new HashSet<>(lastEvents.keySet());
    }
}
