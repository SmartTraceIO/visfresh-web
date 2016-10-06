/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStop implements EntityWithId<Long> {
    private Long id;
    private LocationProfile location;
    private Date date;
    private int time;

    /**
     * Default constructor.
     */
    public InterimStop() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return id;
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
     * @return the time
     */
    public int getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final int time) {
        this.time = time;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
