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

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceLockDaoTest extends BaseDaoTest<DeviceLockDao> {
    private NamedParameterJdbcTemplate jdbc;
    private SystemMessageDao systemMessageDao;

    /**
     * Default constructor.
     */
    public DeviceLockDaoTest() {
        super(DeviceLockDao.class);
    }

    @Before
    public void setUp() {
        this.jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        systemMessageDao = context.getBean(SystemMessageDao.class);
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
        assertTrue(dao.unlockIfNoMessages(device, lockKey));
        assertTrue(dao.lock(device, lockKey));
    }
    @Test
    public void testUpdateTime() {
        final String device = "device";
        final String lockKey = "anyLock";
        assertTrue(dao.lock(device, lockKey));

        //update time to very old
        final Map<String, Object> params = new HashMap<>();
        final Date date = new Date(System.currentTimeMillis() - 10000000000l);
        params.put("time", date);
        jdbc.update("update grouplocks set lastupdate = :time", params);

        //add system message for given device
        final Date readyOn = new Date(System.currentTimeMillis() - 100000l);
        createSystemMessage(device, readyOn);

        //check not unlocked
        assertFalse(dao.unlockIfNoMessages(device, lockKey));

        //check updated last time
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select lastupdate from grouplocks", new HashMap<>());
        assertEquals(1, rows.size());
        assertTrue(Math.abs(((Date) rows.get(0).get("lastupdate")).getTime() - date.getTime()) > 100000l);
    }

    @Test
    public void testIgnoresNotReady() {
        final String device = "device";
        final String lockKey = "anyLock";
        assertTrue(dao.lock(device, lockKey));

        //add system message for given device
        createSystemMessage(device, new Date(System.currentTimeMillis() + 100000l));

        //check not unlocked
        assertTrue(dao.unlockIfNoMessages(device, lockKey));

        //check updated last time
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select lastupdate from grouplocks", new HashMap<>());
        assertEquals(0, rows.size());
    }
    @Test
    public void testDeleteOldLocks() {
        assertTrue(dao.lock("d1", "l1"));
        assertTrue(dao.lock("d2", "l2"));

        //update time to very old
        final Map<String, Object> params = new HashMap<>();
        params.put("time", new Date(System.currentTimeMillis() - 10000000000l));
        jdbc.update("update grouplocks set lastupdate = :time", params);

        //create new lock
        assertTrue(dao.lock("d3", "l3"));

        dao.unlockOlder(new Date(System.currentTimeMillis() - 100000000l));

        assertEquals(1, jdbc.queryForList("select * from grouplocks", new HashMap<>()).size());
    }
    /**
     * @param device the device.
     * @param readyOn ready on time.
     * @return system message.
     */
    private SystemMessage createSystemMessage(final String device, final Date readyOn) {
        final SystemMessage msg = new SystemMessage();
        msg.setType(SystemMessageType.Tracker);
        msg.setGroup(device);
        msg.setMessageInfo("{}");
        msg.setTime(readyOn);
        msg.setRetryOn(readyOn);
        return systemMessageDao.save(msg);
    }
}
