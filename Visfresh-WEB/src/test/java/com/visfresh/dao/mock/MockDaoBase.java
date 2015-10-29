/**
 *
 */
package com.visfresh.dao.mock;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DaoBase;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDaoBase<T extends EntityWithId<ID>, ID extends Serializable&Comparable<ID>>
        implements DaoBase<T, ID> {
    private static final AtomicLong idGen = new AtomicLong();

    protected final Map<ID, T> entities = new ConcurrentHashMap<ID, T>();
    /**
     * @param repository class.
     */
    protected MockDaoBase() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
     */
    @Override
    public <S extends T> Collection<S> save(final Collection<S> entities) {
        final List<S> result = new LinkedList<S>();
        for (final S e : entities) {
            result.add(save(e));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll(java.util.Collection)
     */
    @Override
    public List<T> findAll(final Collection<ID> ids) {
        final List<T> result = new LinkedList<T>();
        for (final ID id : ids) {
            result.add(findOne(id));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    public void delete(final T entity) {
        delete(entity.getId());
    }
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Iterable)
     */
    @Override
    public void delete(final Collection<? extends T> entities) {
        for (final T t : entities) {
            delete(t);
        }
    }
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#deleteAll()
     */
    @Override
    public void deleteAll() {
        delete(findAll());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final ID id) {
        entities.remove(id);

    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends T> S save(final S entity) {
        if (entity.getId() == null) {
            setNewEntityId(entity);
        }
        entities.put(entity.getId(), entity);
        return entity;
    }
    private void setNewEntityId(final T t) {
        final Long id = generateId();
        try {
            final Method m = t.getClass().getMethod("setId", Long.class);
            m.invoke(t, id);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @return
     */
    protected long generateId() {
        return idGen.incrementAndGet();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public T findOne(final ID id) {
        return entities.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<T> findAll() {
        return new LinkedList<T>(entities.values());
    }
    protected List<T> orderById(final List<T> list, final boolean ascent) {
        Collections.sort(list, new Comparator<T>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final T o1, final T o2) {
                if (ascent) {
                    return o1.getId().compareTo(o2.getId());
                }
                return o2.getId().compareTo(o1.getId());
            }
        });
        return list;
    }
}
