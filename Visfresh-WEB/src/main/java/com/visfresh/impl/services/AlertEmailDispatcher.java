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
import com.visfresh.services.AbstractAssyncSystemMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertEmailDispatcher extends AbstractAssyncSystemMessageDispatcher {
    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public AlertEmailDispatcher(final Environment env) {
        super(SystemMessageType.AlertEmail);
        processorId = buildInstanceId(env, "alertemail.dispatcher.id", "alertemail");
        setBatchLimit(Integer.parseInt(env.getProperty("alert.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("alert.dispatcher.retryLimit", "5")));
        //number of threads should be hardcoded to 1
        //setNumThreads(Integer.parseInt(env.getProperty("main.dispatcher.numThreads", "1")));
        setNumThreads(1);
        setInactiveTimeOut(Long.parseLong(env.getProperty("alert.dispatcher.retryLimit", "3000")));
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
