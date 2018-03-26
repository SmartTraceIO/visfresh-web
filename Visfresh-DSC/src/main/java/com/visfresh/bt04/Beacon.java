/**
 *
 */
package com.visfresh.bt04;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Beacon {
    // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<LF>
    // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<LF>
    private String sn;
    private String name;
    private Double temperature;
    private Double humidity;
    private Double distance;
    private double battery;
    private Date lastScannedTime;
    private String hardwareModel;

    /**
     * Default constructor.
     */
    public Beacon() {
        super();
    }

    /**
     * @return the sn
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
     * @return the humidity
     */
    public Double getHumidity() {
        return humidity;
    }
    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final Double humidity) {
        this.humidity = humidity;
    }
    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }
    /**
     * @param distance the distance to set
     */
    public void setDistance(final Double distance) {
        this.distance = distance;
    }
    /**
     * @return the battery
     */
    public double getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final double battery) {
        this.battery = battery;
    }
    /**
     * @return the lastScannedTime
     */
    public Date getLastScannedTime() {
        return lastScannedTime;
    }
    /**
     * @param lastScannedTime the lastScannedTime to set
     */
    public void setLastScannedTime(final Date lastScannedTime) {
        this.lastScannedTime = lastScannedTime;
    }
    /**
     * @return the hardwareModel
     */
    public String getHardwareModel() {
        return hardwareModel;
    }
    /**
     * @param hardwareModel the hardwareModel to set
     */
    public void setHardwareModel(final String hardwareModel) {
        this.hardwareModel = hardwareModel;
    }
}
