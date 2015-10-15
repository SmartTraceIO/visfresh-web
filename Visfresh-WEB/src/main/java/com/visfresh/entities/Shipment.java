/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="shipments")
public class Shipment extends ShipmentBase {
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "id", targetEntity = Device.class)
    private final List<Device> devices = new LinkedList<Device>();
    private String palletId;
    private String poNum;
    private Date shipmentDescriptionDate;
    private String customFields;
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
    public String getPoNum() {
        return poNum;
    }
    /**
     * @param poNum the poNum to set
     */
    public void setPoNum(final String poNum) {
        this.poNum = poNum;
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
