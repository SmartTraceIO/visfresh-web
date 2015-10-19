/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Alert extends NotificationIssue {
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
     * Device.
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
