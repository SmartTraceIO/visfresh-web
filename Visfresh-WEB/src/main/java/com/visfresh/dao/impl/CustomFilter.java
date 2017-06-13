/**
 *
 */
package com.visfresh.dao.impl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface CustomFilter {
    String[] getKeys();
    Object getValue(String key);
    String getFilter();
}
