/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class BaseCrudTest<T extends DaoBase<V, E, ID>, V extends E, E extends EntityWithId<ID>,
        ID extends Serializable & Comparable<ID>> extends BaseDaoTest<T> {
    /**
     *
     */
    protected BaseCrudTest(final Class<T> clazz) {
        super(clazz);
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
        assertTestGetAllOk(2, dao.findAll(null, null, null));
    }
    /**
     * Tests get all method.
     */
    @Test
    public void testGetAllById() {
        final E e1 = createTestEntity();
        dao.save(e1);
        final E e2 = createTestEntity();
        dao.save(e2);

        final List<ID> ids = new LinkedList<>();
        ids.add(e1.getId());
        assertTestGetAllOk(1, dao.findAll(ids));

        ids.add(e2.getId());
        assertTestGetAllOk(2, dao.findAll(ids));
    }

    /**
     * @param numberOfCreatedEntities
     * @param all
     */
    protected void assertTestGetAllOk(final int numberOfCreatedEntities, final List<V> all) {
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

        final List<V> all = dao.findAll(null, null, null);
        assertEquals(1, all.size());
        assertEquals(e2.getId(), all.get(0).getId());
    }
    @Test
    public void testUpdate() {
        final E e1 = createTestEntity();
        dao.save(e1);
        dao.save(e1);
        assertCreateTestEntityOk(dao.findOne(e1.getId()));
    }

    protected abstract E createTestEntity();
}
