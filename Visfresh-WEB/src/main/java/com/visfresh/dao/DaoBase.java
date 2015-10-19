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
public interface DaoBase<T extends EntityWithId<ID>, ID extends Serializable> {
    public abstract <S extends T> S save(final S entity);
    public <S extends T> Collection<S> save(final Collection<S> entities);
    public T findOne(final ID id);
    public List<T> findAll();
    public List<T> findAll(final Collection<ID> ids);
    public void delete(final ID id);
    public void delete(final T entity);
    public void delete(final Collection<? extends T> entities);
    public void deleteAll();
}
