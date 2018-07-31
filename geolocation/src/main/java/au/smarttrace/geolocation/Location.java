/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Location {
    private double latitude;
    private double longitude;
    private double altitude;

    public Location() {
        super();
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
     * @return the altitude
     */
    public double getAltitude() {
        return altitude;
    }
    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(final double altitude) {
        this.altitude = altitude;
    }
}
