/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
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
@Component
public abstract class EntityWithCompanyDaoImplBase<
    T extends EntityWithId<ID> & EntityWithCompany,
    ID extends Serializable & Comparable<ID>
    > extends DaoImplBase<T, ID> implements EntityWithCompanyDaoBase<T, ID> {

    @Autowired
    private CompanyDao companyDao;
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
    public final List<T> findByCompany(final Company company, final Sorting sorting, final Page page,
            final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(getCompanyFieldName(), company.getId());
        return findAll(f, sorting, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.EntityWithCompanyDaoBase#getEntityCount(com.visfresh.entities.Company, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Company company, final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(getCompanyFieldName(), company.getId());
        return getEntityCount(f);
    }
    @Override
    protected void resolveReferences(final T t, final Map<String, Object> row,
            final Map<String, Object> cache) {
        final String id = ((Number) row.get(getCompanyFieldName())).toString();
        final String cacheKey = "company_" + id;
        Company company = (Company) cache.get(cacheKey);
        if (company == null) {
            company = companyDao.findOne(Long.valueOf(id));
            cache.put(cacheKey, company);
        }
        t.setCompany(company);
    }
    /**
     * @return
     */
    protected abstract String getCompanyFieldName();
    @Override
    public boolean clearCache(final Company c, final ID id) {
        if (id != null) {
            if (hasInCache(c, id)) {
                deleteFromCache(id);
                return true;
            }
        }
        return false;
    }
    /**
     * @param c company.
     * @param id entity ID.
     * @return true if entity by given ID is exist in cache.
     */
    @Override
    public boolean hasInCache(final Company c, final ID id) {
        final T e = getFromCache(id);
        if (e != null && e.getCompany() != null && e.getCompany().getId().equals(c.getId())) {
            return true;
        }
        return false;
    }
}
