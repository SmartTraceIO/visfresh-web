/**
 *
 */
package com.visfresh.dispatcher;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessageBase;
import com.visfresh.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractDispatcher.class);

    /**
     * Processor ID.
     */
    private String processorId;
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

    /**
     * Message DAO.
     */
    @Autowired
    protected MessageDao dao;
    /**
     * Thread for asynchronous launch
     */
    private Thread thread;
    private final AtomicBoolean isStoped = new AtomicBoolean(false);

    /**
     * Default constructor..
     */
    public AbstractDispatcher() {
        super();
        setBatchLimit(10);
        setRetryLimit(5);
        setInactiveTimeOut(3000L);
    }

    public void start() {
        thread = new Thread() {
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                while (!isStoped()) {
                    try {
                        final int numProcessed = processMessages();
                        if (numProcessed > 0) {
                            log.debug(numProcessed + " messages is processed by " + getProcessorId());
                        } else if (getInactiveTimeOut() > 0){
                            log.debug("0 messages has processed, dispatcher "
                                    + getProcessorId() + " will paused for " + getInactiveTimeOut() + " ms");
                            sleep(getInactiveTimeOut());
                        }
                    } catch (final Throwable e) {
                        log.error("Global exception during dispatch of messaegs", e);
                    }
                }

                log.debug("Dispatcher " + getProcessorId() + " has stopped");
            }
        };
        thread.start();
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
     * @param id
     * @return
     */
    public String setProcessorId(final String id) {
        return this.processorId = id;
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

    /**
     * @return number of processed messages.
     */
    public abstract int processMessages();

    /**
     * @param msg the message.
     * @param e the exception.
     */
    protected void handleError(final DeviceMessageBase msg, final Throwable e) {
        if (e instanceof RetryableException && ((RetryableException) e).canRetry()) {
            if (msg.getNumberOfRetry() < getRetryLimit()) {
                log.error("Retryable exception has occured for message " + msg + ", will retry later", e);
                msg.setRetryOn(new Date(msg.getRetryOn().getTime() + getRetryTimeOut()));
                msg.setNumberOfRetry(msg.getNumberOfRetry() + 1);
                dao.saveForRetry(msg);
            } else {
                log.error("Retry limit has exceed for message " + msg + ", will deleted", e);
                dao.delete(msg);
            }
        } else {
            log.error("Not retryable exception has occured for message " + msg.getId() + ", will deleted", e);
            dao.delete(msg);
        }
    }
    /**
     * @param msg the message.
     */
    protected void handleSuccess(final DeviceMessageBase msg) {
        log.debug("The message " + msg.getId() + " successfylly processed by " + getProcessorId() + ", deleting it");
        dao.delete(msg);
    }
    public void stop() {
        isStoped.set(true);
    }

    /**
     * @return
     */
    protected boolean isStoped() {
        return isStoped.get();
    }
}
