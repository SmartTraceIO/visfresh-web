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
public class SmsMessageDispatcher extends AbstractSystemMessageDispatcher {
    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public SmsMessageDispatcher(final Environment env) {
        super(SystemMessageType.SMS);
        processorId = env.getProperty("sms.dispatcher.baseProcessorId", "sms");

        setBatchLimit(Integer.parseInt(env.getProperty("sms.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("sms.dispatcher.retryLimit", "5")));
        setNumThreads(Integer.parseInt(env.getProperty("sms.dispatcher.numThreads", "1")));
        setInactiveTimeOut(Long.parseLong(env.getProperty("sms.dispatcher.retryLimit", "3000")));
    }

    /**
     * @param h SMS handler.
     */
    public void setSmsHandler(final SystemMessageHandler h) {
        super.setSystemMessageHandler(SystemMessageType.SMS, h);
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
