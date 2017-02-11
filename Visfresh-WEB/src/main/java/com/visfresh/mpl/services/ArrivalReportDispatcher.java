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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalReportDispatcher extends AbstractSystemMessageDispatcher {
    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public ArrivalReportDispatcher(final Environment env) {
        super(SystemMessageType.ArrivalReport);
        processorId = env.getProperty("main.dispatcher.baseProcessorId", "arrival-report");
        setBatchLimit(Integer.parseInt(env.getProperty("arrival.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("arrival.dispatcher.retryLimit", "5")));
        //number of threads should be hardcoded to 1
        //setNumThreads(Integer.parseInt(env.getProperty("main.dispatcher.numThreads", "1")));
        setNumThreads(1);
        setInactiveTimeOut(Long.parseLong(env.getProperty("arrival.dispatcher.retryLimit", "3000")));
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
