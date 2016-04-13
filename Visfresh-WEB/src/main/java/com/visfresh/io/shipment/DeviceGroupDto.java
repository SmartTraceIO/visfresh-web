/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.DeviceGroup;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupDto {
    /**
     * Group Name
     */
    private String name;
    /**
     * Group description.
     */
    private String description;

    /**
     * Default constructor.
     */
    public DeviceGroupDto() {
        super();
    }
    /**
     * @param g device group.
     */
    public DeviceGroupDto(final DeviceGroup g) {
        super();
        setName(g.getName());
        setDescription(g.getDescription());
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
}
