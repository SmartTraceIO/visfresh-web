/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="trackerevents")
public class TrackerEvent implements EntityWithId {
    /**
     * Event ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    private Long id;
    /**
     * Message type.
     */
    @Column
    @Enumerated
    private TrackerEventType type;
    /**
     * Time of creation.
     */
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;
    /**
     * Battery charge.
     */
    @Column
    private int battery;
    /**
     * Temperature
     */
    @Column
    private double temperature;
    /**
     * The device.
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
    public TrackerEvent() {
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
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the type
     */
    public TrackerEventType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final TrackerEventType type) {
        this.type = type;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }
    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDevice(final Device device) {
        this.device = device;
    }
}
