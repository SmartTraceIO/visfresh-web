/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "alerts")
@DiscriminatorColumn(name = "alerttype")
public abstract class AbstractAlert extends NotificationIssue {
    /**
     * Alert type.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType type;
    /**
     * Alert name.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Description.
     */
    @Column
    private String description;
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
     *
     */
    public AbstractAlert() {
        super();
    }

    /**
     * @return the type
     */
    public AlertType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final AlertType type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
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
