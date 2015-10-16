/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CompanyDaoImpl extends DaoImplBase<Company, Long> implements CompanyDao {
    /**
     * Default constructor.
     */
    public CompanyDaoImpl() {
        super(Company.class);
    }
}
