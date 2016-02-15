/**
 *
 */
package com.visfresh.dao.impl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SynteticFilter {
    String[] getKeys();
    Object[] getValues();
    String getFilter();
}
