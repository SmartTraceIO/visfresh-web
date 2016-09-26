/**
 *
 */
package com.visfresh.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompareResult<T> {
    private final List<T> deleted = new LinkedList<>();
    private final List<T> added = new LinkedList<>();

    /**
     * Default constructor.
     */
    public CompareResult() {
        super();
    }

    /**
     * @return the added
     */
    public List<T> getAdded() {
        return added;
    }
    /**
     * @return the deleted
     */
    public List<T> getDeleted() {
        return deleted;
    }
}
