/**
 *
 */
package com.visfresh.io.json;

import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;
import com.visfresh.dao.impl.TimeRanges;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
class AccessibleShipmentStatsCollector extends ShipmentTemperatureStatsCollector {
    /**
     * Default constructor.
     */
    public AccessibleShipmentStatsCollector(final ShipmentTemperatureStatsCollector origin) {
        super();

        this.timeRanges = origin.getTimeRanges();
        this.lastEvent = origin.getLastEvent();
        this.n = origin.getN();
        this.summt2 = origin.getSummt2();
        this.summt = origin.getSummt();
        this.min = origin.getMin();
        this.max = origin.getMax();
        this.hotTime = origin.getHotTime();
        this.coldTime = origin.getColdTime();
    }
    /**
     * Default constructor.
     */
    public AccessibleShipmentStatsCollector() {
        super();
    }

    /**
     * @param timeRanges the timeRanges to set
     */
    public void setTimeRanges(final TimeRanges timeRanges) {
        this.timeRanges = timeRanges;
    }
    /**
     * @param lastEvent the lastEvent to set
     */
    public void setLastEvent(final TrackerEvent lastEvent) {
        this.lastEvent = lastEvent;
    }
    /**
     * @param n the n to set
     */
    public void setN(final int n) {
        this.n = n;
    }
    /**
     * @param summt2 the summt2 to set
     */
    public void setSummt2(final double summt2) {
        this.summt2 = summt2;
    }
    /**
     * @param summt the summt to set
     */
    public void setSummt(final double summt) {
        this.summt = summt;
    }
    /**
     * @param min the min to set
     */
    public void setMin(final double min) {
        this.min = min;
    }
    /**
     * @param max the max to set
     */
    public void setMax(final double max) {
        this.max = max;
    }
    /**
     * @param hotTime the hotTime to set
     */
    public void setHotTime(final long hotTime) {
        this.hotTime = hotTime;
    }
    /**
     * @param coldTime the coldTime to set
     */
    public void setColdTime(final long coldTime) {
        this.coldTime = coldTime;
    }
}
