/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="devicecommands")
public class DeviceCommand implements EntityWithId {
    /**
     * Command ID.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
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
     * Command to device
     */
    @Column(nullable = false)
    private String command;

    /**
     * Default constructor.
     */
    public DeviceCommand() {
        super();
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
    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command the command to set
     */
    public void setCommand(final String command) {
        this.command = command;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return id;
    }
}
