/**
 *
 */
package com.visfresh.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DaoBase<E, ID extends Serializable> extends CrudRepository<E, ID> {
    /**
     * @return entity manager.
     */
    public EntityManager getEntityManager();
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    public List<E> findAll();
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     */
    @Override
    public List<E> findAll(Iterable<ID> ids);
}
