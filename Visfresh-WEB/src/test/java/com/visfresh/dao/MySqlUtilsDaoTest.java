/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MySqlUtilsDaoTest extends BaseDaoTest<MySqlUtilsDao> {

    /**
     * Default constructor.
     */
    public MySqlUtilsDaoTest() {
        super(MySqlUtilsDao.class);
    }

    @Test
    public void testBuildCurrentProcessList() {
        final String processes = dao.getCurrentProcesses();
        System.out.println(processes);
        assertTrue(processes.length() > 0);
    }
}
