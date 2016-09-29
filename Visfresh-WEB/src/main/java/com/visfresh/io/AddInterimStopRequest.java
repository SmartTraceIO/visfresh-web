/**
 *
 */
package com.visfresh.io;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AddInterimStopRequest {
    private Long shipmentId;
    private Long locationId;
    private double latitude;
    private double longitude;
    private Date date;
    private int time;

    /**
     * Default constructor.
     */
    public AddInterimStopRequest() {
        super();
    }

    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the locationId
     */
    public Long getLocationId() {
        return locationId;
    }
    /**
     * @param locationId the locationId to set
     */
    public void setLocationId(final Long locationId) {
        this.locationId = locationId;
    }
    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final int time) {
        this.time = time;
    }
}
