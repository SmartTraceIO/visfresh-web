/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.impl.services.AbstractAssyncSystemMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractAssyncSystemMessageDispatcherTest extends AbstractAssyncSystemMessageDispatcher {
    protected final SystemMessageDispatcherTestHelper helper = new SystemMessageDispatcherTestHelper();

    /**
     * Default constructor.
     */
    public AbstractAssyncSystemMessageDispatcherTest() {
        super(SystemMessageType.Tracker);
        setSystemMessageHandler(SystemMessageType.Tracker, helper);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractAssyncSystemMessageDispatcher#getProcessorId()
     */
    @Override
    protected String getBaseProcessorId() {
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
     * @see com.visfresh.services.AbstractAssyncSystemMessageDispatcher#selectMessagesForProcess(java.lang.String, java.util.Date)
     */
    @Override
    protected List<SystemMessage> selectMessagesForProcess(final String processorId, final Date readyOn) {
        final List<SystemMessage> msgs = new LinkedList<>();
        for (final SystemMessage m : helper.getMessages().values()) {
            if (!m.getRetryOn().after(readyOn)) {
                m.setProcessor(processorId);
                msgs.add(SystemMessageDispatcherTestHelper.cloneSystemMessage(m));
            }
        }

        //sort messages by ready on
        Collections.sort(msgs, (m1, m2) -> m1.getRetryOn().compareTo(m2.getRetryOn()));
        return msgs;
    }
    @Test
    public void testSuccess() {
        final long time = System.currentTimeMillis() - 1000000000l;

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        assertEquals(3, processMessages(getBaseProcessorId()));
        assertEquals(0, helper.getMessages().size());
    }
    @Test
    public void testError() {
        final long time = System.currentTimeMillis() - 1000000000l;
        setRetryTimeOut(10000l);

        saveMessage(helper.createMessage(new Date(time + 1 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 2 * 100000l)));
        saveMessage(helper.createMessage(new Date(time + 3 * 100000l)));

        helper.setError(new RetryableException());
        assertEquals(3, processMessages(getBaseProcessorId()));
        assertEquals(3, helper.getMessages().size());
    }
}
