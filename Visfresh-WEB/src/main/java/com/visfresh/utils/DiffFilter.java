/**
 *
 */
package com.visfresh.utils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DiffFilter {
    /**
     * @param key key.
     * @return true if given key is accepted.
     */
    boolean accept(String key);
}
