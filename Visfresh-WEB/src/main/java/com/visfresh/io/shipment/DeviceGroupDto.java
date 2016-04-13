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
     * Group ID
     */
    private Long id;
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
        setId(g.getId());
        setName(g.getName());
        setDescription(g.getDescription());
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
