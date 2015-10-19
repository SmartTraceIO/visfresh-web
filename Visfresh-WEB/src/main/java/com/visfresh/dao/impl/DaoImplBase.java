/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DaoBase;
import com.visfresh.entities.EntityWithId;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class DaoImplBase<T extends EntityWithId<ID>, ID extends Serializable> implements DaoBase<T, ID> {
    /**
     * JDBC template.
     */
    @Autowired(required = true)
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * @param repository class.
     */
    protected DaoImplBase() {
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
    /**
     * @param fields
     * @return
     */
    protected String buildSelectAs(final Map<String, String> fields) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> e : fields.entrySet()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e.getKey() + " as " + e.getValue());
        }
        return sb.toString();
    }

    /**
     * @param strings
     * @return
     */
    protected String combine(final String... strings) {
        return StringUtils.combine(strings, ",");
    }
}
