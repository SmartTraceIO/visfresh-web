/**
 *
 */
package com.visfresh.dao;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class BaseDaoTest<T> extends BaseDbTest {
    /**
     * DAO class.
     */
    private final Class<T> clazz;
    /**
     * DAO for test.
     */
    protected T dao;
    /**
     *
     */
    protected BaseDaoTest(final Class<T> clazz) {
        super();
        this.clazz = clazz;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseDbTest#initialize(org.springframework.context.support.AbstractApplicationContext)
     */
    @Override
    protected void initialize(final AbstractApplicationContext ctxt) {
        super.initialize(ctxt);
        dao = ctxt.getBean(clazz);
    }
}
