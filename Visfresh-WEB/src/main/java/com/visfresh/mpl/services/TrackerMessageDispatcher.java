/**
 *
 */
package com.visfresh.mpl.services;

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
        super(env, SystemMessageType.Tracker);
        processorId = env.getProperty("tracker.dispatcher.baseProcessorId", "tracker-dispatcher");
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
