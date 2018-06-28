/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentLocationBean {
    private Long id;
    private Double latitude;
    private Double longitude;
    private double temperature;
    /**
     * Humidity in percents 0-100%
     */
    private Integer humidity;
    private Date time;
    private final List<AlertBean> alerts = new LinkedList<>();
    private TrackerEventType type;

    /**
     * Default constructor.
     */
    public SingleShipmentLocationBean() {
        super();
    }
    /**
     * @param e tracker event.
     */
    public SingleShipmentLocationBean(final TrackerEventDto e) {
        super();
        setId(e.getId());
        setLatitude(e.getLatitude());
        setLongitude(e.getLongitude());
        setTemperature(e.getTemperature());
        setHumidity(e.getHumidity());
        setTime(e.getTime());
        setType(e.getType());
    }

    /**
     * @param id
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
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
    public void setType(final TrackerEventType eventType) {
        this.type = eventType;
    }
    /**
     * @return the type
     */
    public TrackerEventType getType() {
        return type;
    }
}
