/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEvent implements EntityWithId<Long>, Comparable<TrackerEvent> {
    /**
     * Event ID.
     */
    private Long id;
    /**
     * Tracker event type.
     */
    private TrackerEventType type;
    /**
     * Creation date.
     */
    private Date createdOn;
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
    private Device device;
    private Shipment shipment;
    private Double latitude;
    private Double longitude;
    private String beaconId;

    /**
     * Default constructor.
     */
    public TrackerEvent() {
        super();
        setCreatedOn(new Date());
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
     * @return the shipment
     */
    public Shipment getShipment() {
        return shipment;
    }
    /**
     * @param shipment the shipment to set
     */
    public void setShipment(final Shipment shipment) {
        this.shipment = shipment;
    }
    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }
    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }
    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }
    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(final Date createdOn) {
        this.createdOn = createdOn;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final TrackerEvent o) {
        return getTime().compareTo(o.getTime());
    }
    /**
     * @return the beaconId
     */
    public String getBeaconId() {
        return beaconId;
    }
    /**
     * @param beaconId the beaconId to set
     */
    public void setBeaconId(final String beaconId) {
        this.beaconId = beaconId;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
            .append("device: ")
            .append(getDevice())
            .append(", location: (lan ")
            .append(getLatitude())
            .append(", lon ")
            .append(getLongitude())
            .append("), temperature: ")
            .append(getTemperature());
        return sb.toString();
    }
}
