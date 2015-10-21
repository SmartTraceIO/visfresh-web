/**
 *
 */
package com.visfresh.dispatcher;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.visfresh.ResolvedDeviceMessage;
import com.visfresh.config.DeviceConstants;
import com.visfresh.db.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ResolvedMessageDispatcher extends AbstractDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ResolvedMessageDispatcher.class);
    @Autowired
    private DeviceConstants deviceConstants;
    @Autowired
    private SystemMessageDao systemMessageDao;

    /**
     * Default constructor..
     */
    public ResolvedMessageDispatcher() {
        super();
        setProcessorId("resolvedmsg");
    }

    @Override
    public int processMessages() {
        try {
            //mark messages for processing
            dao.markResolvedMessagesForProcess(getProcessorId(), getBatchLimit());
        } catch (final Throwable e) {
            log.error("Failed to mark messages for process", e);
            return 0;
        }

        final List<ResolvedDeviceMessage> msgs = dao.getResolvedMessagesForProcess(getProcessorId());
        for (final ResolvedDeviceMessage m : msgs) {
            try {
                processMessage(m);
                handleSuccess(m);
            } catch (final Throwable e) {
                handleError(m, e);
            }
        }

        return msgs.size();
    }

    /**
     * @param m device message.
     * @throws RetryableException exception.
     */
    private void processMessage(final ResolvedDeviceMessage m) throws RetryableException {
        systemMessageDao.sendSystemMessageFor(m);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setBatchLimit(int)
     */
    @Override
    @Value("${resolvedMessages.batchLimit}")
    public void setBatchLimit(final int limit) {
        super.setBatchLimit(limit);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setInactiveTimeOut(long)
     */
    @Override
    @Value("${resolvedMessages.inactiveTimeOut}")
    public void setInactiveTimeOut(final long inactiveTimeOut) {
        super.setInactiveTimeOut(inactiveTimeOut);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setProcessorId(java.lang.String)
     */
    @Override
    @Value("${resolvedMessages.processorId}")
    public String setProcessorId(final String id) {
        return super.setProcessorId(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setRetryLimit(int)
     */
    @Override
    @Value("${resolvedMessages.retryLimit}")
    public void setRetryLimit(final int retryLimit) {
        super.setRetryLimit(retryLimit);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setRetryTimeOut(long)
     */
    @Override
    @Value("${resolvedMessages.retryTimeOut}")
    public void setRetryTimeOut(final long retryTimeOut) {
        super.setRetryTimeOut(retryTimeOut);
    }
    /**
     * @return the deviceConstants
     */
    public DeviceConstants getDeviceConstants() {
        return deviceConstants;
    }
}
