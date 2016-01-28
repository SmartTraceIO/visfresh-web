/**
 *
 */
package com.visfresh.services;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.SystemMessageDao;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractSystemMessageDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractSystemMessageDispatcher.class);

    /**
     * Select messages limit.
     */
    protected int batchLimit;
    /**
     * Maximal inactivity time.
     */
    protected long inactiveTimeOut;
    /**
     * The limit of message retrying.
     */
    protected int retryLimit;
    /**
     * The time out before retry message.
     */
    protected long retryTimeOut;

    private final AtomicBoolean isStoped = new AtomicBoolean(false);

    private final Map<SystemMessageType, SystemMessageHandler> messageHandlers
        = new HashMap<SystemMessageType, SystemMessageHandler>();
    private final Set<SystemMessageType> messageTypes = new LinkedHashSet<>();

    @Autowired
    private SystemMessageDao messageDao;

    private int numThreads;

    private class Dispathcer extends Thread {
        private String id;

        /**
         * @param id dispatcher ID.
         */
        public Dispathcer(final String id) {
            super();
            this.id = id;
        }
        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            while (!isStoped.get()) {
                try {
                    final int numProcessed = processMessages(id);
                    if (numProcessed > 0) {
                        log.debug(numProcessed + " messages is processed by " + id);
                    } else if (getInactiveTimeOut() > 0){
                        log.debug("0 messages has processed, dispatcher "
                                + id + " will paused for " + getInactiveTimeOut() + " ms");

                        synchronized (isStoped) {
                            isStoped.wait(getInactiveTimeOut());
                        }
                    }
                } catch (final Throwable e) {
                    log.error("Global exception during dispatch of messaegs", e);
                    try {
                        Thread.sleep(10000L);
                    } catch (final InterruptedException ine) {
                        ine.printStackTrace();
                    }
                }
            }

            log.debug("Dispatcher " + id + " has stopped");
        }
    };
    /**
     * @param t message type.
     */
    @Autowired
    public AbstractSystemMessageDispatcher(final SystemMessageType... t) {
        super();
        this.messageTypes.addAll(Arrays.asList(t));
        setBatchLimit(10);
        setRetryLimit(5);
        setNumThreads(1);
        setInactiveTimeOut(3000);
    }

    /**
     * @param parseInt
     */
    protected void setNumThreads(final int numThreads) {
        this.numThreads = numThreads;
    }
    /**
     * @return the numThreads
     */
    public int getNumThreads() {
        return numThreads;
    }
    /**
     * @return the inactiveTimeOut
     */
    public long getInactiveTimeOut() {
        return inactiveTimeOut;
    }
    /**
     * @param inactiveTimeOut the inactiveTimeOut to set
     */
    public void setInactiveTimeOut(final long inactiveTimeOut) {
        this.inactiveTimeOut = inactiveTimeOut;
    }
    /**
     * @return the processorId
     */
    protected abstract String getProcessorId();
    /**
     * @return the limit
     */
    public int getBatchLimit() {
        return batchLimit;
    }
    /**
     * @param limit the limit to set
     */
    public void setBatchLimit(final int limit) {
        this.batchLimit = limit;
    }
    /**
     * @return the retryLimit
     */
    public int getRetryLimit() {
        return retryLimit;
    }
    /**
     * @param retryLimit the retryLimit to set
     */
    public void setRetryLimit(final int retryLimit) {
        this.retryLimit = retryLimit;
    }
    /**
     * @return the retryTimeOut
     */
    public long getRetryTimeOut() {
        return retryTimeOut;
    }
    /**
     * @param retryTimeOut the retryTimeOut to set
     */
    public void setRetryTimeOut(final long retryTimeOut) {
        this.retryTimeOut = retryTimeOut;
    }

    /**
     * @param processorId
     * @return
     */
    protected int processMessages(final String processorId) {
        final int count = 0;

        if (!messageHandlers.isEmpty()) {
            final List<SystemMessage> messages = messageDao.selectMessagesForProcessing(
                    messageTypes, processorId, getBatchLimit(), new Date());

            for (final SystemMessage msg : messages) {
                try {
                    messageHandlers.get(msg.getType()).handle(msg);
                    handleSuccess(msg);
                } catch(final Throwable e) {
                    handleError(msg, e);
                }
            }
        }

        return count;
    }

    /**
     * @param msg the message.
     * @param e the exception.
     */
    protected void handleError(final SystemMessage msg, final Throwable e) {
        if (e instanceof RetryableException && ((RetryableException) e).canRetry()) {
            if (msg.getNumberOfRetry() < getRetryLimit()) {
                final RetryableException re = (RetryableException) e;

                log.error("Retryable exception has occured for message " + msg + ", will retry later", re);
                long timeOut = getRetryTimeOut();
                if (re.getRetryTimeOut() > 0) { // if retry time out is set on exception, use it
                    timeOut = re.getRetryTimeOut();
                }

                msg.setRetryOn(new Date(msg.getRetryOn().getTime() + timeOut));
                msg.setNumberOfRetry(msg.getNumberOfRetry() + 1);
                msg.setProcessor(null);//clear processor
                messageDao.save(msg);
            } else {
                log.error("Retry limit has exceed for message " + msg + ", will not dispatched next", e);
                messageDao.delete(msg);
            }
        } else {
            log.error("Not retryable exception has occured for message " + msg.getId()
                    + ", will not dispatched next", e);
            messageDao.delete(msg);
        }
    }
    /**
     * @param msg the message.
     */
    protected void handleSuccess(final SystemMessage msg) {
        log.debug("The message " + msg.getId() + " successfylly processed by " + getProcessorId()
                + ", will not dispatched next");
        messageDao.delete(msg);
    }

    /**
     * Stops the dispatcher.
     */
    public void stop() {
        synchronized (isStoped) {
            isStoped.set(true);
            isStoped.notifyAll();
        }
    }
    /**
     * starts the dispatcher.
     */
    public void start() {
        stop();
        synchronized (isStoped) {
            isStoped.set(false);
            for (int i = 0; i < getNumThreads(); i++) {
                final String id = getProcessorId() + "-" + i;
                final Dispathcer d = new Dispathcer(id);
                d.start();
            }
        }
    }
    /**
     * @param type message type.
     * @param h message handler.
     */
    public void setSystemMessageHandler(final SystemMessageType type, final SystemMessageHandler h) {
        if (!messageTypes.contains(type)) {
            throw new IllegalArgumentException("Unsupported message type for given processor "
                    + type + ", expected types " + messageTypes);
        }
        this.messageHandlers.put(type, h);
    }
    /**
     * @param messagePayload message payload.
     * @param type system message type.
     */
    public void sendSystemMessage(final String messagePayload, final SystemMessageType type) {
        sendSystemMessage(messagePayload, type, new Date());
    }
    /**
     * @param messagePayload message payload.
     * @param type system message type.
     * @param retryOn retry date.
     */
    public void sendSystemMessage(final String messagePayload,
            final SystemMessageType type, final Date retryOn) {
        final SystemMessage sm = new SystemMessage();
        sm.setType(type);
        sm.setMessageInfo(messagePayload);
        sm.setTime(new Date());
        sm.setRetryOn(retryOn);
        messageDao.save(sm);
    }
    /**
     * @return the messageDao
     */
    public SystemMessageDao getMessageDao() {
        return messageDao;
    }
}
