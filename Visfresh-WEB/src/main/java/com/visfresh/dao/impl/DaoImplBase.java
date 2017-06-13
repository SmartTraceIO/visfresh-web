/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DaoBase;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithId;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class DaoImplBase<V extends T, T extends EntityWithId<ID>, ID extends Serializable&Comparable<ID>>
        implements DaoBase<V, T, ID> {
    protected int defaultCacheTimeSeconds = 3 * 60;

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    private EntityCache<ID> cache;

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
    @Override
    public final <S extends T> S save(final S entity) {
        final S s = saveImpl(entity);
        cache.remove(s.getId());
        return s;
    }
    public abstract <S extends T> S saveImpl(final S entity);

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll(java.util.Collection)
     */
    @Override
    public List<V> findAll(final Collection<ID> originIds) {
        final List<ID> ids = new LinkedList<>(originIds);
        final List<V> result = new LinkedList<>();

        //first of all attempt to get from cache;
        Iterator<ID> iter = ids.iterator();
        while (iter.hasNext()) {
            final ID id = iter.next();
            final V entity = getFromCache(id);
            if (entity != null) {
                result.add(entity);
                iter.remove();
            }
        }

        if (ids.size() == 0) {
            return result;
        }

        final DefaultCustomFilter idFilter = new DefaultCustomFilter();

        //get other from DB
        iter = ids.iterator();
        while(iter.hasNext()) {
            final ID id = iter.next();
            idFilter.addValue(SelectAllSupport.DEFAULT_FILTER_KEY_PREFIX + "_id_" + id, id);
        }

        idFilter.setFilter(getTableName() + "." + getIdFieldName()
            + " in (:" + StringUtils.combine(idFilter.getKeys(), ",:") + ")");

        final Filter f = new Filter();
        f.addFilter(SelectAllSupport.DEFAULT_FILTER_KEY_PREFIX + ".getAllById",
            idFilter);

        result.addAll(findAll(f, null, null));
        return result;
    }
    /* (non-Javadoc)
     * @see org.springframework.data.repository.CrudRepository#delete(java.lang.Object)
     */
    @Override
    public void delete(final T entity) {
        if (entity != null) {
            delete(entity.getId());
        }
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
    /**
     * @param tableName table name.
     * @param fields fields.
     * @param idFieldName ID field name.
     * @return insert.
     */
    protected static String createInsertScript(final String tableName, final List<String> fields) {
        final StringBuilder names = new StringBuilder();
        final StringBuilder values = new StringBuilder();

        boolean first = true;
        for (final String field : fields) {
            if (!first) {
                names.append(',');
                values.append(',');
            } else {
                first = false;
            }

            names.append(field);
            values.append(':').append(field);
        }

        return "insert into " + tableName + "(" + names + ") values (" + values + ")";
    }
    /**
     * @param tableName
     * @param fields
     * @param idField
     * @return
     */
    protected String createUpdateScript(final String tableName, final List<String> fields,
        final String idField) {
        final StringBuilder sb = new StringBuilder();
        for (final String field : fields) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(field).append("=:").append(field);
        }

        sb.insert(0, "update " + tableName + " set ");
        sb.append(" where " + idField + " =:").append(idField);
        return sb.toString();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public V findOne(final ID id) {
        final V e = getFromCache(id);
        if (e != null) {
            return e;
        }

        final Filter f = new Filter();
        f.addFilter(getIdFieldName(), id);

        final List<V> list = findAll(f, null, null);
        return list.size() == 0 ? null : list.get(0);
    }
    /**
     * @param id
     * @return
     */
    protected V getFromCache(final ID id) {
        final Map<String, Object> map = cache.get(id);
        if (map != null) {
            final V e = createEntity(map);
            resolveReferences(e, map, new HashMap<String, Object>());
            return e;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#getEntityCount(com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Filter filter) {
        final SelectAllSupport support = getSelectAllSupport();
        support.buildGetCount(filter);

        final List<Map<String, Object>> list = jdbc.queryForList(support.getQuery(), support.getParameters());
        return ((Number) list.get(0).get("count")).intValue();
    }
    /**
     * @return
     */
    protected abstract String getTableName();
    /**
     * @return
     */
    protected abstract String getIdFieldName();
    protected abstract EntityCache<ID> createCache();
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public final List<V> findAll(final Filter filter, final Sorting sorting, final Page page) {
        final SelectAllSupport support = getSelectAllSupport();
        support.buildSelectAll(filter, sorting, page);
        return findAll(support);
    }

    /**
     * @param support
     * @return
     */
    protected List<V> findAll(final SelectAllSupport support) {
        final String sql = support.getQuery();
        final List<Map<String, Object>> list = jdbc.queryForList(sql, support.getParameters());

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<V> result = new LinkedList<>();
        for (final Map<String,Object> map : list) {
            final V t = createEntity(map);
            resolveReferences(t, map, cache);
            //cache result
            cacheEntity(t, map);
            result.add(t);
        }
        return result;
    }
    /**
     * @return
     */
    protected SelectAllSupport getSelectAllSupport() {
        final SelectAllSupport support = createSelectAllSupport();
        customizeSupport(support);
        return support;
    }
    /**
     * @param support
     */
    protected void customizeSupport(final SelectAllSupport support) {
        support.addAliases(getPropertyToDbMap());
    }
    /**
     * @return
     */
    protected SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName());
    }
    /**
     * @return
     */
    protected abstract Map<String, String> getPropertyToDbMap();

    /**
     * @param t
     * @param map
     */
    protected void cacheEntity(final T t, final Map<String, Object> map) {
        this.cache.put(t.getId(), map);
    }
    /**
     * @param t
     * @param map
     * @param cache
     */
    protected abstract void resolveReferences(T t, Map<String, Object> map,
            Map<String, Object> cache);
    /**
     * @param map
     * @return
     */
    protected abstract V createEntity(Map<String, Object> map);

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public final void delete(final ID id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + getTableName() + " where " + getIdFieldName() + " = :id", paramMap);
        deleteFromCache(id);
    }
    public void deleteFromCache(final ID id) {
        cache.remove(id);
    }
    @PostConstruct
    public void initCache() {
        cache = createCache();
        cache.initialize();
    }
    @PreDestroy
    public void destroyCache() {
        if (cache != null) {
            cache.destroy();
        }
    }
    /**
     * @param strings
     * @return
     */
    protected String combine(final String... strings) {
        return StringUtils.combine(strings, ",");
    }
}
