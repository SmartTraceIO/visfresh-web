/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SelectAllSupport {
    protected static final String DEFAULT_FILTER_KEY_PREFIX = "filter_";

    private Map<String, Object> params;
    private String query;
    private String tableName;
    private Map<String, String> aliases = new HashMap<>();
    private List<Filter> filters = new LinkedList<>();

    /**
     * @param tableName table name.
     */
    public SelectAllSupport(final String tableName) {
        this.tableName = tableName;
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
        final String field = aliases.get(property);
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
        if (!(value instanceof CustomFilter)) {
            final String key = DEFAULT_FILTER_KEY_PREFIX + property;

            String dbFieldName = aliases.get(property);
            if (dbFieldName == null) {
                dbFieldName = property;
            }

            params.put(key, value);
            filters.add(getTableName() + "." + dbFieldName + "= :" + key);
        } else {
            final CustomFilter sf = (CustomFilter) value;
            final String[] keys = sf.getKeys();
            for (int i = 0; i < keys.length; i++) {
                params.put(keys[i], sf.getValue(keys[i]));
            }

            filters.add(sf.getFilter());
        }
    }

    /**
     * @param filter the filter.
     * @return
     */
    protected String buildSelectBlockForEntityCount(final Filter filter) {
        return "select count(*) as count from " + getTableName();
    }

    /**
     * @param filter the filter.
     * @return select all string depending of filter.
     */
    protected String buildSelectBlockForFindAll(final Filter filter) {
        return "select * from " + getTableName();
    }
    /**
     *
     */
    public void buildSelectAll(final Filter originFilter, final Sorting sorting, final Page page) {
        final Filter filter = buildFilter(originFilter);
        final String selectAll = buildSelectBlockForFindAll(filter);
        buildSelectAll(selectAll, sorting, page, filter);
    }
    /**
     * @param selectAll
     * @param sorting
     * @param page
     * @param filter
     */
    protected void buildSelectAll(final String selectAll, final Sorting sorting, final Page page, final Filter filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final List<String> filters = new LinkedList<String>();
        final List<String> sorts = new LinkedList<String>();

        if (filter != null) {
            addFiltesForFindAll(filter, params, filters);
        }
        if (sorting != null) {
            addSortsForFindAll(sorts, params, sorting);
        }

        this.params = params;
        this.query = selectAll + (filters.size() == 0 ? "" : " where " + StringUtils.combine(filters, " and ")) + (sorts.size() == 0 ? "" : " order by " + StringUtils.combine(sorts, ",")) + (page == null ? "" : " limit "
                + ((page.getPageNumber() - 1) * page.getPageSize())
                + "," + page.getPageSize());
    }
    /**
     * @param filter
     */
    public void buildGetCount(final Filter originFilter) {
        final Filter filter = buildFilter(originFilter);

        final Map<String, Object> params = new HashMap<String, Object>();
        final List<String> filters = new LinkedList<String>();

        if (filter != null) {
            addFiltesForFindAll(filter, params, filters);
        }

        this.params = params;
        this.query = buildSelectBlockForEntityCount(filter)
                + (filters.size() == 0 ? "" : " where " + StringUtils.combine(filters, " and "));
    }
    /**
     * @param originFilter
     * @return
     */
    protected Filter buildFilter(final Filter originFilter) {
        if (originFilter == null && filters.isEmpty()) {
            return null;
        }

        //create composite filter.
        final Filter f = new Filter(originFilter);
        for (final Filter filter : filters) {
            for (final String prop : filter.getFilteredProperties()) {
                f.addFilter(prop, filter.getFilter(prop));
            }
        }

        return f;
    }
    /**
     * @return the params
     */
    public Map<String, Object> getParameters() {
        return params;
    }
    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }
    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }
    /**
     * @param propertyToDbMap
     */
    public void addAliases(final Map<String, String> propertyToDbMap) {
        aliases.putAll(propertyToDbMap);
    }
    public void addFilter(final Filter f) {
        filters.add(f);
    }
}
