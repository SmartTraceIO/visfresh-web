/**
 *
 */
package com.visfresh.model;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Location {
    private Double latitude;
    private Double longitude;

    /**
     * Default constructor.
     */
    public Location() {
        super();
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     */
    public Location(final double lat, final double lon) {
        super();
        this.latitude = lat;
        this.longitude = lon;
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
    public void setLatitude(final double latitude) {
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
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "lat: " + getLatitude() + ", lon: " + getLongitude();
    }
}
