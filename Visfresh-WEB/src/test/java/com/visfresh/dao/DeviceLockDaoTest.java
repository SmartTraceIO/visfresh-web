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
    @Test
    public void testGetNotLockedDevicesWithReadyMessagesLimit() {
        final Date msgReadyOn = new Date(System.currentTimeMillis() - 10000000000l);

        createSystemMessage("d1", msgReadyOn);
        createSystemMessage("d1", msgReadyOn);

        createSystemMessage("d2", msgReadyOn);
        createSystemMessage("d2", msgReadyOn);

        final Date readyOn = new Date(System.currentTimeMillis() - 1000000000l);
        assertEquals(1, dao.getNotLockedDevicesWithReadyMessages(readyOn, 1).size());
        assertEquals(2, dao.getNotLockedDevicesWithReadyMessages(readyOn, 2).size());
        assertEquals(2, dao.getNotLockedDevicesWithReadyMessages(readyOn, 3).size());

        //lock one device
        dao.lock("d1", "junit");
        assertEquals(1, dao.getNotLockedDevicesWithReadyMessages(readyOn, 2).size());
    }
    @Test
    public void testGetNotLockedDevicesWithReadyMessagesReadyOn() {
        final Date readyOn1 = new Date(System.currentTimeMillis() - 10000000000l);
        final Date readyOn2 = new Date(System.currentTimeMillis() - 1000000000l);

        createSystemMessage("d1", readyOn1);
        createSystemMessage("d1", readyOn1);

        createSystemMessage("d2", readyOn2);
        createSystemMessage("d2", readyOn2);

        assertEquals(1, dao.getNotLockedDevicesWithReadyMessages(
                new Date((readyOn1.getTime() + readyOn2.getTime()) / 2), 100).size());
        assertEquals("d1", dao.getNotLockedDevicesWithReadyMessages(
                new Date((readyOn1.getTime() + readyOn2.getTime()) / 2), 100).get(0));
        assertEquals(2, dao.getNotLockedDevicesWithReadyMessages(
                new Date(readyOn2.getTime() + 100000l), 100).size());
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
