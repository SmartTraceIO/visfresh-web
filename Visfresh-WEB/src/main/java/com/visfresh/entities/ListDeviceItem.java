/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListDeviceItem {
    /**
     * Device IMEI code
     */
    private String imei;
    /**
     * Device name.
     */
    private String name;
    /**
     * Device model.
     */
    private DeviceModel model;
    /**
     * Device description
     */
    private String description;
    /**
     * Current device trip count.
     */
    private int tripCount;
    /**
     * Active flag.
     */
    private boolean active;
    /**
     * Color.
     */
    private Color color;

    //last reading data
    private Date lastReadingTime;
    private Double temperature;
    private Integer battery;
    private Double latitude;
    private Double longitude;
    private Long shipmentId;
    private ShipmentStatus shipmentStatus;
    private Long autostartTemplateId;
    private String autostartTemplateName;

    /**
     * Default constructor.
     */
    public ListDeviceItem() {
        super();
    }
    /**
     * @param d device.
     */
    public ListDeviceItem(final Device d) {
        super();
        setActive(d.isActive());
        setAutostartTemplateId(d.getAutostartTemplateId());
        setDescription(d.getDescription());
        setImei(d.getImei());
        setName(d.getName());
        setTripCount(d.getTripCount());
        setColor(d.getColor());
        setModel(d.getModel());
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
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
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
     * @return the temperature
     */
    public Double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the battery
     */
    public Integer getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final Integer battery) {
        this.battery = battery;
    }
    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
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
     * @return the shipmentStatus
     */
    public ShipmentStatus getShipmentStatus() {
        return shipmentStatus;
    }
    /**
     * @param shipmentStatus the shipmentStatus to set
     */
    public void setShipmentStatus(final ShipmentStatus shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }
    /**
     * @return the autostartTemplateId
     */
    public Long getAutostartTemplateId() {
        return autostartTemplateId;
    }
    /**
     * @param id the autostartTemplateId to set
     */
    public void setAutostartTemplateId(final Long id) {
        this.autostartTemplateId = id;
    }
    /**
     * @return the autostartTemplateName
     */
    public String getAutostartTemplateName() {
        return autostartTemplateName;
    }
    /**
     * @param name the autostartTemplateName to set
     */
    public void setAutostartTemplateName(final String name) {
        this.autostartTemplateName = name;
    }
    /**
     * @param c device color.
     */
    public void setColor(final Color c) {
        this.color = c;
    }
    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }
    /**
     * @return the model
     */
    public DeviceModel getModel() {
        return model;
    }
    /**
     * @param model the model to set
     */
    public void setModel(final DeviceModel model) {
        this.model = model;
    }
}
