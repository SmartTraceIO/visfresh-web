/**
 *
 */
package com.visfresh.entities;

import com.visfresh.io.shipment.LocationProfileDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProfile extends LocationProfileDto implements EntityWithId<Long>, EntityWithCompany {
    /**
     * Company
     */
    private Company company;

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
}
