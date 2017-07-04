/**
 *
 */
package com.visfresh.impl.services;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.mock.MockDeviceLockService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageDispatcherTestHelper;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerMessageDispatcherTest extends TrackerMessageDispatcher {
    protected final SystemMessageDispatcherTestHelper helper = new SystemMessageDispatcherTestHelper();
    private final MockDeviceLockService lockService = new MockDeviceLockService();

    /**
     * Default constructor.
     */
    public TrackerMessageDispatcherTest() {
        super();
        this.deviceLocker = lockService;
        setSystemMessageHandler(SystemMessageType.Tracker, helper);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractAssyncSystemMessageDispatcher#getProcessorId()
     */
    @Override
    protected String getProcessorId() {
        return helper.getProcessorId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#saveMessage(com.visfresh.entities.SystemMessage)
     */
    @Override
    protected SystemMessage saveMessage(final SystemMessage msg) {
        return helper.saveMessage(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#deleteMessage(com.visfresh.entities.SystemMessage)
     */
    @Override
    protected void deleteMessage(final SystemMessage msg) {
        helper.deleteMessage(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.NewTrackerMessageDispatcher#getMessagesForGoup(java.util.Date, java.lang.String)
     */
    @Override
    protected List<SystemMessage> getMessagesForGoup(final Date readyOn, final String device) {
        final List<SystemMessage> msgs = new LinkedList<>();

        for (final SystemMessage msg : this.helper.getMessages().values()) {
            if (device.equals(msg.getGroup()) && !msg.getRetryOn().after(readyOn)) {
                msgs.add(msg);
            }
        }

        return msgs;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.NewTrackerMessageDispatcher#lockFreeDevice(java.util.Date)
     */
    @Override
    protected String lockFreeDevice(final Date readyOn) {
        for (final SystemMessage msg : this.helper.getMessages().values()) {
            if (!lockService.getLocks().containsKey(msg.getGroup()) && !msg.getRetryOn().after(readyOn)) {
                lockService.lockDevice(msg.getGroup(), getProcessorId());
                return msg.getGroup();
            }
        }
        return null;
    }

    @Test
    public void testSuccess() {
        final long time = System.currentTimeMillis() - 100000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        //process and check result.
        assertEquals(3, processMessages(getProcessorId()));
        assertEquals(0, helper.getMessages().size());

        //check unlock device
        assertEquals(0, lockService.getLocks().size());
    }
    @Test
    public void testNotTouchAlreadyLockedDevices() {
        final long time = System.currentTimeMillis() - 100000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        //lock devices (by another processor before process
        lockFreeDevice(new Date());

        //process and check result.
        assertEquals(0, processMessages(getProcessorId()));
        assertEquals(3, helper.getMessages().size());
    }
    @Test
    public void testRetryableException() {
        final long time = System.currentTimeMillis() - 100000000l;

        SystemMessage msg1 = helper.createMessage(new Date(time));
        saveMessage(msg1);
        SystemMessage msg2 = helper.createMessage(new Date(time + 10));
        saveMessage(msg2);
        SystemMessage msg3 = helper.createMessage(new Date(time + 20));
        saveMessage(msg3);

        final long timeOut = 1000000l;
        setRetryTimeOut(timeOut);
        helper.setError(new RetryableException());

        processMessages(processorId);

        assertEquals(3, helper.getMessages().size());
        assertEquals(1, lockService.getLocks().size());

        //check shifted
        msg1 = helper.getMessages().get(msg1.getId());
        msg2 = helper.getMessages().get(msg2.getId());
        msg3 = helper.getMessages().get(msg3.getId());

        //check number of retries
        assertEquals(1, msg1.getNumberOfRetry());
        assertEquals(0, msg2.getNumberOfRetry());
        assertEquals(0, msg3.getNumberOfRetry());
    }
    @Test
    public void testRetryBlock() {
        final long time = System.currentTimeMillis() - 100000000l;

        final SystemMessage msg1 = helper.createMessage(new Date(time));
        saveMessage(msg1);
        final SystemMessage msg2 = helper.createMessage(new Date(time));
        saveMessage(msg2);

        final long timeOut = 1000000l;
        setRetryTimeOut(timeOut);
        helper.setError(new RetryableException());

        final Date readyOn = new Date(time + 3000l);
        final String device = helper.getGroup();

        assertEquals(2, getMessagesForGoup(readyOn, device).size());
        assertEquals(2, processMessages(processorId));
        //not secondary process the messages because lock not removed
        assertEquals(0, processMessages(processorId));
    }
    @Test
    public void testRetryableExceptionNotRetry() {
        final long time = System.currentTimeMillis() - 100000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        setRetryLimit(1);

        final RetryableException e = new RetryableException();
        e.setCanRetry(false);

        helper.setError(e);

        processMessages(processorId);

        //check message has removed immediately
        assertEquals(0, helper.getMessages().size());

        //check unlock device
        assertEquals(0, lockService.getLocks().size());
    }
    @Test
    public void testHandleNotRetryableException() {
        final long time = System.currentTimeMillis() - 100000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        helper.setError(new RuntimeException());
        processMessages(getProcessorId());

        //check message has removed immediately
        assertEquals(0, helper.getMessages().size());
    }
    @Test
    public void testHandleSqlException() {
        final long time = System.currentTimeMillis() - 100000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        helper.setError(new RuntimeException(new SQLException("Test exception")));
        processMessages(getProcessorId());

        //check message has removed immediately
        assertEquals(3, helper.getMessages().size());
    }
}
