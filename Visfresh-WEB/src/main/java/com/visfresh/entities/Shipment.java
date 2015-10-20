/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Shipment extends ShipmentBase {
    /**
     * List of associated devices.
     */
    private final List<Device> devices = new LinkedList<Device>();
    /**
     * Pallet ID.
     */
    private String palletId;
    /**
     * PO number (?)
     */
    private String assetNum;
    /**
     * Shipment description date
     */
    private Date shipmentDescriptionDate;
    /**
     * This field has reserver for future custom fields implementation.
     */
    private String customFields;
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
     * @return the shipmentDescriptionDate
     */
    public Date getShipmentDescriptionDate() {
        return shipmentDescriptionDate;
    }
    /**
     * @param shipmentDescriptionDate the shipmentDescriptionDate to set
     */
    public void setShipmentDescriptionDate(final Date shipmentDescriptionDate) {
        this.shipmentDescriptionDate = shipmentDescriptionDate;
    }
    /**
     * @return the customFields
     */
    public String getCustomFields() {
        return customFields;
    }
    /**
     * @param customFields the customFields to set
     */
    public void setCustomFields(final String customFields) {
        this.customFields = customFields;
    }
    /**
     * @return the devices
     */
    public List<Device> getDevices() {
        return devices;
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
}
