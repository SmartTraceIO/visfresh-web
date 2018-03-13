/**
 *
 */
package com.visfresh.reports;

/**
 * @author  Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 */
public class TemperatureStats {
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
     *
     */
    private Double minimumTemperature;
    /**
     *
     */
    private Double maximumTemperature;
    /**
     *
     */
    private double lowerTemperatureLimit = 0;
    /**
     *
     */
    private double upperTemperatureLimit = 5;
    /**
     *
     */
    private long timeBelowLowerLimit;
    /**
     *
     */
    private long timeAboveUpperLimit;

    /**
     * Default constructor.
     */
    public TemperatureStats() {
        super();
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
     * @return the lowerTemperatureLimit
     */
    public double getLowerTemperatureLimit() {
        return lowerTemperatureLimit;
    }

    /**
     * @param lowerTemperatureLimit the lowerTemperatureLimit to set
     */
    public void setLowerTemperatureLimit(final double lowerTemperatureLimit) {
        this.lowerTemperatureLimit = lowerTemperatureLimit;
    }

    /**
     * @return the upperTemperatureLimit
     */
    public double getUpperTemperatureLimit() {
        return upperTemperatureLimit;
    }

    /**
     * @param upperTemperatureLimit the upperTemperatureLimit to set
     */
    public void setUpperTemperatureLimit(final double upperTemperatureLimit) {
        this.upperTemperatureLimit = upperTemperatureLimit;
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
}