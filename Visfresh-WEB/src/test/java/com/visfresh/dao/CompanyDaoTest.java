/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

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
    protected Company createCompany(final String name) {
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
        c.setDescription("Any Description");
        return c;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Company c) {
        assertEquals("JUnit company", c.getName());
        assertEquals("Any Description", c.getDescription());
    }
}
