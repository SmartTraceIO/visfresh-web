/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertDto {
    /**
     * Alert ID.
     */
    private Long id;
    /**
     * Alert type.
     */
    private AlertType type;
    /**
     * Alert description.
     */
    private String description;
    /**
     * Time ISO.
     */
    private String timeISO;
    /**
     * Time.
     */
    private String time;

    /**
     * Default constructor.
     */
    public AlertDto() {
        super();
    }

    /**
     * @return the id
     */
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
     * @return the timeISO
     */
    public String getTimeISO() {
        return timeISO;
    }
    /**
     * @param timeISO the timeISO to set
     */
    public void setTimeISO(final String timeISO) {
        this.timeISO = timeISO;
    }
    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final String time) {
        this.time = time;
    }
}
