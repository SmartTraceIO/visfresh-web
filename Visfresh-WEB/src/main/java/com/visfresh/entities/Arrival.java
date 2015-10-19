/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Arrival extends NotificationIssue {
    /**
     * Number of meters of arrival
     */
    private int numberOfMetersOfArrival;
    /**
     * Device.
     */
    private Device device;

    /**
     * Default constructor.
     */
    public Arrival() {
        super();
    }

    /**
     * @return the numberOfMetersOfArrival
     */
    public int getNumberOfMettersOfArrival() {
        return numberOfMetersOfArrival;
    }
    /**
     * @param numberOfMetersOfArrival the numberOfMetersOfArrival to set
     */
    public void setNumberOfMettersOfArrival(final int numberOfMetersOfArrival) {
        this.numberOfMetersOfArrival = numberOfMetersOfArrival;
    }
    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device
     */
    public void setDevice(final Device device) {
        this.device = device;
    }
}
