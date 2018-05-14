/**
 *
 */
package com.visfresh.impl.services;

import java.util.Date;
import java.util.List;

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
import com.visfresh.services.GroupLockService;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerMessageDispatcher extends AbstractSystemMessageDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TrackerMessageDispatcher.class);

    @Autowired
    protected GroupLockService deviceLocker;
    @Autowired
    private SystemMessageDao systemMessageDao;
    private final ThreadLocal<Date> unLockTime = new ThreadLocal<Date>();
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public TrackerMessageDispatcher(final Environment env) {
        this();
        //batch limit should be hardcoded for given version
        setBatchLimit(Integer.parseInt(env.getProperty("tracker.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("tracker.dispatcher.retryLimit", "5")));
        //number of threads should be hardcoded to 1
        setNumThreads(Integer.parseInt(env.getProperty("tracker.dispatcher.numThreads", "7")));
        setInactiveTimeOut(Long.parseLong(env.getProperty("tracker.dispatcher.retryLimit", "3000")));
    }
    protected TrackerMessageDispatcher() {
        super(SystemMessageType.Tracker);
        setBatchLimit(15);
        setRetryLimit(5);
        setNumThreads(0);
        setInactiveTimeOut(3000);
    }

    protected int processMessages() {
        int count = 0;

        final Date readyOn = new Date(System.currentTimeMillis());
        final String device = lockFreeDevice(readyOn);

        if (device != null) {
            try {
                boolean shouldStop = false;
                if (!shouldStop && !isStoped.get() && !messageHandlers.isEmpty()) {
                    final List<SystemMessage> messages = getMessagesForGoup(readyOn, device);

                    for (final SystemMessage msg : messages) {
                        if (isStoped.get()) {
                            break;
                        }

                        try {
                            messageHandlers.get(msg.getType()).handle(msg);
                            handleSuccess(msg);
                        } catch(final Throwable e) {
                            log.error("Error detected for processing messages for device " + device
                                    + " ");
                            if (handleError(msg, e)) {
                                shouldStop = true;
                                break;
                            }
                        }
                    }

                    count += messages.size();
                }
            } finally {
                final Date t = this.unLockTime.get();
                if (t == null) {
                    unlockDevice(device);
                } else {
                    unLockTime.set(null);
                    setUnlockTime(device, t);
                }
            }
        }

        return count;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#prepareForNextRetry(com.visfresh.entities.SystemMessage, java.lang.Throwable)
     */
    @Override
    protected void prepareForNextRetry(final SystemMessage msg, final Throwable e) {
        //should not update retryOn
        final Date oldRetryOn = msg.getRetryOn();
        super.prepareForNextRetry(msg, e);

        unLockTime.set(msg.getRetryOn());
        msg.setRetryOn(oldRetryOn);
    }

    /**
     * @param device
     */
    protected void unlockDevice(final String device) {
        deviceLocker.unlock(device, processorId);
    }
    /**
     * @param device device.
     * @param unlockOn unlock date.
     */
    protected void setUnlockTime(final String device, final Date unlockOn) {
        deviceLocker.setUnlockOn(device, processorId, unlockOn);
    }

    /**
     * @param readyOn
     * @return device IMEI.
     */
    protected String lockFreeDevice(final Date readyOn) {
        final List<String> devices = systemMessageDao.getNotLockedDevicesWithReadyMessages(
                readyOn, 10);
        for (final String device : devices) {
            if (deviceLocker.lockGroup(device, processorId)) {
                return device;
            }
        }
        return null;
    }

    /**
     * @param readyOn retry date.
     * @param device device.
     * @return list of messages for given group. Messages should be sorted by retryOn.
     */
    protected List<SystemMessage> getMessagesForGoup(final Date readyOn, final String device) {
        return getMessageDao().getMessagesForGoup(
                SystemMessageType.Tracker, device, readyOn, getBatchLimit());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#setSystemMessageHandler(com.visfresh.entities.SystemMessageType, com.visfresh.services.SystemMessageHandler)
     */
    public void setHandler(final SystemMessageHandler h) {
        super.setSystemMessageHandler(SystemMessageType.Tracker, h);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#createWorker(int)
     */
    @Override
    protected Worker createWorker(final int number) {
        return new Worker("trackerevents-" + number) {

            @Override
            protected int processMessages() {
                return TrackerMessageDispatcher.this.processMessages();
            }
        };
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#stop()
     */
    @Override
    @PreDestroy
    public void stop() {
        super.stop();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#start()
     */
    @Override
    @PostConstruct
    public void start() {
        this.processorId = getInstanceId() + ".trke";
        super.start();
    }
}
