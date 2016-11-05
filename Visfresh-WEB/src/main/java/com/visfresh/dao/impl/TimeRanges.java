/**
 *
 */
package com.visfresh.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TimeRanges {
    private long startTime = -1;
    private long endTime;

    /**
     * Default constructor.
     */
    public TimeRanges() {
        super();
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
    public boolean contains(final Date date) {
        return date == null ? false : contains(date.getTime());
    }
    public boolean contains(final long time) {
        return time >= startTime && time <= endTime;
    }
    /**
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(final long endTime) {
        this.endTime = endTime;
    }
    public void addTime(final long time) {
        if (startTime == -1) {
            startTime = time;
            endTime = time;
        } else {
            startTime = Math.min(startTime, time);
            endTime = Math.max(endTime, time);
        }
    }
    public long getTotalTime() {
        return endTime - startTime;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TimeRanges)) {
            return false;
        }

        final TimeRanges other = (TimeRanges) obj;
        return startTime == other.startTime && endTime == other.endTime;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final long v = startTime + endTime;
        return (int)(v ^ (v >>> 32));
    }

    /**
     * @param endMonth
     * @return
     */
    public boolean after(final TimeRanges endMonth) {
        return this.startTime > endMonth.endTime;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'mm:ss:SSS");

        final StringBuilder sb = new StringBuilder();
        if (getStartTime() > 0) {
            sb.append(fmt.format(new Date(getStartTime())));
        }
        sb.append("<->");
        sb.append(fmt.format(new Date(getEndTime())));
        return sb.toString();
    }
}
