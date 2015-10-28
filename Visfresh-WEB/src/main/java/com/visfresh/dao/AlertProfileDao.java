/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlertProfileDao extends DaoBase<AlertProfile, Long> {
    /**
     * @param company company.
     * @return
     */
    List<AlertProfile> findByCompany(Company company);
}
