/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;

import com.visfresh.entities.InterimStop;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopBean {
    private Long id;
    private LocationProfileBean location;
    private Date stopDate;
    private int time;

    /**
     * Default constructor.
     */
    public InterimStopBean() {
        super();
    }
    /**
     * @param stop interim stop.
     */
    public InterimStopBean(final InterimStop stop) {
        super();
        setId(stop.getId());
        if (stop.getLocation() != null) {
            setLocation(new LocationProfileBean(stop.getLocation()));
        }
        setStopDate(stop.getDate());
        setTime(stop.getTime());
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the location
     */
    public LocationProfileBean getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final LocationProfileBean location) {
        this.location = location;
    }
    /**
     * @return the stopDate
     */
    public Date getStopDate() {
        return stopDate;
    }
    /**
     * @param stopDate the stopDate to set
     */
    public void setStopDate(final Date stopDate) {
        this.stopDate = stopDate;
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
}
