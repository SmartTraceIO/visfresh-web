/**
 *
 */
package com.visfresh.reports.shipment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportBean {
    private String device;
    private String companyName;
    private int tripCount;
    private String shippedFrom;
    private String shippedTo;
    private Date dateShipped;
    private Date dateArrived;
    private ShipmentStatus status;
    private String description;
    private String palletId;
    private String assetNum;
    private String comment;
    private int numberOfSiblings;
    private ArrivalBean arrival;
    private final List<ShortTrackerEvent> readings = new LinkedList<>();
    private boolean suppressFurtherAlerts;

    //temperature history
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
    public ShipmentReportBean() {
        super();
    }
    /**
     * @return the device
     */
    public String getDevice() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDevice(final String device) {
        this.device = device;
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
     * @return the shippedFrom
     */
    public String getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final String shippedFrom) {
        this.shippedFrom = shippedFrom;
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
     * @return the dateArrived
     */
    public Date getDateArrived() {
        return dateArrived;
    }
    /**
     * @param dateArrived the dateArrived to set
     */
    public void setDateArrived(final Date dateArrived) {
        this.dateArrived = dateArrived;
    }
    /**
     * @return the status
     */
    public ShipmentStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final ShipmentStatus status) {
        this.status = status;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the palletId
     */
    public String getPalletId() {
        return palletId;
    }
    /**
     * @param palletId the palletId to set
     */
    public void setPalletId(final String palletId) {
        this.palletId = palletId;
    }
    /**
     * @return the assetNum
     */
    public String getAssetNum() {
        return assetNum;
    }
    /**
     * @param assetNum the assetNum to set
     */
    public void setAssetNum(final String assetNum) {
        this.assetNum = assetNum;
    }
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comment the comment to set
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }
    /**
     * @return the numberOfSiblings
     */
    public int getNumberOfSiblings() {
        return numberOfSiblings;
    }
    /**
     * @param numberOfSiblings the numberOfSiblings to set
     */
    public void setNumberOfSiblings(final int numberOfSiblings) {
        this.numberOfSiblings = numberOfSiblings;
    }
    /**
     * @return the arrival
     */
    public ArrivalBean getArrival() {
        return arrival;
    }
    /**
     * @param arrival the arrival to set
     */
    public void setArrival(final ArrivalBean arrival) {
        this.arrival = arrival;
    }
    /**
     * @return the readings
     */
    public List<ShortTrackerEvent> getReadings() {
        return readings;
    }
    /**
     * @return
     */
    public boolean isSuppressFurtherAlerts() {
        return suppressFurtherAlerts;
    }
    /**
     * @param suppressFurtherAlerts the suppressFurtherAlerts to set
     */
    public void setSuppressFurtherAlerts(final boolean suppressFurtherAlerts) {
        this.suppressFurtherAlerts = suppressFurtherAlerts;
    }

    //temperature history

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
    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }
    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
}
