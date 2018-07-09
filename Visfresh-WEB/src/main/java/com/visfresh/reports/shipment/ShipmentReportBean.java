/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.reports.TemperatureStats;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportBean {
    private String device;
    private DeviceModel deviceModel = DeviceModel.SmartTrace;
    private String companyName;
    private int tripCount;
    private LocationProfileBean shippedFrom;
    private final List<InterimStop> interimStops = new LinkedList<>();
    private LocationProfileBean shippedTo;
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
    private final List<AlertRuleBean> firedAlertRules = new LinkedList<>();
    private final List<AlertBean> alerts = new LinkedList<>();
    private final List<String> whoWasNotifiedByAlert = new LinkedList<>();
    private final List<String> whoReceivedReport = new LinkedList<>();
    private TemperatureStats temperatureStats = new TemperatureStats();
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
    public LocationProfileBean getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final LocationProfileBean shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public LocationProfileBean getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final LocationProfileBean shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the interimStops
     */
    public List<InterimStop> getInterimStops() {
        return interimStops;
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
    public List<AlertRuleBean> getFiredAlertRules() {
        return firedAlertRules;
    }
    /**
     * @return the alerts
     */
    public List<AlertBean> getAlerts() {
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
    public List<String> getWhoReceivedReport() {
        return whoReceivedReport;
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
    /**
     * @return the temperatureStats
     */
    public TemperatureStats getTemperatureStats() {
        return temperatureStats;
    }
    /**
     * @param s the temperatureStats to set
     */
    public void setTemperatureStats(final TemperatureStats s) {
        this.temperatureStats = s;
    }
    /**
     * @return the deviceModel
     */
    public DeviceModel getDeviceModel() {
        return deviceModel;
    }
    /**
     * @param deviceModel the deviceModel to set
     */
    public void setDeviceModel(final DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }
}
