/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.Location;
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
    private Location shippedFromLocation;
    private String shippedTo;
    private Location shippedToLocation;
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

    //temperature history
    private String alertProfile;
    private final List<AlertRule> firedAlertRules = new LinkedList<>();
    private final List<Alert> alerts = new LinkedList<>();
    private final List<String> whoWasNotifiedByAlert = new LinkedList<>();
    private final List<String> whoWasNotifiedByArrival = new LinkedList<>();
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
    private Double minimumTemperature;
    private Double maximumTemperature;
    private double lowerTemperatureLimit = 0;
    private double upperTemperatureLimit = 5;
    private long timeBelowLowerLimit;
    private long timeAboveUpperLimit;
    private Color deviceColor = Color.GREEN.darker();
    private int alertSuppressionMinutes;
    private List<String> possibleShippedTo;
    private Date shutdownTime;

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
    public List<AlertRule> getFiredAlertRules() {
        return firedAlertRules;
    }
    /**
     * @return the alerts
     */
    public List<Alert> getAlerts() {
        return alerts;
    }
    /**
     * @return the whoWasNotified
     */
    public List<String> getWhoWasNotifiedByAlert() {
        return whoWasNotifiedByAlert;
    }
    /**
     * @return the whoWasNotified
     */
    public List<String> getWhoWasNotifiedByArrival() {
        return whoWasNotifiedByArrival;
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
     * @return minimum temperature.
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
     * @return minimum temperature.
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
    /**
     * @return lower temperature limit.
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
     * @return upper temperature limit.
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
     * @return time below lower limit.
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
     * @return time above upper limit.
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
     * @return the deviceColor
     */
    public Color getDeviceColor() {
        return deviceColor;
    }
    /**
     * @param deviceColor the deviceColor to set
     */
    public void setDeviceColor(final Color deviceColor) {
        this.deviceColor = deviceColor;
    }
    /**
     * @param alertSuppressionMinutes
     */
    public void setAlertSuppressionMinutes(final int alertSuppressionMinutes) {
        this.alertSuppressionMinutes = alertSuppressionMinutes;
    }
    /**
     * @return the alertSuppressionMinutes
     */
    public int getAlertSuppressionMinutes() {
        return alertSuppressionMinutes;
    }
    /**
     * @param locNames
     */
    public void setPossibleShippedTo(final List<String> locNames) {
        possibleShippedTo = locNames;
    }
    /**
     * @return the possibleShippedTo
     */
    public List<String> getPossibleShippedTo() {
        return possibleShippedTo;
    }
    /**
     * @return
     */
    public Location getShippedFromLocation() {
        return shippedFromLocation;
    }
    /**
     * @param loc the shippedFromLocation to set
     */
    public void setShippedFromLocation(final Location loc) {
        this.shippedFromLocation = loc;
    }
    /**
     * @return the shippedToLocation
     */
    public Location getShippedToLocation() {
        return shippedToLocation;
    }
    /**
     * @param shippedToLocation the shippedToLocation to set
     */
    public void setShippedToLocation(final Location shippedToLocation) {
        this.shippedToLocation = shippedToLocation;
    }
    /**
     * @return device shutdown time.
     */
    public Date getShutdownTime() {
        return shutdownTime;
    }
    /**
     * @param shutdownTime the shutdownTime to set
     */
    public void setShutdownTime(final Date shutdownTime) {
        this.shutdownTime = shutdownTime;
    }
}
