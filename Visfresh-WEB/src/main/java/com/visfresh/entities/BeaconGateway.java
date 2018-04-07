/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BeaconGateway implements EntityWithId<Long> {
    private Long id;
    private Long company;
    private String gateway;
    private String beacon;
    private boolean active;
    private String description;

    /**
     * Default constructor.
     */
    public BeaconGateway() {
        super();
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
    /**
     * @return the company
     */
    public Long getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Long company) {
        this.company = company;
    }
    /**
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }
    /**
     * @param gateway the gateway to set
     */
    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }
    /**
     * @return the beacon
     */
    public String getBeacon() {
        return beacon;
    }
    /**
     * @param beacon the beacon to set
     */
    public void setBeacon(final String beacon) {
        this.beacon = beacon;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
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
