/**
 *
 */
package com.visfresh.rules.state;

import java.util.Date;

import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LeaveLocationState {
    private Long locationId;
    private double latitude;
    private double longitude;
    private String name ;
    private Date leaveOn;
    private int radius;

    /**
     * Default constructor.
     */
    public LeaveLocationState() {
        super();
    }
    /**
     * @param loc origin location.
     */
    public LeaveLocationState(final LocationProfile loc) {
        super();
        setLatitude(loc.getLocation().getLatitude());
        setLongitude(loc.getLocation().getLongitude());
        setLocationId(loc.getId());
        setName(loc.getName());
        setRadius(loc.getRadius());
    }

    public Long getLocationId() {
        return locationId;
    }
    public void setLocationId(final Long locationId) {
        this.locationId = locationId;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }
    public Date getLeaveOn() {
        return leaveOn;
    }
    public void setLeaveOn(final Date leaveOn) {
        this.leaveOn = leaveOn;
    }
    public int getRadius() {
        return radius;
    }
    public void setRadius(final int radius) {
        this.radius = radius;
    }
    public boolean isLeave() {
        return getLeaveOn() != null;
    }
}
