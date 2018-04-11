/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PairedPhone implements EntityWithId<Long> {
    private Long id;
    private Long company;
    private String beaconId;
    private String imei;
    private boolean active;
    private String description;

    /**
     * Default constructor.
     */
    public PairedPhone() {
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
    public String getBeaconId() {
        return beaconId;
    }
    /**
     * @param beacon the gateway to set
     */
    public void setBeaconId(final String beacon) {
        this.beaconId = beacon;
    }
    /**
     * @return the beacon
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the IMEI to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
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
