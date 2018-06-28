/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentLocation {
    private Double latitude;
    private Double longitude;
    private double temperature;
    private String timeIso;
    private String time;
    private final List<SingleShipmentAlert> alerts = new LinkedList<>();
    private String type;
    private Integer humidity;

    /**
     * Default constructor.
     */
    public SingleShipmentLocation() {
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
     * @return the timeIso
     */
    public String getTimeIso() {
        return timeIso;
    }
    /**
     * @param timeIso the timeIso to set
     */
    public void setTimeIso(final String timeIso) {
        this.timeIso = timeIso;
    }
    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final String time) {
        this.time = time;
    }
    /**
     * @return the alerts
     */
    public List<SingleShipmentAlert> getAlerts() {
        return alerts;
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
