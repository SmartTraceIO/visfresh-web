/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDispatcherTestHelper implements SystemMessageHandler {
    private final Map<Long, SystemMessage> messages = new HashMap<>();
    private long lastId = 1l;
    private Throwable error;
    private String group = "junit";

    /**
     * Default constructor.
     */
    public SystemMessageDispatcherTestHelper() {
        super();
    }

    public String getProcessorId() {
        return "junit";
    }
    public SystemMessage saveMessage(final SystemMessage msg) {
        if (msg.getId() == null) {
            msg.setId(lastId++);
        }

        messages.put(msg.getId(), cloneSystemMessage(msg));
        return msg;
    }
    /**
     * @param msg
     * @return
     */
    public static SystemMessage cloneSystemMessage(final SystemMessage msg) {
        final SystemMessage m = new SystemMessage();
        m.setGroup(msg.getGroup());
        m.setId(msg.getId());
        m.setMessageInfo(msg.getMessageInfo());
        m.setNumberOfRetry(msg.getNumberOfRetry());
        m.setProcessor(msg.getProcessor());
        m.setRetryOn(new Date(msg.getRetryOn().getTime()));
        m.setTime(new Date(msg.getTime().getTime()));
        m.setType(msg.getType());
        return m;
    }
    public void deleteMessage(final SystemMessage msg) {
        messages.remove(msg.getId());
    }
    /**
     * @return the messages
     */
    public Map<Long, SystemMessage> getMessages() {
        return messages;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        if (error instanceof RetryableException) {
            throw (RetryableException) error;
        } else if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else if (error instanceof Error) {
            throw (Error) error;
        }
        //else nothing.
    }
    /**
     * @param error the error to set
     */
    public void setError(final RetryableException error) {
        this.error = error;
    }
    /**
     * @param error the error to set
     */
    public void setError(final RuntimeException error) {
        this.error = error;
    }
    /**
     * @param error the error to set
     */
    public void setError(final Error error) {
        this.error = error;
    }

    /**
     * @param retryOn retry date.
     * @return system message.
     */
    public SystemMessage createMessage(final Date retryOn) {
        final SystemMessage msg = new SystemMessage();
        msg.setRetryOn(retryOn);
        msg.setGroup(group);
        msg.setMessageInfo("{}");
        msg.setTime(retryOn);
        msg.setType(SystemMessageType.Tracker);
        return msg;
    }

    /**
     * @return
     */
    public String getGroup() {
        return group;
    }
    /**
     * @param group the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
    }
}
