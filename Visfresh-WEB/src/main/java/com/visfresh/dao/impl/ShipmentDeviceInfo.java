/**
 *
 */
package com.visfresh.dao.impl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentDeviceInfo {
    private int tripCount;

    /**
     * Default constructor.
     */
    public ShipmentDeviceInfo() {
        super();
    }

    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
}
