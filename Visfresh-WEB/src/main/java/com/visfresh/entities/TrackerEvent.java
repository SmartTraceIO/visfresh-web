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
     * Message type.
     *     INIT - means auto-collected-data at device starting,
     *     AUT - means auto-collected-data by timer
     *     RSP -means the RESPONSE that is replied to server
     *     VIB - means the device start to vibrating
     *     STP - means the device is stable
     *     BRT - means the device enters bright environment
     *     DRK -means the device enters dark environment
     */

    private String type;
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
    private double latitude;
    private double longitude;

    /**
     * Default constructor.
     */
    public TrackerEvent() {
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
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
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
    public int compareTo(final TrackerEvent o) {
        return getTime().compareTo(o.getTime());
    }
}
