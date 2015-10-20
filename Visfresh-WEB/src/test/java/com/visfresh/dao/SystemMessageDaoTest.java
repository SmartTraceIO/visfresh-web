/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

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
        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo("any message info");
        msg.setNumberOfRetry(99);
        msg.setProcessor("proc-junit");
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
}
