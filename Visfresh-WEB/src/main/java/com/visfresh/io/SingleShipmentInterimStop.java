/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentInterimStop {
    private Long id;
    private LocationProfile location;
    private double latitude;
    private double longitude;
    private String stopDate;
    private String stopDateIso;
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
    public LocationProfile getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final LocationProfile location) {
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
    public String getStopDate() {
        return stopDate;
    }
    /**
     * @param stopDate the stopDate to set
     */
    public void setStopDate(final String stopDate) {
        this.stopDate = stopDate;
    }
    /**
     * @return the stopDateIso
     */
    public String getStopDateIso() {
        return stopDateIso;
    }
    /**
     * @param stopDateIso the stopDateIso to set
     */
    public void setStopDateIso(final String stopDateIso) {
        this.stopDateIso = stopDateIso;
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
