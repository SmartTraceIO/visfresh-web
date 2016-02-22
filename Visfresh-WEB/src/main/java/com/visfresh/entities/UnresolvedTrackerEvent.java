/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnresolvedTrackerEvent implements EntityWithId<Long>, Comparable<UnresolvedTrackerEvent> {
    /**
     * Event ID.
     */
    private Long id;
    /**
     * Tracker event type.
     */
    private TrackerEventType type;
    /**
     * Time of creation.
     */
    private Date time;
    /**
     * Battery charge.
     */
    private int battery;
    /**
     * Temperature
     */
    private double temperature;
    /**
     * The device.
     */
    private String device;
    private Long shipentId;
    private double latitude;
    private double longitude;

    /**
     * Default constructor.
     */
    public UnresolvedTrackerEvent() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the type
     */
    public TrackerEventType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final TrackerEventType type) {
        this.type = type;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }
    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the device
     */
    public String getDeviceImei() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDeviceImei(final String device) {
        this.device = device;
    }
    /**
     * @return the shipment
     */
    public Long getShipmentId() {
        return shipentId;
    }
    /**
     * @param shipment the shipment to set
     */
    public void setShipmentId(final Long shipment) {
        this.shipentId = shipment;
    }
    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }
    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final UnresolvedTrackerEvent o) {
        return getId().compareTo(o.getId());
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
            .append("device: ")
            .append(getDeviceImei())
            .append(", location: (lan ")
            .append(getLatitude())
            .append(", lon ")
            .append(getLongitude())
            .append("), temperature: ")
            .append(getTemperature());
        return sb.toString();
    }
}