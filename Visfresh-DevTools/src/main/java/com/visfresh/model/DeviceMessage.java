/**
 *
 */
package com.visfresh.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessage {
    /**
     * List of station signals.
     */
    private final List<StationSignal> stations = new LinkedList<StationSignal>();
    /**
     * Device IMEI code.
     */
    private String imei;
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
    private String message;
    /**
     * Time of creation.
     */
    private Date loggTime;
    /**
     * Resolved location. Can be null of not resolved.
     */
    private Location location;

    /**
     * Default constructor.
     */
    public DeviceMessage() {
        super();
    }

    /**
     * @return the stations
     */
    public List<StationSignal> getStations() {
        return stations;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        //358688000000158|AUT|2013/10/18 13:28:29|<LF> <IMEI>|<DATA_TYPE>|<TIME>|
        //4023|-10.24|<LF> <BATTERY>|<TEMPERATURE>|
        final StringBuilder sb = new StringBuilder();
        sb.append(getImei()).append('|');
        sb.append(getType()).append('|');
        sb.append(getTime()).append('|');

        sb.append('\n');

        sb.append(getBattery()).append('|');
        sb.append(getTemperature()).append('|');
        sb.append('\n');

        for (final StationSignal station : getStations()) {
            sb.append(station).append('\n');
        }
        return sb.toString();
    }

    /**
     * @param msg
     */
    public void setMessage(final String msg) {
        this.message = msg;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @return the loggTime
     */
    public Date getLoggTime() {
        return loggTime;
    }
    /**
     * @param loggTime the loggTime to set
     */
    public void setLoggTime(final Date loggTime) {
        this.loggTime = loggTime;
    }
    /**
     * @return the location
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
}
