/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Embeddable;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Embeddable
public class Location {
    private double latitude;
    private double longitude;

    /**
     * Default constructor.
     */
    public Location() {
        super();
    }

    /**
     * @return the x
     */
    public double getLatitude() {
        return latitude;
    }
    /**
     * @param lat the latitude to set.
     */
    public void setLatitude(final double lat) {
        this.latitude = lat;
    }
    /**
     * @return the y
     */
    public double getLongitude() {
        return longitude;
    }
    /**
     * @param lon the longitude to set.
     */
    public void setLongitude(final double lon) {
        this.longitude = lon;
    }
}
