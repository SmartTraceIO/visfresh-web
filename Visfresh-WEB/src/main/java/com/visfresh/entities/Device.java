/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Device implements EntityWithId<String>, EntityWithCompany {
    /**
     * Device IMEI code
     */
    private String imei;
    /**
     * Device name.
     */
    private String name;
    /**
     * Device description
     */
    private String description;
    /**
     * Company
     */
    private Company company;
    /**
     * Current device trip count.
     */
    private int tripCount;
    /**
     * Active flag.
     */
    private boolean active = true;

    /**
     * Default constructor.
     */
    public Device() {
        super();
    }

    /**
     * @return the sn
     */
    public String getSn() {
        final String im = getImei();
        if (im == null) {
            return null;
        }

        final int len = im.length();
        return im.substring(len - 7, len - 1);
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
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
     * @return the id
     */
    @Override
    public String getId() {
        return getImei();
    }
    /**
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getImei() + " (" + getDescription() + ")";
    }
}
