/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentLocationBean {
    private Double latitude;
    private Double longitude;
    private double temperature;
    private Date time;
    private final List<AlertBean> alerts = new LinkedList<>();
    private String type;

    /**
     * Default constructor.
     */
    public SingleShipmentLocationBean() {
        super();
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
     * @return the alerts
     */
    public List<AlertBean> getAlerts() {
        return alerts;
    }

    /**
     * @param eventType
     */
    public void setType(final String eventType) {
        this.type = eventType;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
}
