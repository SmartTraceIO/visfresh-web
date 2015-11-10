/**
 *
 */
package com.visfresh.dao.mock;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.dao.EntityWithCompanyDaoBase;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Company;
import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class MockEntityWithCompanyDaoBase<
    T extends EntityWithId<ID> & EntityWithCompany,
    ID extends Serializable & Comparable<ID>
    > extends MockDaoBase<T, ID> implements EntityWithCompanyDaoBase<T, ID> {

    /**
     * Default constructor.
     */
    public MockEntityWithCompanyDaoBase() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.EntityWithCompanyDaoBase#findByCompany(com.visfresh.entities.Company, com.visfresh.dao.Sorting, com.visfresh.dao.Page, com.visfresh.dao.Filter)
     */
    @Override
    public final List<T> findByCompany(final Company company, final Sorting sorting, final Page page,
            final Filter filter) {
        final List<T> result = getFiltered(filter, sorting, company);
        return page == null ? result : getPage(result, page);
    }

    /**
     * @param filter
     * @param company
     * @return
     */
    private List<T> getFiltered(final Filter filter, final Sorting sorting, final Company company) {
        final List<T> result = new LinkedList<T>();
        for (final T t : findAll(filter, sorting, null)) {
            if (company.getId().equals(t.getCompany().getId())) {
                result.add(t);
            }
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.EntityWithCompanyDaoBase#getEntityCount(com.visfresh.entities.Company, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Company company, final Filter filter) {
        return getFiltered(filter, null, company).size();
    }
}
