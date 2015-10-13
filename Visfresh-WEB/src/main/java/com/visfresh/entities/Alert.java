/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Alert implements EntityWithId {
    /**
     * Alert ID.
     */
    private Long id;
    /**
     * Alert type.
     */
    private AlertType type;
    /**
     * Alert name.
     */
    private String name;
    /**
     * Description.
     */
    private String description;
    /**
     * Date of occurrence.
     */
    private Date date;
    /**
     * Allert device.
     */
    private Device device;

    /**
     * Default constructor.
     */
    public Alert() {
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
