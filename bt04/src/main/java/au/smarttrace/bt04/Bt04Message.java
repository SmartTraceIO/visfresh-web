/**
 *
 */
package au.smarttrace.bt04;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Bt04Message {
    private String imei;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double accuracy;
    private String rawData;

    private final List<Beacon> beacons = new LinkedList<>();
    private Date time;

    /**
     * Default constructor.
     */
    public Bt04Message() {
        super();
    }

    /**
     * @return the beacons
     */
    public List<Beacon> getBeacons() {
        return beacons;
    }

    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
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
     * @return the altitude
     */
    public Double getAltitude() {
        return altitude;
    }
    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(final Double altitude) {
        this.altitude = altitude;
    }
    /**
     * @return the accuracy
     */
    public Double getAccuracy() {
        return accuracy;
    }
    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(final Double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * @param t time to set
     */
    public void setTime(final Date t) {
        this.time = t;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return the rawData
     */
    public String getRawData() {
        return rawData;
    }
    /**
     * @param rawData the rawData to set
     */
    public void setRawData(final String rawData) {
        this.rawData = rawData;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getRawData();
    }
}
