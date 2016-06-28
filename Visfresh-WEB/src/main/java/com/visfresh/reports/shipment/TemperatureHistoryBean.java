/**
 *
 */
package com.visfresh.reports.shipment;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureHistoryBean {
    private String alertProfile;
    private final List<String> alertsFired = new LinkedList<>();
    private final List<String> whoWasNotified = new LinkedList<>();
    private final List<String> schedules = new LinkedList<>();
    /**
     * Total time of monitoring: 234hrs
     */
    private long totalTime;
    /**
     * Average temperature: 4.5°C
     */
    private double avgTemperature;
    /**
     * Standard deviation: 0.4
     */
    private double standardDevitation;
    private final List<TimeWithLabel> alerts = new LinkedList<>();

    /**
     * Default constructor.
     */
    public TemperatureHistoryBean() {
        super();
    }

    /**
     * @return the alertProfile
     */
    public String getAlertProfile() {
        return alertProfile;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final String alertProfile) {
        this.alertProfile = alertProfile;
    }
    /**
     * @return the alertsFired
     */
    public List<String> getAlertsFired() {
        return alertsFired;
    }
    /**
     * @return the whoWasNotified
     */
    public List<String> getWhoWasNotified() {
        return whoWasNotified;
    }
    /**
     * @return the schedules
     */
    public List<String> getSchedules() {
        return schedules;
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
     * @return the standardDevitation
     */
    public double getStandardDevitation() {
        return standardDevitation;
    }
    /**
     * @param standardDevitation the standardDevitation to set
     */
    public void setStandardDevitation(final double standardDevitation) {
        this.standardDevitation = standardDevitation;
    }
    /**
     * @return the alerts
     */
    public List<TimeWithLabel> getAlerts() {
        return alerts;
    }
}
