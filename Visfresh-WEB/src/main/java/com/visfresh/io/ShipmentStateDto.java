/**
 *
 */
package com.visfresh.io;

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
public class ShipmentStateDto implements EntityWithId<Long> {
    private Long shipmentId;
    private String deviceSN;
    private String deviceName;
    private int tripcount;
    private String shipmentDescription;
    private String palettid;
    private Long shippedFrom;
    private Long shippedTo;
    private Date shipmentDate;
    private Date estArrivalDate;
    private Date actualArrivalDate;
    private int percentageCompleted;
    private String assetNum;
    private String assetType;
    private Long alertProfile;
    private String alertProfileName;
    private final Map<AlertType, Integer> alertSummary = new HashMap<AlertType, Integer>();
    private ShipmentStatus status;

    /**
     * Default constructor.
     */
    public ShipmentStateDto() {
        super();
    }
    public ShipmentStateDto(final Shipment s) {
        super();
        this.setAlertProfile(s.getAlertProfile() != null ? s.getAlertProfile().getId() : null);
        this.setAlertProfileName(s.getAlertProfile() != null ? s.getAlertProfile().getName() : null);
        this.setAssetNum(s.getAssetNum());
        this.setAssetType(s.getAssetType());
        this.setDeviceName(s.getDevice().getName());
        this.setDeviceSN(s.getDevice().getId());
        this.setEstArrivalDate(s.getShipmentDate());
        this.setPalettid(s.getPalletId());
        this.setPercentageCompleted(0);
        this.setShipmentDate(s.getShipmentDate());
        this.setShipmentDescription(s.getShipmentDescription());
        this.setShipmentId(s.getId());
        this.setShippedFrom(s.getShippedFrom() == null ? null : s.getShippedFrom().getId());
        this.setShippedTo(s.getShippedTo() == null ? null : s.getShippedTo().getId());
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
    public int getTripcount() {
        return tripcount;
    }
    /**
     * @param tripcount the tripcount to set
     */
    public void setTripcount(final int tripcount) {
        this.tripcount = tripcount;
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
    public String getPalettid() {
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
    public Long getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final Long shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public Long getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final Long shippedTo) {
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
    public int getPercentageCompleted() {
        return percentageCompleted;
    }
    /**
     * @param percentageCompleted the percentageCompleted to set
     */
    public void setPercentageCompleted(final int percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
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
    public Long getAlertProfile() {
        return alertProfile;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final Long alertProfile) {
        this.alertProfile = alertProfile;
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
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return getShipmentId();
    }
}
