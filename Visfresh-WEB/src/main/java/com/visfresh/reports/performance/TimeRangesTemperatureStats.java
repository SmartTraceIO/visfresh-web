/**
 *
 */
package com.visfresh.reports.performance;

import com.visfresh.dao.impl.TimeRanges;
import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TimeRangesTemperatureStats {
    private int numShipments;
    private final TimeRanges timeRanges;
    private int numExcludedHours;
    private final ReportsWithAlertStats alertStats = new ReportsWithAlertStats();
    private TemperatureStats temperatureStats = new TemperatureStats();

    /**
     * Default constructor.
     */
    public TimeRangesTemperatureStats(final TimeRanges r) {
        super();
        this.timeRanges = r;
    }
    /**
     * @return the numShipments
     */
    public int getNumShipments() {
        return numShipments;
    }
    /**
     * @param numShipments the numShipments to set
     */
    public void setNumShipments(final int numShipments) {
        this.numShipments = numShipments;
    }
    /**
     * @return the month
     */
    public TimeRanges getTimeRanges() {
        return timeRanges;
    }
    /**
     * @return the numExcludedHours
     */
    public int getNumExcludedHours() {
        return numExcludedHours;
    }
    /**
     * @param numExcludedHours the numExcludedHours to set
     */
    public void setNumExcludedHours(final int numExcludedHours) {
        this.numExcludedHours = numExcludedHours;
    }
    /**
     * @return the alertStats
     */
    public ReportsWithAlertStats getAlertStats() {
        return alertStats;
    }
    /**
     * @return the temperatureStats
     */
    public TemperatureStats getTemperatureStats() {
        return temperatureStats;
    }
    /**
     * @param temperatureStats the temperatureStats to set
     */
    public void setTemperatureStats(final TemperatureStats temperatureStats) {
        this.temperatureStats = temperatureStats;
    }
}
