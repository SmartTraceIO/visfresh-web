/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractSystemMessageDispatcherTest extends AbstractSystemMessageDispatcher {
    protected final SystemMessageDispatcherTestHelper helper = new SystemMessageDispatcherTestHelper();

    /**
     * Default constructor.
     */
    public AbstractSystemMessageDispatcherTest() {
        super(SystemMessageType.Tracker);
        setSystemMessageHandler(SystemMessageType.Tracker, helper);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#getCurrentMySqlProcesses()
     */
    @Override
    protected String getCurrentMySqlProcesses() {
        return "Processes";
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
    @Test
    public void testHandleSuccess() {
        final SystemMessage msg = helper.createMessage(new Date());

        handleSuccess(msg);
        assertNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testRetryableException() {
        final Date retryOn = new Date();
        final SystemMessage msg = helper.createMessage(retryOn);

        setRetryLimit(1);
        setRetryTimeOut(1000000l);

        final RetryableException e = new RetryableException();

        handleError(msg, e);
        assertNotNull(helper.getMessages().get(msg.getId()));
        assertTrue(msg.getRetryOn().after(retryOn));

        handleError(msg, e);
        assertNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testRetryableExceptionRetryTimeOut() {
        final Date retryOn = new Date();
        final long timeOut = 100000l;
        final SystemMessage msg = helper.createMessage(retryOn);

        final RetryableException e = new RetryableException();
        e.setRetryTimeOut(timeOut);

        handleError(msg, e);
        assertTrue(Math.abs(helper.getMessages().get(msg.getId()).getRetryOn().getTime()
                + timeOut - retryOn.getTime()) > 10000l);
    }
    @Test
    public void testRetryableExceptionNotRetry() {
        final SystemMessage msg = helper.createMessage(new Date());
        saveMessage(msg);

        setRetryLimit(1);

        final RetryableException e = new RetryableException();
        e.setCanRetry(false);

        handleError(msg, e);

        //check message has removed immediately
        assertNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testHandleRuntimeException() {
        final SystemMessage msg = helper.createMessage(new Date());
        saveMessage(msg);

        handleError(msg, new RuntimeException());

        //check message has removed immediately
        assertNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testHandleLockWaitTimeOutException() {
        final SystemMessage msg = helper.createMessage(new Date());
        saveMessage(msg);

        handleError(msg, new SQLException("Lock wait timeout exceeded"));

        //check message not removed
        assertNotNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testHandleError() {
        final SystemMessage msg = helper.createMessage(new Date());
        saveMessage(msg);

        handleError(msg, new Error());

        //check message has removed immediately
        assertNull(helper.getMessages().get(msg.getId()));
    }
    @Test
    public void testHandleSqlException() {
        final SystemMessage msg = helper.createMessage(new Date());
        saveMessage(msg);

        handleError(msg, new SQLException("Test exception"));

        //check message not removed
        assertNotNull(helper.getMessages().get(msg.getId()));
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#createWorker(int)
     */
    @Override
    protected Worker createWorker(final int number) {
        return null;
    }
}
