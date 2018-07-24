/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GpsData {
    // -90.0 ~ 90.0 degree --- Signed 32 bits integer from -162000000 to 162000000 (in 1/500")
    private long latitude;
    // -180.0 ~ 180.0 degree --- Signed 32 bits integer from -324000000 to 324000000 (in 1/500")
    private long longitude;
    // Altitude 2 Signed 16 bits integer from -32768 to 32767 (in meters)
    private long altitude;
    // Speed 2 Unsigned 16 bits integer (in km/h)
    private int speed;
    //Course 2 Unsigned 16 bits integer from 0 to 360 (in degrees)
    private int course;
    //Satellites 1 The number of satellites
    private int satellites;

    /**
     * Default constructor.
     */
    public GpsData() {
        super();
    }

    /**
     * @return the latitude
     */
    public long getLatitude() {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final long latitude) {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public long getLongitude() {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final long longitude) {
        this.longitude = longitude;
    }
    /**
     * @return the altitude
     */
    public long getAltitude() {
        return altitude;
    }
    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(final long altitude) {
        this.altitude = altitude;
    }
    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }
    /**
     * @param speed the speed to set
     */
    public void setSpeed(final int speed) {
        this.speed = speed;
    }
    /**
     * @return the course
     */
    public int getCourse() {
        return course;
    }
    /**
     * @param course the course to set
     */
    public void setCourse(final int course) {
        this.course = course;
    }
    /**
     * @return the satellites
     */
    public int getSatellites() {
        return satellites;
    }
    /**
     * @param satellites the satellites to set
     */
    public void setSatellites(final int satellites) {
        this.satellites = satellites;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Lat: " + getLatitude());
        sb.append(", Lon: " + getLongitude());
        sb.append(", Alt: " + getAltitude());
        sb.append(", Speed: " + getSpeed());
        sb.append(", Cource: " + getCourse());
        sb.append(", Satelites: " + getSatellites());
        return sb.toString();
    }
}
