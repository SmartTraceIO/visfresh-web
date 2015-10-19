/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

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
public abstract class BaseCrudTest<T extends DaoBase<E, ID>, E extends EntityWithId<ID>, ID extends Serializable> {
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
    protected T dao;
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
        final ID id = dao.save(e).getId();
        assertCreateTestEntityOk(dao.findOne(id));
    }
    /**
     * @param findOne
     */
    protected abstract void assertCreateTestEntityOk(E findOne);
    /**
     * Tests get all method.
     */
    @Test
    public void testGetAll() {
        final E e1 = createTestEntity();
        dao.save(e1);
        final E e2 = createTestEntity();
        dao.save(e2);
        assertTestGetAllOk(2, dao.findAll());
    }

    /**
     * @param numberOfCreatedEntities
     * @param all
     */
    protected void assertTestGetAllOk(final int numberOfCreatedEntities, final List<E> all) {
        assertEquals(numberOfCreatedEntities, all.size());
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
        dao.delete(e1);

        final List<E> all = dao.findAll();
        assertEquals(1, all.size());
        assertEquals(e2.getId(), all.get(0).getId());
    }
    @Test
    public void testUpdate() {
        final E e1 = createTestEntity();
        dao.save(e1);
        dao.save(e1);
        assertCreateTestEntityOk(e1);
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

    protected abstract E createTestEntity();

    /**
     * Deletes all created entities.
     */
    @After
    public void clear() {
        dao.deleteAll();
    }
//    @AfterClass
//    public static void afterClass() {
//        System.out.println("!!!!!");
//    }

    /**
     *
     */
    protected final void handleFinished() {
        if (companyDao != null) {
            companyDao.deleteAll();
        }
    }
}
