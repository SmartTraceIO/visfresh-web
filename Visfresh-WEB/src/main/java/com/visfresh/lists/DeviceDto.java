/**
 *
 */
package com.visfresh.lists;

import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDto {
    /**
     * Device IMEI code
     */
    private String imei;
    /**
     * Device name.
     */
    private String name;
    /**
     * Device SN.
     */
    private String sn;
    /**
     * Device description
     */
    private String description;
    /**
     * Active flag.
     */
    private boolean active;

    //last reading data
    private String lastReadingTimeISO;
    private String lastReadingTime;
    private String lastReadingTemperature;
    private Integer lastReadingBattery;
    private Double lastReadingLat;
    private Double lastReadingLong;
    private Long lastShipmentId;
    private String shipmentStatus;
    private String shipmentNumber;
    private Long autostartTemplateId;
    private String autostartTemplateName;

    /**
     * Default constructor.
     */
    public DeviceDto() {
        super();
    }
    /**
     * @param d device.
     */
    public DeviceDto(final Device d) {
        super();
        setDescription(d.getDescription());
        setImei(d.getImei());
        setName(d.getName());
        setSn(d.getSn());
        setActive(d.isActive());
    }

    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the SN
     */
    public String getSn() {
        return sn;
    }
    /**
     * @param sn the sn to set
     */
    public void setSn(final String sn) {
        this.sn = sn;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
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
     * @return the lastReadingTime
     */
    public String getLastReadingTime() {
        return lastReadingTime;
    }
    /**
     * @param time the lastReadingTime to set
     */
    public void setLastReadingTime(final String time) {
        this.lastReadingTime = time;
    }
    /**
     * @return the lastReadingTemperature
     */
    public String getLastReadingTemperature() {
        return lastReadingTemperature;
    }
    /**
     * @param lastReadingTemperature the lastReadingTemperature to set
     */
    public void setLastReadingTemperature(final String lastReadingTemperature) {
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
    public void setLastReadingBattery(final Integer lastReadingBattery) {
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
    /**
     * @param lastShipmentId the lastShipmentId to set
     */
    public void setLastShipmentId(final Long lastShipmentId) {
        this.lastShipmentId = lastShipmentId;
    }
    /**
     * @return the lastShipmentId
     */
    public Long getLastShipmentId() {
        return lastShipmentId;
    }
    public String getShipmentStatus() {
        return this.shipmentStatus;
    }
    /**
     * @param s the shipment status.
     */
    public void setShipmentStatus(final String s) {
        this.shipmentStatus = s;
    }
    public String getShipmentNumber() {
        return shipmentNumber;
    }
    /**
     * @param shipmentNumber the shipmentNumber to set
     */
    public void setShipmentNumber(final String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }
    /**
     * @return the autostartTemplateId
     */
    public Long getAutostartTemplateId() {
        return autostartTemplateId;
    }
    /**
     * @param autostartTemplateId the autostartTemplateId to set
     */
    public void setAutostartTemplateId(final Long autostartTemplateId) {
        this.autostartTemplateId = autostartTemplateId;
    }
    /**
     * @return the autostartTemplateName
     */
    public String getAutostartTemplateName() {
        return autostartTemplateName;
    }
    /**
     * @param autostartTemplateName the autostartTemplateName to set
     */
    public void setAutostartTemplateName(final String autostartTemplateName) {
        this.autostartTemplateName = autostartTemplateName;
    }
}