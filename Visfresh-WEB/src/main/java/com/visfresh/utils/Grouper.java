/**
 *
 */
package com.visfresh.utils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface Grouper<T> {
    /**
     * @param obj the object.
     * @return the group name for object.
     */
    String getGroup(T obj);
}
