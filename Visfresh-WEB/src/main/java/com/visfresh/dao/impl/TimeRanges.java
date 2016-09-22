/**
 *
 */
package com.visfresh.dao.impl;

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
}
