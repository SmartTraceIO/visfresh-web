/**
 *
 */
package com.visfresh.rules.correctmoving;

import java.util.Date;

import com.visfresh.entities.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LastLocationInfo {
    private Location lastLocation;
    private Date lastReadTime;

    /**
     * Default constructor.
     */
    public LastLocationInfo() {
        super();
    }

    /**
     * @return the lastLocation
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    /**
     * @param lastLocation the lastLocation to set
     */
    public void setLastLocation(final Location lastLocation) {
        this.lastLocation = lastLocation;
    }
    /**
     * @return the lastReadTime
     */
    public Date getLastReadTime() {
        return lastReadTime;
    }
    /**
     * @param lastReadTime the lastReadTime to set
     */
    public void setLastReadTime(final Date lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
}
