/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AppUserNotification {
    /**
     * Notification ID.
     */
    private Long id;
    /**
     * Notification type.
     */
    private NotificationType type;
    /**
     * Is notification read flag.
     */
    private boolean isRead;
    /**
     * Whether or not notification should be visible for user.
     */
    private boolean isHidden;
    /**
     * Notification issue ID.
     */
    private Long issueId;
    /**
     * Date of occurrence.
     */
    private Date issueDate;
    /**
     * Device.
     */
    private String device;
    /**
     * Shipment;
     */
    private Long shipmentId;
    /**
     * The trackere event ID.
     */
    private Long trackerEventId;
    /**
     * Alert type in case if the notification is alert notification.
     */
    private AlertType alertType;
    private int shipmentTripCount;
    private String shipmentDescription;
    private Date eventTime;
    private Integer alertMinutes;
    private Integer alertRuleTimeOutMinutes;
    private Double temperature;
    private Double alertRuleTemperature;
    private int numberOfMettersOfArrival;
    private boolean alertCumulative;
    private Double readingTemperature;
    private DeviceModel deviceModel;


    /**
     * Default constructor.
     */
    public AppUserNotification() {
        super();
    }

    /**
     * @return the type
     */
    public NotificationType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final NotificationType type) {
        this.type = type;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the isRead
     */
    public boolean isRead() {
        return isRead;
    }
    /**
     * @param isRead the isRead to set
     */
    public void setRead(final boolean isRead) {
        this.isRead = isRead;
    }
    /**
     * @return the isHidden
     */
    public boolean isHidden() {
        return isHidden;
    }
    /**
     * @param isHidden the isHidden to set
     */
    public void setHidden(final boolean isHidden) {
        this.isHidden = isHidden;
    }
    /**
     * @return the issueId
     */
    public Long getIssueId() {
        return issueId;
    }
    /**
     * @param issueId the issueId to set
     */
    public void setIssueId(final Long issueId) {
        this.issueId = issueId;
    }
    /**
     * @return the issueDate
     */
    public Date getIssueDate() {
        return issueDate;
    }
    /**
     * @param issueDate the issueDate to set
     */
    public void setIssueDate(final Date issueDate) {
        this.issueDate = issueDate;
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
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the trackerEventId
     */
    public Long getTrackerEventId() {
        return trackerEventId;
    }
    /**
     * @param trackerEventId the trackerEventId to set
     */
    public void setTrackerEventId(final Long trackerEventId) {
        this.trackerEventId = trackerEventId;
    }
    /**
     * @return the alertType
     */
    public AlertType getAlertType() {
        return alertType;
    }
    /**
     * @param alertType the alertType to set
     */
    public void setAlertType(final AlertType alertType) {
        this.alertType = alertType;
    }
    /**
     * @return shipment trip count.
     */
    public int getShipmentTripCount() {
        return shipmentTripCount;
    }
    /**
     * @param count the shipmentTripCount to set
     */
    public void setShipmentTripCount(final int count) {
        this.shipmentTripCount = count;
    }
    /**
     * @return shipment description.
     */
    public String getShipmentDescription() {
        return shipmentDescription;
    }
    /**
     * @param shipmentDescription the shipmentDescription to set
     */
    public void setShipmentDescription(final String shipmentDescription) {
        this.shipmentDescription = shipmentDescription;
    }
    /**
     * @return event time.
     */
    public Date getEventTime() {
        return eventTime;
    }
    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(final Date eventTime) {
        this.eventTime = eventTime;
    }
    /**
     * @return time of alert minutes.
     */
    public Integer getAlertMinutes() {
        return alertMinutes;
    }
    /**
     * @param alertMinutes the alertMinutes to set
     */
    public void setAlertMinutes(final Integer alertMinutes) {
        this.alertMinutes = alertMinutes;
    }
    /**
     * @return
     */
    public Integer getAlertRuleTimeOutMinutes() {
        return alertRuleTimeOutMinutes;
    }
    /**
     * @param alertRuleTimeOutMinutes the alertRuleTimeOutMinutes to set
     */
    public void setAlertRuleTimeOutMinutes(final Integer alertRuleTimeOutMinutes) {
        this.alertRuleTimeOutMinutes = alertRuleTimeOutMinutes;
    }
    /**
     * @return temperature.
     */
    public Double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return alert rule temperature.
     */
    public Double getAlertRuleTemperature() {
        return alertRuleTemperature;
    }
    /**
     * @param alertRuleTemperature the alertRuleTemperature to set
     */
    public void setAlertRuleTemperature(final Double alertRuleTemperature) {
        this.alertRuleTemperature = alertRuleTemperature;
    }
    /**
     * @return number of meters for arrival.
     */
    public int getNumberOfMettersOfArrival() {
        return numberOfMettersOfArrival;
    }
    /**
     * @param numberOfMettersOfArrival the numberOfMettersOfArrival to set
     */
    public void setNumberOfMettersOfArrival(final int numberOfMettersOfArrival) {
        this.numberOfMettersOfArrival = numberOfMettersOfArrival;
    }
    /**
     * @return true if alert is cummulative
     */
    public boolean isAlertCumulative() {
        return alertCumulative;
    }
    /**
     * @param alertCumulative the alertCumulative to set
     */
    public void setAlertCumulative(final boolean alertCumulative) {
        this.alertCumulative = alertCumulative;
    }
    /**
     * @return the readingTemperature
     */
    public Double getReadingTemperature() {
        return readingTemperature;
    }
    /**
     * @param readingTemperature the readingTemperature to set
     */
    public void setReadingTemperature(final Double readingTemperature) {
        this.readingTemperature = readingTemperature;
    }
    /**
     * @return device model.
     */
    public DeviceModel getDeviceModel() {
        return deviceModel;
    }
    /**
     * @param model the deviceModel to set
     */
    public void setDeviceModel(final DeviceModel model) {
        this.deviceModel = model;
    }
}
