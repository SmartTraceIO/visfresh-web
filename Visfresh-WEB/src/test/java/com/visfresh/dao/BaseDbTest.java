/**
 *
 */
package com.visfresh.dao;

import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 */
@RunWith(DbTestRunner.class)
public abstract class BaseDbTest {

    /**
     * Spring context.
     */
    protected AbstractApplicationContext context;
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
    public BaseDbTest() {
        super();
    }

    /**
     * @param ctxt spring context.
     */
    protected void initialize(final AbstractApplicationContext ctxt) {
        this.context = ctxt;
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