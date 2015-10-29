/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockCompanyDao extends MockDaoBase<Company, Long> implements CompanyDao {
    /**
     * Default constructor.
     */
    public MockCompanyDao() {
        super();
    }
}
