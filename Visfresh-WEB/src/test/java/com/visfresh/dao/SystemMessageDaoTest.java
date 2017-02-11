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
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDaoTest extends
        BaseCrudTest<SystemMessageDao, SystemMessage, Long> {
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
