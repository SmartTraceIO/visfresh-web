/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="arrivals")
public class Arrival implements EntityWithId {
    /**
     * Entity ID.
     */
    @Id
    @GeneratedValue
    private Long id;
    /**
     * Number of meters of arrival
     */
    private int numberOfMetersOfArrival;
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
