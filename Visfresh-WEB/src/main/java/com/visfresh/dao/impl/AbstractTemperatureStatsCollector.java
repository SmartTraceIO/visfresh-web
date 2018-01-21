/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.Date;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractTemperatureStatsCollector {
    protected int n;

    protected double summt2 = 0;
    protected double summt = 0;
    protected double min = Double.MAX_VALUE;
    protected double max = Double.MIN_VALUE;
    protected long hotTime;
    protected long coldTime;

    /**
     */
    public AbstractTemperatureStatsCollector() {
        super();
    }

    public void processEvent(final TrackerEvent e, final double lowerTemperatureLimit, final double upperTemperatureLimit) {
        if (filter(e)) {
            return;
        }

        n++;
        final double t = e.getTemperature();
        min = Math.min(min, t);
        max = Math.max(max, t);
        summt += t;
        summt2 += t * t;

        final long eventTime = e.getTime().getTime();

        final TimeRanges tr = getTimeRanges(e);
        tr.addTime(eventTime);

        final TrackerEvent last = getPreviousEvent(e);
        if (last != null) {
            if (last.getTemperature() > upperTemperatureLimit) {
                this.hotTime += eventTime - last.getTime().getTime();
            } else if (last.getTemperature() < lowerTemperatureLimit) {
                this.coldTime += eventTime - last.getTime().getTime();
            }
        }

        saveAsLastEvent(e);
    }
    /**
     * @param e
     * @return
     */
    protected boolean filter(final TrackerEvent e) {
        //check possible should ignore
        final Shipment shipment = e.getShipment();
        if (shipment != null && shipment.getAlertProfile() != null) {
            //check alert suppressed.
            if (shipment.getAlertSuppressionMinutes() > 0
                    && e.getTime().before(new Date(shipment.getShipmentDate().getTime()
                    + 60 * 1000l * shipment.getAlertSuppressionMinutes()))) {
                return true;
            }

            if (shipment.getArrivalDate() != null && e.getTime().after(shipment.getArrivalDate())) {
                return true;
            }
        }

        return false;
    }

    public TemperatureStats getStatistics() {
        final TemperatureStats stats = new TemperatureStats();

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

            long totalTime = 0;
            for (final TimeRanges tr : getCollectedTimeRanges()) {
                totalTime += tr.getTotalTime();
            }
            stats.setTotalTime(totalTime);
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
     * @return all collected time ranges.
     */
    protected abstract Collection<TimeRanges> getCollectedTimeRanges();
    /**
     * @param e tracer event.
     */
    protected abstract void saveAsLastEvent(final TrackerEvent e);
    /**
     * @param e tacker event.
     * @return previous event for given tracker event.
     */
    protected abstract TrackerEvent getPreviousEvent(final TrackerEvent e);
    /**
     * @param e tracker event.
     * @return time ranges.
     */
    protected abstract TimeRanges getTimeRanges(final TrackerEvent e);

    /**
     * @return the n
     */
    public int getN() {
        return n;
    }
    /**
     * @return the summt2
     */
    public double getSummt2() {
        return summt2;
    }
    /**
     * @return the summt
     */
    public double getSummt() {
        return summt;
    }
    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }
    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }
    /**
     * @return the hotTime
     */
    public long getHotTime() {
        return hotTime;
    }
    /**
     * @return the coldTime
     */
    public long getColdTime() {
        return coldTime;
    }
}
