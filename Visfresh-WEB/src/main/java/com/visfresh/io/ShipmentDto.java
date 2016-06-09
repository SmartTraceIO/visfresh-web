/**
 *
 */
package com.visfresh.io;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentDto extends ShipmentBaseDto {
    private String deviceImei;
    private String deviceSN;
    private String deviceName;
    /**
     * Pallet ID.
     */
    private String palletId;
    /**
     * Asset number (?)
     */
    private String assetNum;
    /**
     * Trip count for given device.
     */
    private int tripCount;
    /**
     * PO number
     */
    private int poNum;
    /**
     * AssertType
     */
    private String assetType;

    /**
     * This field has reserver for future custom fields implementation.
     */
    private final Map<String, String> customFields = new HashMap<String, String>();

    /**
     * Shipment description date.
     */
    private Date shipmentDate;
    /**
     * The date of last tracker event for given shipment.
     */
    private Date lastEventDate;
    /**
     * The start date.
     */
    private Date startDate;
    /**
     * Email of author.
     */
    private String createdBy;
    /**
     * Shipment status.
     */
    private ShipmentStatus status = ShipmentStatus.Default;
    private Date deviceShutdownTime;
    private int siblingCount;
    private Date arrivalDate;
    private Date eta;

    /**
     * Default constructor.
     */
    public ShipmentDto() {
        super();
    }
    /**
     * @param s template.
     */
    public ShipmentDto(final Shipment s) {
        super(s);

        if (s.getDevice() != null) {
            setDeviceImei(s.getDevice().getImei());
            setDeviceSN(s.getDevice().getSn());
            setDeviceName(s.getDevice().getName());
        }

        setPalletId(s.getPalletId());
        setAssetNum(s.getAssetNum());
        setTripCount(s.getTripCount());
        setPoNum(s.getPoNum());
        setAssetType(s.getAssetType());
        getCustomFields().putAll(s.getCustomFields());
        setShipmentDate(s.getShipmentDate());
        setLastEventDate(s.getLastEventDate());
        setStartDate(s.getStartDate());
        setCreatedBy(s.getCreatedBy());
        setStatus(s.getStatus());
        setDeviceShutdownTime(s.getDeviceShutdownTime());
        setSiblingCount(s.getSiblings().size());
        setArrivalDate(s.getArrivalDate());
        setEta(s.getEta());
    }

    /**
     * @return the deviceImei
     */
    public String getDeviceImei() {
        return deviceImei;
    }
    /**
     * @param deviceImei the deviceImei to set
     */
    public void setDeviceImei(final String deviceImei) {
        this.deviceImei = deviceImei;
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
     * @return the lastEventDate
     */
    public Date getLastEventDate() {
        return lastEventDate;
    }
    /**
     * @param lastEventDate the lastEventDate to set
     */
    public void setLastEventDate(final Date lastEventDate) {
        this.lastEventDate = lastEventDate;
    }
    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }
    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
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
     * @return the deviceShutdownTime
     */
    public Date getDeviceShutdownTime() {
        return deviceShutdownTime;
    }
    /**
     * @param deviceShutdownTime the deviceShutdownTime to set
     */
    public void setDeviceShutdownTime(final Date deviceShutdownTime) {
        this.deviceShutdownTime = deviceShutdownTime;
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
     * @return the arrivalDate
     */
    public Date getArrivalDate() {
        return arrivalDate;
    }
    /**
     * @param arrivalDate the arrivalDate to set
     */
    public void setArrivalDate(final Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    /**
     * @return the eta
     */
    public Date getEta() {
        return eta;
    }
    /**
     * @param eta the eta to set
     */
    public void setEta(final Date eta) {
        this.eta = eta;
    }
    /**
     * @return the customFields
     */
    public Map<String, String> getCustomFields() {
        return customFields;
    }
}
