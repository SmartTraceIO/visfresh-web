/**
 *
 */
package com.visfresh.reports.performance;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PerformanceReportBean {
    private int numberOfShipments;
    private int numberOfTrackers;
    private double avgShipmentsPerTracker;
    private double avgTrackersPerShipment;
    private Date startDate;
    private Date endDate;

    private final List<AlertProfileStats> alertProfiles = new LinkedList<>();

    /**
     * Default constructor.
     */
    public PerformanceReportBean() {
        super();
    }

    /**
     * @return the numberOfShipments
     */
    public int getNumberOfShipments() {
        return numberOfShipments;
    }
    /**
     * @param numberOfShipments the numberOfShipments to set
     */
    public void setNumberOfShipments(final int numberOfShipments) {
        this.numberOfShipments = numberOfShipments;
    }
    /**
     * @return the numberOfTrackers
     */
    public int getNumberOfTrackers() {
        return numberOfTrackers;
    }
    /**
     * @param numberOfTrackers the numberOfTrackers to set
     */
    public void setNumberOfTrackers(final int numberOfTrackers) {
        this.numberOfTrackers = numberOfTrackers;
    }
    /**
     * @return the avgShipmentsPerTracker
     */
    public double getAvgShipmentsPerTracker() {
        return avgShipmentsPerTracker;
    }
    /**
     * @param avgShipmentsPerTracker the avgShipmentsPerTracker to set
     */
    public void setAvgShipmentsPerTracker(final double avgShipmentsPerTracker) {
        this.avgShipmentsPerTracker = avgShipmentsPerTracker;
    }
    /**
     * @return the avgTrackersPerShipment
     */
    public double getAvgTrackersPerShipment() {
        return avgTrackersPerShipment;
    }
    /**
     * @param avgTrackersPerShipment the avgTrackersPerShipment to set
     */
    public void setAvgTrackersPerShipment(final double avgTrackersPerShipment) {
        this.avgTrackersPerShipment = avgTrackersPerShipment;
    }
    /**
     * @return the alertProfiles
     */
    public List<AlertProfileStats> getAlertProfiles() {
        return alertProfiles;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }
    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }
}
