/**
 *
 */
package com.visfresh.lists;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListResult<E> {
    private final List<E> items = new LinkedList<>();
    private int totalCount;

    /**
     * Default constructor.
     */
    public ListResult() {
        super();
    }

    /**
     * @return the items
     */
    public List<E> getItems() {
        return items;
    }
    /**
     * @param totalCount the totalCount to set
     */
    public void setTotalCount(final int totalCount) {
        this.totalCount = totalCount;
    }
    /**
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }
}
