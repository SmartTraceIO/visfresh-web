/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface LocationProfileDao extends
        DaoBase<LocationProfile, Long> {
    /**
     * @param company company.
     * @return location profiles.
     */
    List<LocationProfile> findByCompany(Company company);
}
