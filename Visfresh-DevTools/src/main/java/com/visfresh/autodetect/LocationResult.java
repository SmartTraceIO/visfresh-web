/**
 *
 */
package com.visfresh.autodetect;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.model.DeviceMessage;
import com.visfresh.model.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationResult {
    private LocationProfile location;
    private double minDistance = Double.MAX_VALUE;
    private final List<DeviceMessage> messagesInsideLocation = new LinkedList<>();
    private int minDinstance;

    /**
     * Default constructor.
     */
    public LocationResult() {
        super();
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
     * @return the minDistance
     */
    public double getMinDistance() {
        return minDistance;
    }
    /**
     * @param minDistance the minDistance to set
     */
    public void setMinDistance(final double minDistance) {
        this.minDistance = minDistance;
    }
    /**
     * @return the messagesInsideLocation
     */
    public List<DeviceMessage> getMessagesInsideLocation() {
        return messagesInsideLocation;
    }
    /**
     * @return the minDinstance
     */
    public int getMinDinstance() {
        return minDinstance;
    }
    /**
     * @param minDinstance the minDinstance to set
     */
    public void setMinDinstance(final int minDinstance) {
        this.minDinstance = minDinstance;
    }
}
