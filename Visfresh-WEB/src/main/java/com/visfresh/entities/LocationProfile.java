/**
 *
 */
package com.visfresh.entities;

import com.visfresh.io.shipment.LocationProfileBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProfile extends LocationProfileBean implements EntityWithId<Long>, EntityWithCompany {
    /**
     * Company
     */
    private Long company;

    /**
     * Default constructor.
     */
    public LocationProfile() {
        super();
    }

    /**
     * @return the company
     */
    @Override
    public Long getCompanyId() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Long company) {
        this.company = company;
    }
}
