/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.dao.SystemMessageDao;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageDispatcher;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SystemMessageDispatcherImpl implements SystemMessageDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SystemMessageDispatcherImpl.class);

    /**
     * Processor ID.
     */
    private final String processorId;
    /**
     * Select messages limit.
     */
    private int batchLimit;
    /**
     * Maximal inactivity time.
     */
    private long inactiveTimeOut;
    /**
     * The limit of message retrying.
     */
    private int retryLimit;
    /**
     * The time out before retry message.
     */
    private long retryTimeOut;

    private final AtomicBoolean isStoped = new AtomicBoolean(false);

    private final Map<SystemMessageType, SystemMessageHandler> handlers
        = new ConcurrentHashMap<SystemMessageType, SystemMessageHandler>();

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
                }
            }

            log.debug("Dispatcher " + id + " has stopped");
        }
    };
    /**
     * Default constructor..
     */
    @Autowired
    public SystemMessageDispatcherImpl(final Environment env) {
        super();
        processorId = env.getProperty("system.dispatcher.baseProcessorId", "sys-dispatcher");
        setBatchLimit(Integer.parseInt(env.getProperty("system.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("system.dispatcher.retryLimit", "5")));
        setNumThreads(Integer.parseInt(env.getProperty("system.dispatcher.numThreads", "1")));
        setInactiveTimeOut(Long.parseLong(env.getProperty("system.dispatcher.retryLimit", "3000")));
    }

    /**
     * @param parseInt
     */
    private void setNumThreads(final int numThreads) {
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
    public String getProcessorId() {
        return processorId;
    }
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

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.SystemMessageDispatcher#processMessages(java.lang.String)
     */
    @Override
    public int processMessages(final String processor) {
        final int count = 0;

        final Set<SystemMessageType> processors = handlers.keySet();
        if (!processors.isEmpty()) {
            final List<SystemMessage> messages = messageDao.selectMessagesForProcessing(
                    processors, processor, getBatchLimit(), new Date());
            for (final SystemMessage msg : messages) {
                final SystemMessageHandler h = handlers.get(msg.getType());
                try {
                    h.handle(msg);
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

    @PreDestroy
    public void stop() {
        synchronized (isStoped) {
            isStoped.set(true);
            isStoped.notifyAll();
        }
    }

    @PostConstruct
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

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.SystemMessageDispatcher#setSystemMessageHandler(com.visfresh.entities.SystemMessageType, com.visfresh.services.SystemMessageHandler)
     */
    @Override
    public void setSystemMessageHandler(final SystemMessageType type, final SystemMessageHandler h) {
        if (h == null) {
            handlers.remove(type);
        } else {
            handlers.put(type, h);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#sendSystemMessage(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void sendSystemMessage(final SystemMessageType type, final String messagePayload) {
        final SystemMessage sm = new SystemMessage();
        sm.setType(type);
        sm.setMessageInfo(messagePayload);
        messageDao.save(sm);
    }
    /**
     * @return the messageDao
     */
    public SystemMessageDao getMessageDao() {
        return messageDao;
    }
}
