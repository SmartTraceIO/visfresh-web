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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDaoTest extends
        BaseCrudTest<SystemMessageDao, SystemMessage, Long> {
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
                new Date(m1.getRetryOn().getTime() + 10L)).size());

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
                new Date(m3.getRetryOn().getTime() + 10L));
        assertEquals(2, messages.size());

        //test ordering
        assertEquals(m1.getId(), messages.get(0).getId());
        assertNull(dao.findOne(m3.getId()).getProcessor());
    }
}
