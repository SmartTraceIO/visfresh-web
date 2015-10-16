/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;

import com.visfresh.entities.Company;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public abstract class BaseCrudTest<T extends DaoBase<E, ID>, E extends EntityWithId, ID extends Serializable> {
    /**
     * Spring context.
     */
    private AbstractApplicationContext context;
    /**
     * DAO class.
     */
    private Class<T> clazz;
    /**
     * DAO for test.
     */
    private T dao;
    /**
     * Company DAO.
     */
    private CompanyDao companyDao;
    /**
     * Shared company.
     */
    protected Company sharedCompany;

    /**
     *
     */
    protected BaseCrudTest(final Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * Tests create element.
     */
    @Test
    public void testCreate() {
        final E e = createTestEntity();

        @SuppressWarnings("unchecked")
        final ID id = (ID) dao.save(e).getId();

        dao.getEntityManager().flush();

        assertNotNull(dao.findOne(id));
    }
    /**
     * Tests get all method.
     */
    @Test
    public void testGetAll() {
        final E e1 = createTestEntity();
        dao.save(e1);

        final E e2 = createTestEntity();
        dao.save(e2);

        dao.getEntityManager().flush();

        assertEquals(2, dao.findAll().size());
    }
    /**
     * Tests delete method.
     */
    @Test
    public void testDelete() {
        final E e1 = createTestEntity();
        dao.save(e1);

        final E e2 = createTestEntity();
        dao.save(e2);

        dao.getEntityManager().flush();
        dao.delete(e1);
        dao.getEntityManager().flush();

        final List<E> all = dao.findAll();
        assertEquals(1, all.size());
        assertEquals(e1.getId(), all.get(0).getId());
    }

    /**
     * @param ctxt spring context.
     */
    protected void initialize(final AbstractApplicationContext ctxt) {
        this.context = ctxt;
        dao = ctxt.getBean(clazz);
        companyDao = getContext().getBean(CompanyDao.class);

        this.sharedCompany = createSharedCompany();
    }

    /**
     * @return company.
     */
    protected Company createSharedCompany() {
        final Company company = new Company();
        company.setName("Unit Test LLC");
        return companyDao.save(company);
    }
    /**
     * @return the context
     */
    protected AbstractApplicationContext getContext() {
        return context;
    }

    protected abstract E createTestEntity();

    /**
     * Deletes all created entities.
     */
    @After
    public void clear() {
        dao.deleteAll();
    }

    /**
     *
     */
    protected final void destroy() {
        if (companyDao != null) {
            companyDao.deleteAll();
        }
    }
}
