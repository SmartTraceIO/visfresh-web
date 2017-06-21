/**
 *
 */
package com.visfresh.impl.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.AbstractSystemMessageDispatcher;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerMessageDispatcher extends AbstractSystemMessageDispatcher {
    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public TrackerMessageDispatcher(final Environment env) {
        super(SystemMessageType.Tracker);
        processorId = env.getProperty("tracker.dispatcher.baseProcessorId", "tracker-dispatcher");
        setBatchLimit(Integer.parseInt(env.getProperty("tracker.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("tracker.dispatcher.retryLimit", "5")));
        //number of threads should be hardcoded to 1
        //setNumThreads(Integer.parseInt(env.getProperty("tracker.dispatcher.numThreads", "1")));
        setNumThreads(1);
        setInactiveTimeOut(Long.parseLong(env.getProperty("tracker.dispatcher.retryLimit", "3000")));
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#setSystemMessageHandler(com.visfresh.entities.SystemMessageType, com.visfresh.services.SystemMessageHandler)
     */
    public void setHandler(final SystemMessageHandler h) {
        super.setSystemMessageHandler(SystemMessageType.Tracker, h);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#getProcessorId()
     */
    @Override
    protected String getProcessorId() {
        return processorId;
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
        super.start();
    }
}
