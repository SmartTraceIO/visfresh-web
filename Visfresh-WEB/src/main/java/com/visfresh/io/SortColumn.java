/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SortColumn {
    /**
     * Sort column name.
     */
    private String name;
    /**
     * Sort direction.
     */
    private boolean ascent = true;

    /**
     * Default constructor.
     */
    public SortColumn() {
        super();
    }
    /**
     * @param name column name.
     * @param ascent sorting direction.
     */
    public SortColumn(final String name, final boolean ascent) {
        super();
        this.name = name;
        this.ascent = ascent;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the ascent
     */
    public boolean isAscent() {
        return ascent;
    }
    /**
     * @param ascent the ascent to set
     */
    public void setAscent(final boolean ascent) {
        this.ascent = ascent;
    }
}
