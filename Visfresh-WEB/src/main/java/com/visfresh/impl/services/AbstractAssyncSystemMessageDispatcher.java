/**
 *
 */
package com.visfresh.impl.services;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractAssyncSystemMessageDispatcher extends AbstractSystemMessageDispatcher {
    /**
     * @param t message type.
     */
    public AbstractAssyncSystemMessageDispatcher(final SystemMessageType... t) {
        super(t);
    }

    /**
     * @return the processorId
     */
    protected abstract String getBaseProcessorId();

    /**
     * @param processorId
     * @return
     */
    protected int processMessages(final String processorId) {
        int count = 0;

        if (!isStoped.get() && !messageHandlers.isEmpty()) {
            final Date readyOn = new Date(System.currentTimeMillis() + 1000l);
            final List<SystemMessage> messages = selectMessagesForProcess(processorId, readyOn);

            for (final SystemMessage msg : messages) {
                if (isStoped.get()) {
                    break;
                }

                try {
                    final SystemMessageHandler h = messageHandlers.get(msg.getType());
                    if (h != null) {
                        h.handle(msg);
                        handleSuccess(msg);
                    } else {
                        //possible dispatcher not yet fully initialized but messages are selected
                        //on previous web application deploy session.
                        final RetryableException exc = new RetryableException("Found not handler"
                                + " for message of given type " + msg.getType()
                                + ". Possible not yet initialized.");
                        exc.setCanRetry(true);
                        exc.setRetryTimeOut(180000l);
                        throw exc;
                    }
                } catch(final Throwable e) {
                    handleError(msg, e);
                }
            }

            count += messages.size();
        }

        return count;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#createWorker(int)
     */
    @Override
    protected Worker createWorker(final int number) {
        final String id = getBaseProcessorId() + "-" + number;
        return new Worker(id) {
            /* (non-Javadoc)
             * @see com.visfresh.services.AbstractSystemMessageDispatcher.Worker#processMessages()
             */
            @Override
            protected int processMessages() {
                return AbstractAssyncSystemMessageDispatcher.this.processMessages(id);
            }
        };
    }

    /**
     * @param processorId
     * @param readyOn
     * @return
     */
    protected List<SystemMessage> selectMessagesForProcess(final String processorId, final Date readyOn) {
        return getMessageDao().selectMessagesForProcessing(
                messageTypes, processorId, getBatchLimit(), readyOn);
    }
}
