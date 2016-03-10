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
public abstract class DaoImplBase<T extends EntityWithId<ID>, ID extends Serializable&Comparable<ID>>
        implements DaoBase<T, ID> {
    protected static final String DEFAULT_FILTER_KEY_PREFIX = "filter_";

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
        if (ids.size() == 0) {
            return new LinkedList<T>();
        }

        final String[] keys = new String[ids.size()];
        final Iterator<ID> iter = ids.iterator();
        int i = 0;
        while(iter.hasNext()) {
            keys[i] = DEFAULT_FILTER_KEY_PREFIX + "_id_" + iter.next();
            i++;
        }

        final Filter f = new Filter();
        f.addFilter(DEFAULT_FILTER_KEY_PREFIX + ".getAllById",
                new SynteticFilter() {

                    @Override
                    public Object[] getValues() {
                        return ids.toArray();
                    }
                    @Override
                    public String[] getKeys() {
                        return keys;
                    }
                    @Override
                    public String getFilter() {
                        return getTableName() + "." + getIdFieldName()
                                + " in (:" + StringUtils.combine(getKeys(), ",:") + ")";
                    }
                });

        return findAll(f, null, null);
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
    protected String createInsertScript(final String tableName, final List<String> fields) {
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

    /**
     * @return
     */
    protected String buildSelectBlockForFindAll() {
        return "select * from " + getTableName();
    }
    /**
     * @param sorts
     * @param params
     * @param sorting
     */
    protected void addSortsForFindAll(final List<String> sorts, final Map<String, Object> params,
            final Sorting sorting) {
        final boolean isAscent = sorting.isAscentDirection();
        for (final String property : sorting.getSortProperties()) {
            addSortForFindAll(property, sorts, isAscent);
        }
    }

    /**
     * @param property property.
     * @param sorts sorts list.
     * @param isAscent is accent.
     */
    protected void addSortForFindAll(final String property,
            final List<String> sorts,
            final boolean isAscent) {
        final String field = getPropertyToDbMap().get(property);
        if (field != null) {
            addSortForDbField(field, sorts, isAscent);
        } else {
            addSortForDbField(property, sorts, isAscent);
        }
    }

    /**
     * @param field
     * @param sorts
     * @param isAscent
     */
    protected void addSortForDbField(final String field,
            final List<String> sorts, final boolean isAscent) {
        sorts.add(field + (isAscent ? " asc" : " desc"));
    }
    /**
     * @param filter
     * @param params
     * @param filters
     */
    protected void addFiltesForFindAll(final Filter filter, final Map<String, Object> params,
            final List<String> filters) {
        for (final String property : filter.getFilteredProperties()) {
            final Object value = filter.getFilter(property);
            addFilterValue(property, value, params, filters);
        }
    }
    /**
     * @param property property name.
     * @param value property value.
     * @param params parameter map.
     * @param filters filter segments.
     */
    protected void addFilterValue(final String property, final Object value,
            final Map<String, Object> params, final List<String> filters) {
        if (!(value instanceof SynteticFilter)) {
            final String key = DEFAULT_FILTER_KEY_PREFIX + property;

            String dbFieldName = getPropertyToDbMap().get(property);
            if (dbFieldName == null) {
                dbFieldName = property;
            }

            params.put(key, value);
            filters.add(getTableName() + "." + dbFieldName + "= :" + key);
        } else {
            final SynteticFilter sf = (SynteticFilter) value;
            final String[] keys = sf.getKeys();
            final Object[] values = sf.getValues();

            for (int i = 0; i < keys.length; i++) {
                params.put(keys[i], values[i]);
            }

            filters.add(sf.getFilter());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public T findOne(final ID id) {
        final Filter f = new Filter();
        f.addFilter(getIdFieldName(), id);

        final List<T> list = findAll(f, null, null);
        return list.size() == 0 ? null : list.get(0);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#getEntityCount(com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Filter filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final List<String> filters = new LinkedList<String>();

        if (filter != null) {
            addFiltesForFindAll(filter, params, filters);
        }

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select count(*) as count from "
                + getTableName()
                + (filters.size() == 0 ? "" : " where " + StringUtils.combine(filters, " and ")),
                params);
        return ((Number) list.get(0).get("count")).intValue();
    }
    /**
     * @return
     */
    protected abstract Map<String, String> getPropertyToDbMap();
    /**
     * @return
     */
    protected abstract String getTableName();
    /**
     * @return
     */
    protected abstract String getIdFieldName();
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<T> findAll(final Filter filter, final Sorting sorting, final Page page) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final List<String> filters = new LinkedList<String>();
        final List<String> sorts = new LinkedList<String>();

        final String selectAll = buildSelectBlockForFindAll();
        if (filter != null) {
            addFiltesForFindAll(filter, params, filters);
        }
        if (sorting != null) {
            addSortsForFindAll(sorts, params, sorting);
        }

        final List<Map<String, Object>> list = jdbc.queryForList(
                selectAll + (filters.size() == 0 ? "" : " where " + StringUtils.combine(filters, " and ")) + (sorts.size() == 0 ? "" : " order by " + StringUtils.combine(sorts, ",")) + (page == null ? "" : " limit "
                        + ((page.getPageNumber() - 1) * page.getPageSize())
                        + "," + page.getPageSize()),
                params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<T> result = new LinkedList<T>();
        for (final Map<String,Object> map : list) {
            final T t = createEntity(map);
            resolveReferences(t, map, cache);
            result.add(t);
        }
        return result;
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
    protected abstract T createEntity(Map<String, Object> map);

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public final void delete(final ID id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + getTableName() + " where " + getIdFieldName() + " = :id", paramMap);
    }
    /**
     * @param strings
     * @return
     */
    protected String combine(final String... strings) {
        return StringUtils.combine(strings, ",");
    }
}
