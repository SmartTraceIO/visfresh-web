/**
 *
 */
package com.visfresh.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DaoBase<T extends EntityWithId<ID>, ID extends Serializable & Comparable<ID>> {
    abstract <S extends T> S save(final S entity);
    <S extends T> Collection<S> save(final Collection<S> entities);
    T findOne(final ID id);
    List<T> findAll(Filter filter, Sorting sorting, Page page);
    List<T> findAll(final Collection<ID> ids);
    void delete(final ID id);
    void delete(final T entity);
    void delete(final Collection<? extends T> entities);
    void deleteAll();
    int getEntityCount(Filter filter);
}
