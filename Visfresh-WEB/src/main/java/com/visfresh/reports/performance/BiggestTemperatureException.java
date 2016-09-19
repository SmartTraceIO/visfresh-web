/**
 *
 */
package com.visfresh.reports.performance;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.TemperatureRule;
import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BiggestTemperatureException {
    private String serialNumber;
    private int tripCount;
    private TemperatureStats temperatureStats = new TemperatureStats();
    private Date dateShipped;
    private String shippedTo;
    private final List<TemperatureRule> alertsFired = new LinkedList<>();

    /**
     * Default constructor.
     */
    public BiggestTemperatureException() {
        super();
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }
    /**
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(final String serialNumber) {
        this.serialNumber = serialNumber;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
    /**
     * @return the time
     */
    public TemperatureStats getTemperatureStats() {
        return temperatureStats;
    }
    /**
     * @return the dateShipped
     */
    public Date getDateShipped() {
        return dateShipped;
    }
    /**
     * @param dateShipped the dateShipped to set
     */
    public void setDateShipped(final Date dateShipped) {
        this.dateShipped = dateShipped;
    }
    /**
     * @return the shippedTo
     */
    public String getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final String shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the alertsFired
     */
    public List<TemperatureRule> getAlertsFired() {
        return alertsFired;
    }
    /**
     * @param temperatureStats the temperatureStats to set
     */
    public void setTemperatureStats(final TemperatureStats temperatureStats) {
        this.temperatureStats = temperatureStats;
    }
}
