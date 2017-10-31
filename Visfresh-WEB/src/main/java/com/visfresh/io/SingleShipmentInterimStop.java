/**
 *
 */
package com.visfresh.io;

import java.util.Date;

import com.visfresh.io.shipment.LocationProfileBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentInterimStop {
    private Long id;
    private LocationProfileBean location;
    private double latitude;
    private double longitude;
    private Date stopDate;
    private int time;

    /**
     * Default constructor.
     */
    public SingleShipmentInterimStop() {
        super();
    }

    /**
     * @return the id
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
     * @return the location
     */
    public LocationProfileBean getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final LocationProfileBean location) {
        this.location = location;
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
     * @return the stopDate
     */
    public Date getStopDate() {
        return stopDate;
    }
    /**
     * @param stopDate the stopDate to set
     */
    public void setStopDate(final Date stopDate) {
        this.stopDate = stopDate;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final int time) {
        this.time = time;
    }
    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }
}
