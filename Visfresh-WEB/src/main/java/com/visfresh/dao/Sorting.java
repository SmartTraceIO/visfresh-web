/**
 *
 */
package com.visfresh.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Sorting {
    private final List<String> sortProperties = new LinkedList<>();
    private final Map<String, Boolean> ascents = new HashMap<>();

    /**
     * @param sortProps sorting properties.
     */
    public Sorting(final String... sortProps) {
        this(true, sortProps);
    }
    /**
     * @param ascent ascent sort direction.
     * @param sortProps sorting properties.
     */
    public Sorting(final boolean ascent, final String... sortProps) {
        super();
        for (final String prop : sortProps) {
            addSortProperty(prop, ascent);
        }
    }

    /**
     * @param prop property name.
     * @param ascent ascent.
     */
    public void addSortProperty(final String prop) {
        addSortProperty(prop, true);
    }
    /**
     * @param prop property name.
     * @param ascent ascent.
     */
    public void addSortProperty(final String prop, final boolean ascent) {
        sortProperties.add(prop);
        ascents.put(prop, ascent);
    }
    /**
     * @param prop property name.
     * @param ascent ascent.
     */
    public void removeSortProperty(final String prop) {
        final Iterator<String> iter = sortProperties.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(prop)) {
                iter.remove();
            }
        }

        ascents.remove(prop);
    }
    /**
     * @return true if the sort direction is ascent, false otherwise.
     */
    public boolean isAscentDirection(final String propName) {
        return !Boolean.FALSE.equals(ascents.get(propName));
    }
    /**
     * @return the sortProperties
     */
    public String[] getSortProperties() {
        return sortProperties.toArray(new String[sortProperties.size()]);
    }
}
