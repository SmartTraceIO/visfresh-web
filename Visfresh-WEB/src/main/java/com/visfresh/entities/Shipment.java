/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Shipment extends ShipmentBase {
    /**
     * List of associated devices.
     */
    private Device device;
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
    private Date shipmentDate = new Date();
    /**
     * The date of last tracker event for given shipment.
     */
    private Date lastEventDate = new Date();
    /**
     * The start date.
     */
    private Date startDate = new Date();
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
    private final Set<Long> siblings = new HashSet<>();
    private Date arrivalDate;
    private Date eta;
    /**
     * Nearest device. Read only property.
     */
    private String nearestTracker;

    /**
     * Default constructor.
     */
    public Shipment() {
        super();
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
    public String getAssetNum() {
        return assetNum;
    }
    /**
     * @param poNum the poNum to set
     */
    public void setAssetNum(final String poNum) {
        this.assetNum = poNum;
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
     * @return the customFields
     */
    public Map<String, String> getCustomFields() {
        return customFields;
    }
    /**
     * @return the devices
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDevice(final Device device) {
        this.device = device;
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
     * @return device shutdown time.
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
     * @return the siblings
     */
    public Set<Long> getSiblings() {
        return siblings;
    }
    /**
     * @param count sibling count.
     */
    public void setSiblingCount(final int count) {
        this.siblingCount = count;
    }
    /**
     * @return the siblingCount
     */
    public int getSiblingCount() {
        return siblingCount;
    }
    /**
     * @param date arrival date.
     */
    public void setArrivalDate(final Date date) {
        this.arrivalDate = date;
    }
    /**
     * @return the arrivalDate
     */
    public Date getArrivalDate() {
        return arrivalDate;
    }
    /**
     * @param eta the eta to set
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
     * @return the nearestDevice
     */
    public String getNearestTracker() {
        return nearestTracker;
    }
    /**
     * Warning!!! this method is only for DAO. The set of nearest device
     * and saving then given device will not change associated DB field.
     * @param nearestDevice the nearestDevice to set
     */
    public void setNearestTracker(final String nearestDevice) {
        this.nearestTracker = nearestDevice;
    }

    /**
     * @return
     */
    public boolean hasFinalStatus() {
        return status != null && status.isFinal();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append(getId());
        if (getDevice() != null) {
            sb.append(": ");
            sb.append(getDevice().getImei());
            sb.append("(");
            sb.append(getTripCount());
            sb.append(")");
        }
        return sb.toString();
    }
}
