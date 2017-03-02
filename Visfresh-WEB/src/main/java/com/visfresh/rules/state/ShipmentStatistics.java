/**
 *
 */
package com.visfresh.rules.state;

import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatistics {
    private Long shipmentId;
    /**
     * Total time of monitoring: 234hrs
     */
    private long totalTime;
    /**
     * Average temperature: 4.5°C
     */
    private Double avgTemperature;
    /**
     * Standard deviation: 0.4
     */
    private Double standardDevitation;
    /**
     * minimum temperature.
     */
    private Double minimumTemperature;
    /**
     * maximum temperature
     */
    private Double maximumTemperature;
    /**
     * temperature below lower limit.
     */
    private long timeBelowLowerLimit;
    /**
     * temperature above upper limit.
     */
    private long timeAboveUpperLimit;

    /**
     * statistics collector.
     */
    private ShipmentTemperatureStatsCollector collector = new ShipmentTemperatureStatsCollector();

    /**
     * Default constructor.
     */
    public ShipmentStatistics() {
        this(null);
    }

    /**
     * @param id shipment ID.
     */
    public ShipmentStatistics(final Long id) {
        super();
        this.setShipmentId(id);
    }

    /**
     * @param id the shipment ID.
     */
    public void setShipmentId(final Long id) {
        this.shipmentId = id;
    }
    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @return the totalTime
     */
    public long getTotalTime() {
        return totalTime;
    }
    /**
     * @param totalTime the totalTime to set
     */
    public void setTotalTime(final long totalTime) {
        this.totalTime = totalTime;
    }
    /**
     * @return the avgTemperature
     */
    public Double getAvgTemperature() {
        return avgTemperature;
    }
    /**
     * @param avgTemperature the avgTemperature to set
     */
    public void setAvgTemperature(final Double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }
    /**
     * @return the standardDevitation
     */
    public Double getStandardDevitation() {
        return standardDevitation;
    }
    /**
     * @param standardDevitation the standardDevitation to set
     */
    public void setStandardDevitation(final Double standardDevitation) {
        this.standardDevitation = standardDevitation;
    }
    /**
     * @return the minimumTemperature
     */
    public Double getMinimumTemperature() {
        return minimumTemperature;
    }
    /**
     * @param minimumTemperature the minimumTemperature to set
     */
    public void setMinimumTemperature(final Double minimumTemperature) {
        this.minimumTemperature = minimumTemperature;
    }
    /**
     * @return the maximumTemperature
     */
    public Double getMaximumTemperature() {
        return maximumTemperature;
    }
    /**
     * @param maximumTemperature the maximumTemperature to set
     */
    public void setMaximumTemperature(final Double maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }
    /**
     * @return the timeBelowLowerLimit
     */
    public long getTimeBelowLowerLimit() {
        return timeBelowLowerLimit;
    }
    /**
     * @param timeBelowLowerLimit the timeBelowLowerLimit to set
     */
    public void setTimeBelowLowerLimit(final long timeBelowLowerLimit) {
        this.timeBelowLowerLimit = timeBelowLowerLimit;
    }
    /**
     * @return the timeAboveUpperLimit
     */
    public long getTimeAboveUpperLimit() {
        return timeAboveUpperLimit;
    }
    /**
     * @param timeAboveUpperLimit the timeAboveUpperLimit to set
     */
    public void setTimeAboveUpperLimit(final long timeAboveUpperLimit) {
        this.timeAboveUpperLimit = timeAboveUpperLimit;
    }
    /**
     * @return the collector
     */
    public ShipmentTemperatureStatsCollector getCollector() {
        return collector;
    }
    /**
     * @param collector the collector to set
     */
    public void setCollector(final ShipmentTemperatureStatsCollector collector) {
        this.collector = collector;
    }
}
