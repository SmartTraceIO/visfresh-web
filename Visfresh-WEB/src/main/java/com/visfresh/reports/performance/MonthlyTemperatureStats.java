/**
 *
 */
package com.visfresh.reports.performance;

import java.util.Date;

import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MonthlyTemperatureStats extends TemperatureStats {
    private int numShipments;
    private final Date month;
    private int numExcludedHours;
    /**
     * Default constructor.
     */
    public MonthlyTemperatureStats(final Date month) {
        super();
        this.month = month;
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
    public Date getMonth() {
        return month;
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
}
