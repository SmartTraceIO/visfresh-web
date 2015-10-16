/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyDaoTest extends BaseCrudTest<CompanyDao, Company, Long> {
    /**
     * Default constructor.
     */
    public CompanyDaoTest() {
        super(CompanyDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createSharedCompany()
     */
    @Override
    protected Company createSharedCompany() {
        //disable creating of shared company.
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Company createTestEntity() {
        final Company c = new Company();
        c.setName("JUnit company");
        return c;
    }
}
