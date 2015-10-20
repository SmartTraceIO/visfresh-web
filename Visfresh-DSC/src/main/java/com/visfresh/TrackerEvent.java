/**
 *
 */
package com.visfresh;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEvent implements Comparable<TrackerEvent> {
    /**
     * Event ID.
     */
    private Long id;
    /**
     * Message type.
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
     * Latitude.
     */
    private double latitude;
    /**
     * Longitude.
     */
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
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final TrackerEvent o) {
        return getTime().compareTo(o.getTime());
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
}
