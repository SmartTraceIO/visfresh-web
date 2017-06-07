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
public interface DaoBase<V extends T, T extends EntityWithId<ID>, ID extends Serializable & Comparable<ID>> {
    abstract <S extends T> S save(final S entity);
    <S extends T> Collection<S> save(final Collection<S> entities);
    V findOne(final ID id);
    List<V> findAll(Filter filter, Sorting sorting, Page page);
    List<V> findAll(final Collection<ID> ids);
    void delete(final ID id);
    void delete(final T entity);
    void delete(final Collection<? extends T> entities);
    void deleteAll();
    int getEntityCount(Filter filter);
}
