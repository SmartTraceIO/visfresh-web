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
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class MockDaoBase<T extends EntityWithId<ID>, ID extends Serializable&Comparable<ID>>
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
        delete(findAll(null, null, null));
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
    public List<T> findAll(final Filter filter, final Sorting sorting, final Page page) {
        //get all items filtered.
        final List<T> result = getAllFiltered(filter);

        //sort result
        if (sorting != null) {
            Collections.sort(result, new Comparator<T>() {
                /* (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(final T o1, final T o2) {
                    for (final String property : sorting.getSortProperties()) {
                        final Object v1 = getValueForFilterOrCompare(property, o1);
                        final Object v2 = getValueForFilterOrCompare(property, o2);

                        final int result = compareObjects(v1, v2);
                        if (result != 0) {
                            return sorting.isAscentDirection() ? result : -result;
                        }
                    }
                    return 0;
                }
            });
        }

        return page == null ? result : getPage(result, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#getEntityCount(com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Filter filter) {
        return getAllFiltered(filter).size();
    }

    /**
     * @param filter
     * @return
     */
    private List<T> getAllFiltered(final Filter filter) {
        final List<T> result = new LinkedList<T>();
        if (filter != null) {
            for (final T t : new LinkedList<T>(entities.values())) {
                if (filter == null || accept(t, filter)) {
                    result.add(t);
                }
            }
        } else {
            result.addAll(entities.values());
        }
        return result;
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

    /**
     * Clears the DAO.
     */
    public void clear() {
        entities.clear();
    }
    /**
     * @param t entity.
     * @param filter filter.
     * @return true if accepted.
     */
    private boolean accept(final T t, final Filter filter) {
        for (final String propertyName : filter.getFilteredProperties()) {
            if (!acceptFilter(propertyName, filter.getFilter(propertyName), getValueForFilterOrCompare(propertyName, t))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param propertyName property name.
     * @param expected expected value.
     * @param realValue real value.
     * @return true if accepts filter.
     */
    protected boolean acceptFilter(final String propertyName, final Object expected, final Object realValue) {
        if (expected == null && realValue == null) {
            return true;
        }
        if (expected == null || realValue == null) {
            return false;
        }
        if (realValue instanceof EntityWithId<?>) {
            final Serializable id1 = ((EntityWithId<?>) realValue).getId();
            if (expected instanceof EntityWithId<?>) {
                final Serializable id2 = ((EntityWithId<?>) expected).getId();
                return id1.equals(id2);
            } else if (id1.equals(expected)) {
                // possible equals ID.
                return true;
            }
        }

        return expected.equals(realValue);
    }

    /**
     * @param v1
     * @param v2
     * @return
     */
    private int compareObjects(final Object v1, final Object v2) {
        final int result = comparePossibleNull(v1, v2);
        if (result != 0) {
            return result;
        }

        if (v1 instanceof Comparable<?>) {
            final Method m = findCompareToMethod(v1.getClass());
            try {
                return (Integer) m.invoke(v1, v2);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }

    /**
     * @param klass
     * @return
     */
    private Method findCompareToMethod(final Class<?> klass) {
        Class<?> c = klass;

        do {
            if (c == null || c == Object.class) {
                throw new IllegalArgumentException();
            }

            final Method[] methods = c.getDeclaredMethods();
            for (final Method m : methods) {
                //check method signature
                if (!m.isAccessible() && !"compareTo".equals(m.getName())) {
                    continue;
                }
                //check number of parameters.
                final Class<?>[] types = m.getParameterTypes();
                if (types.length != 1) {
                    continue;
                }
                //check compatible type.
                if (klass.isAssignableFrom(types[0])) {
                    return m;
                }
            }

            c = c.getSuperclass();
        } while (true);
    }

    /**
     * @param property property name.
     * @param t entity.
     * @return property value.
     */
    protected abstract Object getValueForFilterOrCompare(String property, T t);

    /**
     * @param list
     * @param page page.
     * @return
     */
    protected List<T> getPage(final List<T> list, final Page page) {
        if (page == null) {
            return new LinkedList<T>(list);
        }

        final int pageIndex = page.getPageNumber();
        final int pageSize = page.getPageSize();

        final int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= list.size()) {
            return new LinkedList<T>();
        }

        final int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Sorts the list of entity.
     * @param list origin list.
     */
    protected void sortById(final List<T> list, final boolean ascent) {
        //sort first of all
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
    }
    /**
     * @param o1
     * @param o2
     * @param ascent
     * @return
     */
    private int comparePossibleNull(final Object o1, final Object o2) {
        if (o1 != null && o2 == null) {
            return 1;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        return 0;
    }
}
