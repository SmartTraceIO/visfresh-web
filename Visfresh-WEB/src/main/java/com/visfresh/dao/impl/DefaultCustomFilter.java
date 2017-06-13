/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultCustomFilter implements CustomFilter {
    private final Map<String, Object> values = new HashMap<>();
    private String filter;
    /**
     *
     */
    public DefaultCustomFilter() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.SynteticFilter#getKeys()
     */
    @Override
    public String[] getKeys() {
        final Set<String> keys = values.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.SynteticFilter#getValue(java.lang.String)
     */
    @Override
    public Object getValue(final String key) {
        return values.get(key);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.SynteticFilter#getFilter()
     */
    @Override
    public String getFilter() {
        return filter;
    }
    /**
     * @param filter the filter to set
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }
    public void addValue(final String key, final Object value) {
        values.put(key, value);
    }
}
