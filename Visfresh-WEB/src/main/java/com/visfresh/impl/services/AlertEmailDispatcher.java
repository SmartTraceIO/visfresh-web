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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertEmailDispatcher extends AbstractAssyncSystemMessageDispatcher {
    private final String dispatcherAlias;

    /**
     * @param env spring environment.
     */
    @Autowired
    public AlertEmailDispatcher(final Environment env) {
        super(SystemMessageType.AlertEmail);
        setBatchLimit(Integer.parseInt(env.getProperty("alert.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("alert.dispatcher.retryLimit", "5")));
        setNumThreads(Integer.parseInt(env.getProperty("alert.dispatcher.numThreads", "1")));
        setInactiveTimeOut(Long.parseLong(env.getProperty("alert.dispatcher.retryLimit", "3000")));
        dispatcherAlias = env.getProperty("alertemail.dispatcher.id", "alertemail");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#getProcessorId()
     */
    @Override
    protected String getBaseProcessorId() {
        return getInstanceId() + "." + dispatcherAlias;
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
