/**
 *
 */
package com.visfresh.lists;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.InterimStopDto;
import com.visfresh.io.KeyLocation;

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
    private String shipmentDate;
    private String estArrivalDate;
    private String estArrivalDateISO;
    private String actualArrivalDate;
    private String actualArrivalDateISO;
    private int percentageComplete;
    private String assetNum;
    private String assetType;
    private Long alertProfileId;
    private String alertProfileName;
    private final Map<AlertType, Integer> alertSummary = new HashMap<AlertType, Integer>();
    private ShipmentStatus status;
    private int siblingCount = 5;

    //last reading data
    private String lastReadingTime;
    private String lastReadingTimeISO;
    private Double lastReadingTemperature;
    private Integer lastReadingBattery;
    private Double lastReadingLat;
    private Double lastReadingLong;

    private Double shippedFromLat;
    private Double shippedFromLong;
    private Double shippedToLat;
    private Double shippedToLong;

    private String shipmentDateISO;

    private Double firstReadingLat;
    private Double firstReadingLong;
    private String firstReadingTime;
    private String firstReadingTimeISO;
    private final List<InterimStopDto> interimStops = new LinkedList<>();
    private List<KeyLocation> keyLocations;

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
        this.setDeviceSN(s.getDevice().getSn());
        this.setPalettid(s.getPalletId());
        this.setPercentageComplete(0);
        this.setShipmentDescription(s.getShipmentDescription());
        this.setShipmentId(s.getId());
        this.setShippedFrom(s.getShippedFrom() == null ? null : s.getShippedFrom().getName());
        this.setShippedTo(s.getShippedTo() == null ? null : s.getShippedTo().getName());
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
    public String getShipmentDate() {
        return shipmentDate;
    }
    /**
     * @param shipmentDate the shipmentDate to set
     */
    public void setShipmentDate(final String shipmentDate) {
        this.shipmentDate = shipmentDate;
    }
    /**
     * @param date
     */
    public void setShipmentDateISO(final String date) {
        this.shipmentDateISO = date;
    }
    /**
     * @return the shipmentDateISO
     */
    public String getShipmentDateISO() {
        return shipmentDateISO;
    }

    /**
     * @return the estArrivalDate
     */
    public String getEstArrivalDate() {
        return estArrivalDate;
    }
    /**
     * @param estArrivalDate the estArrivalDate to set
     */
    public void setEstArrivalDate(final String estArrivalDate) {
        this.estArrivalDate = estArrivalDate;
    }
    public String getEstArrivalDateISO() {
        return estArrivalDateISO;
    }
    public void setEstArrivalDateISO(final String date) {
        this.estArrivalDateISO = date;
    }
    /**
     * @return the actualArrivalDate
     */
    public String getActualArrivalDate() {
        return actualArrivalDate;
    }
    /**
     * @param actualArrivalDate the actualArrivalDate to set
     */
    public void setActualArrivalDate(final String actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
    }
    /**
     * @param date
     */
    public void setActualArrivalDateISO(final String date) {
        this.actualArrivalDateISO = date;
    }
    /**
     * @return the actualArrivalDateISO
     */
    public String getActualArrivalDateISO() {
        return actualArrivalDateISO;
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
    /**
     * @return the lastReadingTimeISO
     */
    public String getLastReadingTimeISO() {
        return lastReadingTimeISO;
    }
    /**
     * @param lastReadingTimeISO the lastReadingTimeISO to set
     */
    public void setLastReadingTimeISO(final String lastReadingTimeISO) {
        this.lastReadingTimeISO = lastReadingTimeISO;
    }
    /**
     * @param time
     */
    public void setLastReadingTime(final String time) {
        this.lastReadingTime = time;
    }
    /**
     * @return the lastReadingTime
     */
    public String getLastReadingTime() {
        return lastReadingTime;
    }
    /**
     * @return the lastReadingTemperature
     */
    public Double getLastReadingTemperature() {
        return lastReadingTemperature;
    }
    /**
     * @param lastReadingTemperature the lastReadingTemperature to set
     */
    public void setLastReadingTemperature(final double lastReadingTemperature) {
        this.lastReadingTemperature = lastReadingTemperature;
    }
    /**
     * @return the lastReadingBattery
     */
    public Integer getLastReadingBattery() {
        return lastReadingBattery;
    }
    /**
     * @param lastReadingBattery the lastReadingBattery to set
     */
    public void setLastReadingBattery(final int lastReadingBattery) {
        this.lastReadingBattery = lastReadingBattery;
    }
    /**
     * @return the lastReadingLat
     */
    public Double getLastReadingLat() {
        return lastReadingLat;
    }
    /**
     * @param lastReadingLat the lastReadingLat to set
     */
    public void setLastReadingLat(final double lastReadingLat) {
        this.lastReadingLat = lastReadingLat;
    }
    /**
     * @return the lastReadingLong
     */
    public Double getLastReadingLong() {
        return lastReadingLong;
    }
    /**
     * @param lastReadingLong the lastReadingLong to set
     */
    public void setLastReadingLong(final double lastReadingLong) {
        this.lastReadingLong = lastReadingLong;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return getShipmentId();
    }
    /**
     * @return the shippedFromLat
     */
    public Double getShippedFromLat() {
        return shippedFromLat;
    }
    /**
     * @param shippedFromLat the shippedFromLat to set
     */
    public void setShippedFromLat(final double shippedFromLat) {
        this.shippedFromLat = shippedFromLat;
    }
    /**
     * @return the shippedFromLong
     */
    public Double getShippedFromLong() {
        return shippedFromLong;
    }
    /**
     * @param shippedFromLong the shippedFromLong to set
     */
    public void setShippedFromLong(final double shippedFromLong) {
        this.shippedFromLong = shippedFromLong;
    }
    /**
     * @return the shippedToLat
     */
    public Double getShippedToLat() {
        return shippedToLat;
    }
    /**
     * @param shippedToLat the shippedToLat to set
     */
    public void setShippedToLat(final double shippedToLat) {
        this.shippedToLat = shippedToLat;
    }
    /**
     * @return the shippedToLong
     */
    public Double getShippedToLong() {
        return shippedToLong;
    }
    /**
     * @param shippedToLong the shippedToLong to set
     */
    public void setShippedToLong(final double shippedToLong) {
        this.shippedToLong = shippedToLong;
    }
    /**
     * @return the firstReadingLat
     */
    public Double getFirstReadingLat() {
        return firstReadingLat;
    }
    /**
     * @param firstReadingLat the firstReadingLat to set
     */
    public void setFirstReadingLat(final double firstReadingLat) {
        this.firstReadingLat = firstReadingLat;
    }
    /**
     * @return the firstReadingLong
     */
    public Double getFirstReadingLong() {
        return firstReadingLong;
    }
    /**
     * @param firstReadingLong the firstReadingLong to set
     */
    public void setFirstReadingLong(final double firstReadingLong) {
        this.firstReadingLong = firstReadingLong;
    }
    /**
     * @param date
     */
    public void setFirstReadingTime(final String date) {
        this.firstReadingTime = date;
    }
    /**
     * @return the firstReadingTime
     */
    public String getFirstReadingTime() {
        return firstReadingTime;
    }
    /**
     * @param date
     */
    public void setFirstReadingTimeISO(final String date) {
        this.firstReadingTimeISO = date;
    }
    /**
     * @return the firstReadingTimeISO
     */
    public String getFirstReadingTimeISO() {
        return firstReadingTimeISO;
    }
    /**
     * @return
     */
    public List<InterimStopDto> getInterimStops() {
        return interimStops;
    }
    /**
     * @return
     */
    public List<KeyLocation> getKeyLocations() {
        return keyLocations;
    }
    /**
     * @param keyLocations the keyLocations to set
     */
    public void setKeyLocations(final List<KeyLocation> keyLocations) {
        this.keyLocations = keyLocations;
    }
}
