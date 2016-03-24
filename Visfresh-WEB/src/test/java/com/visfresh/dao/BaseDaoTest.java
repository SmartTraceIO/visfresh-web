/**
 *
 */
package com.visfresh.dao;

import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public abstract class BaseDaoTest<T> {
    /**
     * Spring context.
     */
    protected AbstractApplicationContext context;
    /**
     * DAO class.
     */
    private Class<T> clazz;
    /**
     * DAO for test.
     */
    protected T dao;
    /**
     * Company DAO.
     */
    protected CompanyDao companyDao;
    /**
     * Shared company.
     */
    protected Company sharedCompany;

    /**
     *
     */
    protected BaseDaoTest(final Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * @param ctxt spring context.
     */
    protected void initialize(final AbstractApplicationContext ctxt) {
        this.context = ctxt;
        dao = ctxt.getBean(clazz);
        companyDao = getContext().getBean(CompanyDao.class);

        this.sharedCompany = createCompany("Unit Test LLC");
    }

    /**
     * @return company.
     */
    protected Company createCompany(final String name) {
        final Company company = new Company();
        company.setName(name);
        return companyDao.save(company);
    }
    /**
     * @return the context
     */
    protected AbstractApplicationContext getContext() {
        return context;
    }
}
