/**
 *
 */
package com.visfresh.io;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentDto {
    private String shipmentDescription;
    private String device;
    private int tripCount;
    private String palletId;
    private int poNum;

    private long shippedFrom;
    private long shippedTo;
    private Date shipmentDate;

    private String assetNum;
    private String assetType;

    private long alertProfile;
    private int alertSuppressionDuringCoolDown;
    private long[] alertsNotificationSchedules = {};

    private int arrivalNotificationWithIn;
    private long[] arrivalNotificationSchedules = {};
    private boolean excludeNotificationsIfNoAlertsFired;

    private int shutdownDevice;

    private final Map<String, String> customFields = new HashMap<String, String>();

    private String status;
    private final List<SingleShipmentTimeItem> items = new LinkedList<SingleShipmentTimeItem>();

    /**
     * Default constructor.
     */
    public SingleShipmentDto() {
        super();
    }

    /**
     * @return the shipmentDescription
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
     * @return the poNum
     */
    public int getPoNum() {
        return poNum;
    }
    /**
     * @param poNum the poNum to set
     */
    public void setPoNum(final int poNum) {
        this.poNum = poNum;
    }
    /**
     * @return the shippedFrom
     */
    public long getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final long shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public long getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final long shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the shipmentDate
     */
    public Date getShipmentDate() {
        return shipmentDate;
    }
    /**
     * @param shipmentDate the shipmentDate to set
     */
    public void setShipmentDate(final Date shipmentDate) {
        this.shipmentDate = shipmentDate;
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
     * @return the assetType
     */
    public String getAssetType() {
        return assetType;
    }
    /**
     * @param assetType the assetType to set
     */
    public void setAssetType(final String assetType) {
        this.assetType = assetType;
    }
    /**
     * @return the alertProfile
     */
    public long getAlertProfile() {
        return alertProfile;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final long alertProfile) {
        this.alertProfile = alertProfile;
    }
    /**
     * @return the alertSuppressionDuringCoolDown
     */
    public int getAlertSuppressionDuringCoolDown() {
        return alertSuppressionDuringCoolDown;
    }
    /**
     * @param alertSuppressionDuringCoolDown the alertSuppressionDuringCoolDown to set
     */
    public void setAlertSuppressionDuringCoolDown(final int alertSuppressionDuringCoolDown) {
        this.alertSuppressionDuringCoolDown = alertSuppressionDuringCoolDown;
    }
    /**
     * @return the alertsNotificationSchedules
     */
    public long[] getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @param alertsNotificationSchedules the alertsNotificationSchedules to set
     */
    public void setAlertsNotificationSchedules(final long[] alertsNotificationSchedules) {
        this.alertsNotificationSchedules = alertsNotificationSchedules;
    }
    /**
     * @return the arrivalNotificationWithIn
     */
    public int getArrivalNotificationWithIn() {
        return arrivalNotificationWithIn;
    }
    /**
     * @param arrivalNotificationWithIn the arrivalNotificationWithIn to set
     */
    public void setArrivalNotificationWithIn(final int arrivalNotificationWithIn) {
        this.arrivalNotificationWithIn = arrivalNotificationWithIn;
    }
    /**
     * @return the arrivalNotificationSchedules
     */
    public long[] getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
    }
    /**
     * @param arrivalNotificationSchedules the arrivalNotificationSchedules to set
     */
    public void setArrivalNotificationSchedules(final long[] arrivalNotificationSchedules) {
        this.arrivalNotificationSchedules = arrivalNotificationSchedules;
    }
    /**
     * @return the excludeNotificationsIfNoAlertsFired
     */
    public boolean isExcludeNotificationsIfNoAlertsFired() {
        return excludeNotificationsIfNoAlertsFired;
    }
    /**
     * @param excludeNotificationsIfNoAlertsFired the excludeNotificationsIfNoAlertsFired to set
     */
    public void setExcludeNotificationsIfNoAlertsFired(
            final boolean excludeNotificationsIfNoAlertsFired) {
        this.excludeNotificationsIfNoAlertsFired = excludeNotificationsIfNoAlertsFired;
    }
    /**
     * @return the shutdownDevice
     */
    public int getShutdownDevice() {
        return shutdownDevice;
    }
    /**
     * @param shutdownDevice the shutdownDevice to set
     */
    public void setShutdownDevice(final int shutdownDevice) {
        this.shutdownDevice = shutdownDevice;
    }
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }
    /**
     * @return the customFields
     */
    public Map<String, String> getCustomFields() {
        return customFields;
    }
    /**
     * @return the items
     */
    public List<SingleShipmentTimeItem> getItems() {
        return items;
    }
}
