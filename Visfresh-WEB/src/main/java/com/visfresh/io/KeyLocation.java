/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyLocation implements Comparable<KeyLocation> {
    private String key;
    private double latitude;
    private double longitude;
    private long time;
    private String description;
    private String prettyTime;

    /**
     * Default constructor.
     */
    public KeyLocation() {
        super();
    }

    /**
     * @return
     */
    public String getKey() {
        return key;
    }
    /**
     * @param key the key to set
     */
    public void setKey(final String key) {
        this.key = key;
    }
    /**
     * @return latitude.
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
     * @return longitude.
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
     * @return the time
     */
    public long getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final long time) {
        this.time = time;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final KeyLocation o) {
        return new Long(getTime()).compareTo(new Long(o.getTime()));
    }
    /**
     * @param desc
     */
    public void setDescription(final String desc) {
        this.description = desc;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @return the prettyTime
     */
    public String getPrettyTime() {
        return prettyTime;
    }
    /**
     * @param prettyTime the prettyTime to set
     */
    public void setPrettyTime(final String prettyTime) {
        this.prettyTime = prettyTime;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getKey())
            .append('(')
            .append(getLatitude())
            .append(',')
            .append(getLongitude())
            .append(')')
            .append(' ')
            .append(getPrettyTime())
            .append(": ")
            .append(getDescription());
        return sb.toString();
    }
}
