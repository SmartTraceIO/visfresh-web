/**
 *
 */
package com.visfresh.dao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface MySqlUtilsDao {
    /**
     * @return list of current MySQL processes.
     */
    String getCurrentProcesses();
}
