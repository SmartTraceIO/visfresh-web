package com.visfresh.logs;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LogUnit {
    /**
     * The date.
     */
    private Date date;
    /**
     * The location.
     */
    private String location;
    /**
     * The log level.
     */
    private String level;
    /**
     * The message.
     */
    private String message;
    private byte[] rawData;

    /**
     * The constructor.
     */
    public LogUnit() {
        super();
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
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(final String level) {
        this.level = level;
    }

    /**
     * @param m the message.
     */
    public void setMessage(final String m) {
        this.message = m;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param rawData
     */
    public void setRawData(final byte[] rawData) {
        this.rawData = rawData;
    }
    /**
     * @return the rawData
     */
    public byte[] getRawData() {
        return rawData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getDate() + " " + getLevel() + " [" + getLocation() + "]"
            + getMessage();
    }

}
