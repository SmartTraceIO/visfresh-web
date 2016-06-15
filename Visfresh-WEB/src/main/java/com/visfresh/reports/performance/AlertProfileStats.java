/**
 *
 */
package com.visfresh.reports.performance;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileStats {
    private String name;
    private long totalMonitoringTime;
    private double avgTemperature;
    private double standardDeviation;

    private List<TemperatureRuleStats> temperatureRules = new LinkedList<>();
    /**
     * Default constructor.
     */
    public AlertProfileStats() {
        super();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the totalMonitoringTime
     */
    public long getTotalMonitoringTime() {
        return totalMonitoringTime;
    }
    /**
     * @param totalMonitoringTime the totalMonitoringTime to set
     */
    public void setTotalMonitoringTime(final long totalMonitoringTime) {
        this.totalMonitoringTime = totalMonitoringTime;
    }
    /**
     * @return the avgTemperature
     */
    public double getAvgTemperature() {
        return avgTemperature;
    }
    /**
     * @param avgTemperature the avgTemperature to set
     */
    public void setAvgTemperature(final double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }
    /**
     * @return the standardDeviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }
    /**
     * @param standardDeviation the standardDeviation to set
     */
    public void setStandardDeviation(final double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }
    /**
     * @return the temperatureRules
     */
    public List<TemperatureRuleStats> getTemperatureRules() {
        return temperatureRules;
    }
    /**
     * @param temperatureRules the temperatureRules to set
     */
    public void setTemperatureRules(final List<TemperatureRuleStats> temperatureRules) {
        this.temperatureRules = temperatureRules;
    }
}
