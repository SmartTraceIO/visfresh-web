/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
     * Shipment description date
     */
    private Date shipmentDate = new Date();
    /**
     * Shipment status.
     */
    private ShipmentStatus status = ShipmentStatus.Default;

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
}
