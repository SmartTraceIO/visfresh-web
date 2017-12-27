/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.impl.services.DeviceDcsNativeEvent;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDaoTest extends
        BaseCrudTest<SystemMessageDao, SystemMessage, SystemMessage, Long> {
    private final DeviceDcsNativeEventSerializer eventSerializer = new DeviceDcsNativeEventSerializer();

    /**
     * @param clazz
     */
    public SystemMessageDaoTest() {
        super(SystemMessageDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected SystemMessage createTestEntity() {
        return createTestEntity("proc-junit");
    }

    /**
     * @param proc
     * @return
     */
    protected SystemMessage createTestEntity(final String proc) {
        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo("any message info");
        msg.setNumberOfRetry(99);
        msg.setProcessor(proc);
        msg.setRetryOn(new Date(System.currentTimeMillis() + 10000000l));
        msg.setTime(new Date(System.currentTimeMillis() - 10000000l));
        msg.setType(SystemMessageType.Tracker);
        msg.setGroup("junit");
        return msg;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final SystemMessage msg) {
        assertEquals("any message info", msg.getMessageInfo());
        assertEquals(99, msg.getNumberOfRetry());
        assertEquals("proc-junit", msg.getProcessor());
        assertTrue(msg.getRetryOn().after(new Date(System.currentTimeMillis() + 10000l)));
        assertTrue(msg.getTime().before(new Date(System.currentTimeMillis() - 10000l)));
        assertEquals(SystemMessageType.Tracker, msg.getType());
        assertEquals("junit", msg.getGroup());

    }
    @Test
    public void testSelectMessagesForProcessing() {
        final SystemMessage m1 = createTestEntity(null);
        dao.save(m1);

        //test date
        final String processor = "proc-1";
        final Set<SystemMessageType> types = new HashSet<SystemMessageType>();
        types.add(SystemMessageType.Tracker);

        assertEquals(0, dao.selectMessagesForProcessing(types, processor, 100,
                new Date(m1.getRetryOn().getTime() - 10000L)).size());
        assertEquals(1, dao.selectMessagesForProcessing(types, processor, 100,
                new Date(m1.getRetryOn().getTime() + 1000L)).size());

        //clear processor field
        dao.save(m1);
        //check message type
        types.clear();
        types.add(SystemMessageType.DeviceCommand);
        assertEquals(0, dao.selectMessagesForProcessing(types, processor, 100,
                new Date(m1.getRetryOn().getTime() + 10L)).size());

        //test limit
        types.add(SystemMessageType.Tracker);

        final SystemMessage m2 = createTestEntity(null);
        dao.save(m2);
        final SystemMessage m3 = createTestEntity(null);
        dao.save(m3);

        final List<SystemMessage> messages = dao.selectMessagesForProcessing(types, processor, 2,
                new Date(m3.getRetryOn().getTime() + 1500L));
        assertEquals(2, messages.size());

        //test ordering
        assertEquals(m1.getId(), messages.get(0).getId());
        assertNull(dao.findOne(m3.getId()).getProcessor());
    }
    @Test
    public void testGetMessagesForGoup() {
        final SystemMessage m1 = createTestEntity(null);
        dao.save(m1);

        //test date
        final String group = m1.getGroup();
        final SystemMessageType messageType = SystemMessageType.Tracker;

        assertEquals(0, dao.getMessagesForGoup(messageType, group,
                new Date(m1.getRetryOn().getTime() - 10000L), 100).size());
        assertEquals(1, dao.getMessagesForGoup(messageType, group,
                new Date(m1.getRetryOn().getTime() + 1000L), 100).size());
        assertEquals(0, dao.getMessagesForGoup(messageType, group + "abrakadabra",
                new Date(m1.getRetryOn().getTime() + 1000L), 100).size());

        //clear processor field
        dao.save(m1);

        //check message type
        assertEquals(0, dao.getMessagesForGoup(SystemMessageType.ArrivalReport, group,
                new Date(m1.getRetryOn().getTime() + 1000L), 100).size());

        //test limit
        final SystemMessage m2 = createTestEntity(null);
        dao.save(m2);
        final SystemMessage m3 = createTestEntity(null);
        dao.save(m3);

        final List<SystemMessage> messages = dao.getMessagesForGoup(messageType, group,
                new Date(m1.getRetryOn().getTime() + 1000L), 2);
        assertEquals(2, messages.size());

        //test ordering
        assertEquals(m1.getId(), messages.get(0).getId());
    }
    @Test
    public void testFindTrackerEvents() {
        final long dt = 100000;
        final long t = System.currentTimeMillis() - 20 * dt;

        final SystemMessage mStart = createTrackerEvent(new Date(t + 1 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 1 * dt));

        createTrackerEvent(new Date(t + 2 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 2 * dt));

        createTrackerEvent(new Date(t + 3 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 3 * dt));

        createTrackerEvent(new Date(t + 4 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 4 * dt));

        createTrackerEvent(new Date(t + 5 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 5 * dt));

        createTrackerEvent(new Date(t + 6 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 6 * dt));

        final SystemMessage mEnd = createTrackerEvent(new Date(t + 7 * dt));
        createSystemMessage("any data", SystemMessageType.DeviceCommand, new Date(t + 7 * dt));

        List<SystemMessage> msgs = dao.findTrackerEvents(true);
        assertEquals(7, msgs.size());
        assertEquals(mStart.getId(), msgs.get(0).getId());

        msgs = dao.findTrackerEvents(false);
        assertEquals(7, msgs.size());
        assertEquals(mEnd.getId(), msgs.get(0).getId());
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
        context.getBean(GroupLockDao.class).lock("d1", "junit");
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
        return dao.save(msg);
    }

    private SystemMessage createTrackerEvent(final Date date) {
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(date);
        e.setImei("10923870192873098");
        e.setType(TrackerEventType.AUT.name());
        return createSystemMessage(eventSerializer.toJson(e).toString(), SystemMessageType.Tracker, date);
    }
    /**
     * @param data message data.
     * @param type message type.
     * @param date reading on date.
     * @return system message.
     */
    protected SystemMessage createSystemMessage(final String data,
            final SystemMessageType type, final Date date) {
        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo(data);
        msg.setNumberOfRetry(99);
        msg.setRetryOn(date);
        msg.setTime(date);
        msg.setType(type);
        return dao.save(msg);
    }
}
