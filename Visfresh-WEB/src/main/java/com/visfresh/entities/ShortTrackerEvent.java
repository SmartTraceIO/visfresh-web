/**
 *
 */
package com.visfresh.entities;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortTrackerEvent implements EntityWithId<Long>, Comparable<ShortTrackerEvent>,
        Serializable {
    private static final long serialVersionUID = 5009822165362091286L;
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
    private Double latitude;
    private Double longitude;
    private Date createdOn;
    /**
     * Humidity in percents 0-100%
     */
    private Integer humidity;

    /**
     * Default constructor.
     */
    public ShortTrackerEvent() {
        super();
    }

    /**
     * @param e
     */
    public ShortTrackerEvent(final TrackerEvent e) {
        super();
        this.setBattery(e.getBattery());
        this.setCreatedOn(e.getCreatedOn());
        this.setDeviceImei(e.getDevice().getImei());
        this.setId(e.getId());
        this.setLatitude(e.getLatitude());
        this.setLongitude(e.getLongitude());
        this.setShipmentId(e.getShipment() == null ? null : e.getShipment().getId());
        this.setTemperature(e.getTemperature());
        this.setTime(e.getTime());
        this.setType(e.getType());
        this.setHumidity(e.getHumidity());
    }

    /**
     * @param e
     */
    public ShortTrackerEvent(final ShortTrackerEvent e) {
        super();
        this.setBattery(e.getBattery());
        this.setCreatedOn(e.getCreatedOn());
        this.setDeviceImei(e.getDeviceImei());
        this.setId(e.getId());
        this.setLatitude(e.getLatitude());
        this.setLongitude(e.getLongitude());
        this.setShipmentId(e.getShipmentId());
        this.setTemperature(e.getTemperature());
        this.setTime(e.getTime());
        this.setType(e.getType());
        this.setHumidity(e.getHumidity());
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
     * @param createdOn
     */
    public void setCreatedOn(final Date createdOn) {
        this.createdOn = createdOn;
    }
    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }
    /**
     * @return the humidity
     */
    public Integer getHumidity() {
        return humidity;
    }
    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final Integer humidity) {
        this.humidity = humidity;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ShortTrackerEvent o) {
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
