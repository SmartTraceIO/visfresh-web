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
    private final Location location = new Location();
    private String type;
    private Date date;
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
    public Date getTime() {
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
}
