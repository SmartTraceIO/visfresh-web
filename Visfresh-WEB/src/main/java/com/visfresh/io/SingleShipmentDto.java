/**
 *
 */
package com.visfresh.io;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentDto {
    private String shipmentDescription;
    private int tripCount;
    private String palletId;
    private int poNum;

    private long shippedFrom;
    private long shippedTo;
    private Date shipmentDate;

    private String assetNum;
    private String assetType;

    private long alertProfileId;
    private final List<NotificationScheduleListItem> alertsNotificationSchedules
        = new LinkedList<NotificationScheduleListItem>();

    private int arrivalNotificationWithinKm;
    private final List<NotificationScheduleListItem> arrivalNotificationSchedules
        = new LinkedList<NotificationScheduleListItem>();
    private boolean excludeNotificationsIfNoAlertsFired;

    private String status;
    private final List<SingleShipmentTimeItem> items = new LinkedList<SingleShipmentTimeItem>();
    private String currentLocation;
    private String deviceSn;
    private String deviceName;
    private Date estArrivalDate;
    private Date actualArrivalDate;
    private int percentageComplete;
    private String alertProfileName;
    private int maxTimesAlertFires;
    private int alertSuppressionMinutes;
    private final Map<AlertType, Integer> alertSummary = new HashMap<AlertType, Integer>();
    private Long shipmentId;
    private String commentsForReceiver;

    /**
     * Default constructor.
     */
    public SingleShipmentDto() {
        super();
    }

    /**
     * @param shipment
     */
    public SingleShipmentDto(final Shipment shipment) {
        super();
        setAlertProfileId(shipment.getAlertProfile() == null ? null : shipment.getAlertProfile().getId());
        getAlertsNotificationSchedules().addAll(toListItems(shipment.getAlertsNotificationSchedules()));
        getArrivalNotificationSchedules().addAll(toListItems(shipment.getArrivalNotificationSchedules()));
        setArrivalNotificationWithInKm(shipment.getArrivalNotificationWithinKm());
        setAssetNum(shipment.getAssetNum());
        setAssetType(shipment.getAssetType());
        setPalletId(shipment.getPalletId());
        setPoNum(shipment.getPoNum());
        setShipmentDescription(shipment.getShipmentDescription());
        setShipmentId(shipment.getId());
        setShippedFrom(shipment.getShippedFrom() == null ? null : shipment.getShippedFrom().getId());
        setShippedTo(shipment.getShippedTo() == null ? null : shipment.getShippedTo().getId());
        setStatus(shipment.getStatus().getLabel());
        setTripCount(shipment.getTripCount());
        setCurrentLocation("Not determined");
        setDeviceSn(shipment.getDevice().getSn());
        setDeviceName(shipment.getDevice().getName());
        //Mock arrival date.
        //TODO replace it by calculated.
        final Date arrivalDate = new Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000L);
        setEstArrivalDate(arrivalDate);
        setActualArrivalDate(arrivalDate);
        //TODO replace with autodetected
        setPercentageComplete(0);
        setAlertProfileName(shipment.getAlertProfile() == null ? null : shipment.getAlertProfile().getName());
        setMaxTimesAlertFires(shipment.getMaxTimesAlertFires());
        setAlertSuppressionMinutes(shipment.getAlertSuppressionMinutes());
        setCommentsForReceiver(shipment.getCommentsForReceiver());
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
    public long getAlertProfileId() {
        return alertProfileId;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfileId(final long alertProfile) {
        this.alertProfileId = alertProfile;
    }
    /**
     * @return the alertsNotificationSchedules
     */
    public List<NotificationScheduleListItem> getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @return the arrivalNotificationWithIn
     */
    public int getArrivalNotificationWithInKm() {
        return arrivalNotificationWithinKm;
    }
    /**
     * @param arrivalNotificationWithIn the arrivalNotificationWithIn to set
     */
    public void setArrivalNotificationWithInKm(final int arrivalNotificationWithIn) {
        this.arrivalNotificationWithinKm = arrivalNotificationWithIn;
    }
    /**
     * @return the arrivalNotificationSchedules
     */
    public List<NotificationScheduleListItem> getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
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
     * @return the items
     */
    public List<SingleShipmentTimeItem> getItems() {
        return items;
    }
    /**
     * @return current location.
     */
    public String getCurrentLocation() {
        return currentLocation;
    }
    /**
     * @param currentLocation the currentLocation to set
     */
    public void setCurrentLocation(final String currentLocation) {
        this.currentLocation = currentLocation;
    }
    /**
     * @return device serial number.
     */
    public String getDeviceSn() {
        return deviceSn;
    }
    /**
     * @param deviceSn the deviceSn to set
     */
    public void setDeviceSn(final String deviceSn) {
        this.deviceSn = deviceSn;
    }
    /**
     * @return device name.
     */
    public String getDeviceName() {
        return deviceName;
    }
    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }
    /**
     * @return estimated arrival date.
     */
    public Date getEstArrivalDate() {
        return estArrivalDate;
    }
    /**
     * @param estArrivalDate the estArrivalDate to set
     */
    public void setEstArrivalDate(final Date estArrivalDate) {
        this.estArrivalDate = estArrivalDate;
    }
    /**
     * @return actual arrival date.
     */
    public Date getActualArrivalDate() {
        return actualArrivalDate;
    }
    /**
     * @param actualArrivalDate the actualArrivalDate to set
     */
    public void setActualArrivalDate(final Date actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
    }
    /**
     * @return percentage completed.
     */
    public int getPercentageComplete() {
        return percentageComplete;
    }
    /**
     * @param percentageComplete the percentageComplete to set
     */
    public void setPercentageComplete(final int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }
    /**
     * @return alert profile name.
     */
    public String getAlertProfileName() {
        return alertProfileName;
    }
    /**
     * @param alertProfileName the alertProfileName to set
     */
    public void setAlertProfileName(final String alertProfileName) {
        this.alertProfileName = alertProfileName;
    }
    /**
     * @return max times alert fires.
     */
    public int getMaxTimesAlertFires() {
        return maxTimesAlertFires;
    }
    /**
     * @param maxTimesAlertFires the maxTimesAlertFires to set
     */
    public void setMaxTimesAlertFires(final int maxTimesAlertFires) {
        this.maxTimesAlertFires = maxTimesAlertFires;
    }
    /**
     * @return alert suppression in minutes.
     */
    public int getAlertSuppressionMinutes() {
        return alertSuppressionMinutes;
    }
    /**
     * @param alertSuppressionMinutes the alertSuppressionMinutes to set
     */
    public void setAlertSuppressionMinutes(final int alertSuppressionMinutes) {
        this.alertSuppressionMinutes = alertSuppressionMinutes;
    }
    /**
     * @return alert summary map.
     */
    public Map<AlertType, Integer> getAlertSummary() {
        return alertSummary;
    }
    /**
     * @return shipment ID.
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
     * @return
     */
    public String getCommentsForReceiver() {
        return commentsForReceiver;
    }
    /**
     * @param comments the commentsForReceiver to set
     */
    public void setCommentsForReceiver(final String comments) {
        this.commentsForReceiver = comments;
    }

    /**
     * @param arrivalNotificationSchedules
     * @return
     */
    private List<NotificationScheduleListItem> toListItems(final List<NotificationSchedule> entities) {
        final List<NotificationScheduleListItem> items = new LinkedList<NotificationScheduleListItem>();
        for (final NotificationSchedule s : entities) {
            items.add(new NotificationScheduleListItem(s));
        }
        return items;
    }
}
