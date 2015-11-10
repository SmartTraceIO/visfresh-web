/**
 *
 */
package com.visfresh.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Filter {
    private final Map<String, Object> filters = new HashMap<String, Object>();

    /**
     * Default constructor.
     */
    public Filter() {
        super();
    }
    /**
     * Default constructor.
     */
    public Filter(final Filter f) {
        super();
        if (f != null) {
            filters.putAll(f.filters);
        }
    }

    /**
     * @param propertyName filtered property name.
     * @param value filtered property value.
     */
    public void addFilter(final String propertyName, final Object value) {
        filters.put(propertyName, value);
    }
    /**
     * @param propertyName property name.
     * @return value filter.
     */
    public Object getFilter(final String propertyName) {
        return filters.get(propertyName);
    }
    /**
     * @return filtered property names.
     */
    public Set<String> getFilteredProperties() {
        return new HashSet<String>(filters.keySet());
    }
}
