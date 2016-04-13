/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroup implements EntityWithId<Long>, EntityWithCompany {
    /**
     * Name of group which contains all devices.
     */
    public static final String ALL_DEVICES_GROUP_NAME = "AllDevices";

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
     * Group company.
     */
    private Company company;

    /**
     * Default constructor.
     */
    public DeviceGroup() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithCompany#getCompany()
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithCompany#setCompany(com.visfresh.entities.Company)
     */
    @Override
    public void setCompany(final Company c) {
        this.company = c;
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
