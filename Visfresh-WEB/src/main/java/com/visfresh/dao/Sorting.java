/**
 *
 */
package com.visfresh.dao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Sorting {
    private final String[] sortProperties;
    private boolean ascent = true;

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
        this.ascent = ascent;
        this.sortProperties = sortProps;
    }

    /**
     * @return true if the sort direction is ascent, false otherwise.
     */
    public boolean isAscentDirection() {
        return ascent;
    }
    /**
     * @return the sortProperties
     */
    public String[] getSortProperties() {
        return sortProperties;
    }
}
