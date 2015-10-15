/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DaoImplBase<T, ID extends Serializable> implements CrudRepository<T, ID> {
    private EntityManager em;
    private CrudRepository<T, ID> delegate;
    private final Class<T> domainClass;

    /**
     * @param repository class.
     */
    protected DaoImplBase(final Class<T> clazz) {
        super();
        this.domainClass = clazz;
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
     */
    @Override
    public <S extends T> S save(final S entity) {
        return delegate.save(entity);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
     */
    @Override
    public <S extends T> Iterable<S> save(final Iterable<S> entities) {
        return delegate.save(entities);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findOne(java.io.Serializable)
     */
    @Override
    public T findOne(final ID id) {
        return delegate.findOne(id);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#exists(java.io.Serializable)
     */
    @Override
    public boolean exists(final ID id) {
        return delegate.exists(id);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll()
     */
    @Override
    public Iterable<T> findAll() {
        return delegate.findAll();
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     */
    @Override
    public Iterable<T> findAll(final Iterable<ID> ids) {
        return delegate.findAll(ids);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#count()
     */
    @Override
    public long count() {
        return delegate.count();
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.io.Serializable)
     */
    @Override
    public void delete(final ID id) {
        delegate.delete(id);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    public void delete(final T entity) {
        delegate.delete(entity);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Iterable)
     */
    @Override
    public void delete(final Iterable<? extends T> entities) {
        delegate.delete(entities);
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteAll()
     */
    @Override
    public void deleteAll() {
        delegate.deleteAll();
    }

    @PersistenceContext
    public void setEntityManager(final EntityManager m) {
        this.em = m;
        this.delegate = new SimpleJpaRepository<T, ID>(domainClass, m);
    }
    /**
     * @return the entity manager.
     */
    public EntityManager getEntityManager() {
        return em;
    }
}
