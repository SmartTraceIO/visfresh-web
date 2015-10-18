/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="arrivals")
public class Arrival extends NotificationIssue {
    /**
     * Number of meters of arrival
     */
    @Column(name = "nummeters")
    private int numberOfMetersOfArrival;
    /**
     * Device.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
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
