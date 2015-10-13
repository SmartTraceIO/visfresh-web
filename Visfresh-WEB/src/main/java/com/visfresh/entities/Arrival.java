/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Arrival implements EntityWithId {
    /**
     * Entity ID.
     */
    private Long id;
    /**
     * Number of meters of arrival
     */
    private int numberOfMettersOfArrival;
    /**
     * Date of occurrence.
     */
    private Date date;
    /**
     * Alert device.
     */
    private Device device;

    /**
     * Default constructor.
     */
    public Arrival() {
        super();
    }

    /**
     * @return the numberOfMettersOfArrival
     */
    public int getNumberOfMettersOfArrival() {
        return numberOfMettersOfArrival;
    }
    /**
     * @param numberOfMettersOfArrival the numberOfMettersOfArrival to set
     */
    public void setNumberOfMettersOfArrival(final int numberOfMettersOfArrival) {
        this.numberOfMettersOfArrival = numberOfMettersOfArrival;
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
    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
