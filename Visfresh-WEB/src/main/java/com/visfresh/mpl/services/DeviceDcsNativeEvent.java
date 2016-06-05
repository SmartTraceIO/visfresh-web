/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;

import com.visfresh.entities.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDcsNativeEvent {
    private String imei;
    private Location location;
    private String type;
    private Date date;
    private Date createdOn = new Date();
    private double temperature;
    private int battery;
    /**
     * Default constructor.
     */
    public DeviceDcsNativeEvent() {
        super();
    }

    /**
     * @return
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @return
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @return
     */
    public Date getDate() {
        return date;
    }
    /**
     * @return
     */
    public String getType() {
        return type;
    }
    /**
     * @return
     */
    public Location getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final Location location) {
        this.location = location;
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     */
    public void setLocation(final double lat, final double lon) {
        setLocation(new Location(lat, lon));
    }
    /**
     * @return IMEI code.
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
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
}
