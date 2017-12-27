/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GroupLockDaoTest extends BaseDaoTest<GroupLockDao> {
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public GroupLockDaoTest() {
        super(GroupLockDao.class);
    }

    @Before
    public void setUp() {
        this.jdbc = context.getBean(NamedParameterJdbcTemplate.class);
    }
    @Test
    public void testLock() {
        final String lockKey = "Any lock key";
        final String device = "anydevice";
        final String otherDevice = "other device";

        assertTrue(dao.lock(device, lockKey));
        assertFalse(dao.lock(device, lockKey));
        assertTrue(dao.lock(otherDevice, lockKey));
    }
    @Test
    public void testUnlock() {
        final String lockKey = "Any lock key";
        final String device = "anydevice";

        assertTrue(dao.lock(device, lockKey));
        dao.unlock(device, lockKey);
        assertTrue(dao.lock(device, lockKey));
    }
    @Test
    public void testUpdateTime() {
        final String device = "device";
        final String lockKey = "anyLock";
        assertTrue(dao.lock(device, lockKey));

        final Date unlockOn = new Date(System.currentTimeMillis() + 10000000l);
        dao.setUnlockOn(device, lockKey, unlockOn);

        //update time to very old
        final Map<String, Object> params = new HashMap<>();
        params.put("device", device);
        params.put("lockKey", lockKey);
        final List<Map<String, Object>> rows = jdbc.queryForList("select unlockon from grouplocks"
                + " where `group` = :device and locker = :lockKey", params);

        assertTrue(Math.abs(((Date) rows.get(0).get("unlockon")).getTime() - unlockOn.getTime()) < 1000l);
    }
    @Test
    public void testSetUnlockTime() {

    }
    @Test
    public void testDeleteOldLocks() {
        assertTrue(dao.lock("d1", "l1"));
        assertTrue(dao.lock("d2", "l2"));

        //update time to very old
        final Map<String, Object> params = new HashMap<>();
        params.put("time", new Date(System.currentTimeMillis() - 10000000000l));
        jdbc.update("update grouplocks set unlockon = :time", params);

        //create new lock
        assertTrue(dao.lock("d3", "l3"));

        dao.unlockOlder(new Date(System.currentTimeMillis() - 100000000l));

        assertEquals(1, jdbc.queryForList("select * from grouplocks", new HashMap<>()).size());
    }
}
