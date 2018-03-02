/**
 *
 */
package com.visfresh.dao;

import java.io.Serializable;
import java.util.List;

import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 * @param <T> entity type.
 * @param <ID> entity ID type.
 */
public interface EntityWithCompanyDaoBase
        <
        V extends T,
        T extends EntityWithId<ID> & EntityWithCompany,
        ID extends Serializable & Comparable<ID>
        >
    extends DaoBase<V, T, ID>{
    /**
     * @param company company.
     * @param sorting sorting parameters.
     * @param page page.
     * @param filter filter object.
     * @return list of devices.
     */
    List<V> findByCompany(Long company, Sorting sorting, Page page, Filter filter);
    /**
     * @param company company.
     * @param filter filter.
     * @return count of entity for given filtering.
     */
    int getEntityCount(Long company, Filter filter);
}
