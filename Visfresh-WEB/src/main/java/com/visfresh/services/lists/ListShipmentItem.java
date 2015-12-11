/**
 *
 */
package com.visfresh.services.lists;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListShipmentItem implements EntityWithId<Long> {
    private Long shipmentId;
    private String deviceSN;
    private String deviceName;
    private int tripCount;
    private String shipmentDescription;
    private String palettid;
    private String shippedFrom;
    private String shippedTo;
    private Date shipmentDate;
    private Date estArrivalDate;
    private Date actualArrivalDate;
    private int percentageComplete;
    private String assetNum;
    private String assetType;
    private Long alertProfileId;
    private String alertProfileName;
    private final Map<AlertType, Integer> alertSummary = new HashMap<AlertType, Integer>();
    private ShipmentStatus status;
    private int siblingCount;

    /**
     * Default constructor.
     */
    public ListShipmentItem() {
        super();
    }
    public ListShipmentItem(final Shipment s) {
        super();
        this.setAlertProfileId(s.getAlertProfile() != null ? s.getAlertProfile().getId() : null);
        this.setAlertProfileName(s.getAlertProfile() != null ? s.getAlertProfile().getName() : null);
        this.setAssetNum(s.getAssetNum());
        this.setAssetType(s.getAssetType());
        this.setDeviceName(s.getDevice().getName());
        this.setDeviceSN(s.getDevice().getId());
        this.setEstArrivalDate(s.getShipmentDate());
        this.setPalettid(s.getPalletId());
        this.setPercentageComplete(0);
        this.setShipmentDate(s.getShipmentDate());
        this.setShipmentDescription(s.getShipmentDescription());
        this.setShipmentId(s.getId());
        this.setShippedFrom(s.getShippedFrom() == null ? null : s.getShippedFrom().getAddress());
        this.setShippedTo(s.getShippedTo() == null ? null : s.getShippedTo().getAddress());
        this.setStatus(s.getStatus());
        this.setTripcount(s.getTripCount());
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
     * @return the deviceSN
     */
    public String getDeviceSN() {
        return deviceSN;
    }
    /**
     * @param deviceSN the deviceSN to set
     */
    public void setDeviceSN(final String deviceSN) {
        this.deviceSN = deviceSN;
    }
    /**
     * @return the deviceName
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
     * @return the tripcount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param count the tripcount to set
     */
    public void setTripcount(final int count) {
        this.tripCount = count;
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
     * @return the palettid
     */
    public String getPalettId() {
        return palettid;
    }
    /**
     * @param palettid the palettid to set
     */
    public void setPalettid(final String palettid) {
        this.palettid = palettid;
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
     * @return the estArrivalDate
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
     * @return the actualArrivalDate
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
     * @return the percentageCompleted
     */
    public int getPercentageComplete() {
        return percentageComplete;
    }
    /**
     * @param percentageCompleted the percentageCompleted to set
     */
    public void setPercentageComplete(final int percentageCompleted) {
        this.percentageComplete = percentageCompleted;
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
    public Long getAlertProfileId() {
        return alertProfileId;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfileId(final Long alertProfile) {
        this.alertProfileId = alertProfile;
    }
    /**
     * @return the alertProfileName
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
     * @return the alertSummary
     */
    public Map<AlertType, Integer> getAlertSummary() {
        return alertSummary;
    }
    /**
     * @return the siblingCount
     */
    public int getSiblingCount() {
        return siblingCount;
    }
    /**
     * @param siblingCount the siblingCount to set
     */
    public void setSiblingCount(final int siblingCount) {
        this.siblingCount = siblingCount;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return getShipmentId();
    }
}
