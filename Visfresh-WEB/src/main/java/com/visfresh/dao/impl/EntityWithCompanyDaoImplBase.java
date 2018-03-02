/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.EntityWithCompanyDaoBase;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class EntityWithCompanyDaoImplBase<
    V extends T,
    T extends EntityWithId<ID> & EntityWithCompany,
    ID extends Serializable & Comparable<ID>
    > extends DaoImplBase<V, T, ID> implements EntityWithCompanyDaoBase<V, T, ID> {

    /**
     * Default constructor.
     */
    public EntityWithCompanyDaoImplBase() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.EntityWithCompanyDaoBase#findByCompany(com.visfresh.entities.Company, com.visfresh.dao.Sorting, com.visfresh.dao.Page, com.visfresh.dao.Filter)
     */
    @Override
    public final List<V> findByCompany(final Long company, final Sorting sorting, final Page page,
            final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(getCompanyFieldName(), company);
        return findAll(f, sorting, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.EntityWithCompanyDaoBase#getEntityCount(com.visfresh.entities.Company, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Long company, final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(getCompanyFieldName(), company);
        return getEntityCount(f);
    }
    /**
     * @return
     */
    protected abstract String getCompanyFieldName();
}
