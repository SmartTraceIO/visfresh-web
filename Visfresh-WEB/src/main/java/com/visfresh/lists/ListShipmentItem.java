/**
 *
 */
package com.visfresh.lists;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.TemperatureRuleBean;

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
    private Date actualArrivalDate;
    private int percentageComplete;
    private String assetNum;
    private String assetType;
    private Long alertProfileId;
    private String alertProfileName;
    private ShipmentStatus status;
    private int siblingCount = 5;

    //last reading data
    private Date lastReadingTime;
//    private String lastReadingTimeISO;
    private Double lastReadingTemperature;
    private Integer lastReadingBattery;
    private Double lastReadingLat;
    private Double lastReadingLong;

    private Double shippedFromLat;
    private Double shippedFromLong;
    private Double shippedToLat;
    private Double shippedToLong;

    private Double firstReadingLat;
    private Double firstReadingLong;
    private Date firstReadingTime;
    private final List<InterimStopBean> interimStops = new LinkedList<>();

    private boolean sendArrivalReport;
    private boolean sendArrivalReportOnlyIfAlerts;
    private Date eta;
    private String device;
    private final List<AlertBean> sentAlerts = new LinkedList<>();
    private final List<TemperatureRuleBean> temperatureRules = new LinkedList<>();

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
        this.setTripCount(s.getTripCount());
        this.setSiblingCount(s.getSiblingCount());
        this.setSendArrivalReport(s.isSendArrivalReport());
        this.setSendArrivalReportOnlyIfAlerts(s.isSendArrivalReportOnlyIfAlerts());
        this.setEta(s.getEta());
        this.setLastReadingTime(s.getLastEventDate());
        this.setShipmentDate(s.getShipmentDate());
    }
    /**
     * @param eta
     */
    public void setEta(final Date eta) {
        this.eta = eta;
    }
    /**
     * @return the eta
     */
    public Date getEta() {
        return eta;
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
    public void setTripCount(final int count) {
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
    public void setLastReadingLat(final Double lastReadingLat) {
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
    public void setLastReadingLong(final Double lastReadingLong) {
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
    public void setShippedFromLat(final Double shippedFromLat) {
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
    public void setShippedFromLong(final Double shippedFromLong) {
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
    public void setShippedToLat(final Double shippedToLat) {
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
    public void setShippedToLong(final Double shippedToLong) {
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
    public void setFirstReadingLat(final Double firstReadingLat) {
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
    public void setFirstReadingLong(final Double firstReadingLong) {
        this.firstReadingLong = firstReadingLong;
    }
    /**
     * @return
     */
    public List<InterimStopBean> getInterimStops() {
        return interimStops;
    }
    /**
     * @return the sendArrivalReport
     */
    public boolean isSendArrivalReport() {
        return sendArrivalReport;
    }
    /**
     * @param sendArrivalReport the sendArrivalReport to set
     */
    public void setSendArrivalReport(final boolean sendArrivalReport) {
        this.sendArrivalReport = sendArrivalReport;
    }
    /**
     * @return the sendArrivalReportOnlyIfAlerts
     */
    public boolean isSendArrivalReportOnlyIfAlerts() {
        return sendArrivalReportOnlyIfAlerts;
    }
    /**
     * @param sendArrivalReportOnlyIfAlerts the sendArrivalReportOnlyIfAlerts to set
     */
    public void setSendArrivalReportOnlyIfAlerts(final boolean sendArrivalReportOnlyIfAlerts) {
        this.sendArrivalReportOnlyIfAlerts = sendArrivalReportOnlyIfAlerts;
    }
    /**
     * @return the lastReadingTime
     */
    public Date getLastReadingTime() {
        return lastReadingTime;
    }
    /**
     * @param lastReadingTime the lastReadingTime to set
     */
    public void setLastReadingTime(final Date lastReadingTime) {
        this.lastReadingTime = lastReadingTime;
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
     * @return the firstReadingTime
     */
    public Date getFirstReadingTime() {
        return firstReadingTime;
    }
    /**
     * @param firstReadingTime the firstReadingTime to set
     */
    public void setFirstReadingTime(final Date firstReadingTime) {
        this.firstReadingTime = firstReadingTime;
    }
    /**
     * @return device.
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
     * @return list of sent alerts.
     */
    public List<AlertBean> getSentAlerts() {
        return sentAlerts;
    }
    /**
     * @return list of temperature rules.
     */
    public List<TemperatureRuleBean> getTemperatureRules() {
        return temperatureRules;
    }
}
