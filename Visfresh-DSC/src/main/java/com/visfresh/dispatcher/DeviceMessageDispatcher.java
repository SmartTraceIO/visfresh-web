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

import com.visfresh.DeviceMessage;
import com.visfresh.Location;
import com.visfresh.config.DeviceConstants;
import com.visfresh.db.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceMessageDispatcher extends AbstractDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceMessageDispatcher.class);

    @Autowired
    private LocationService locationService;
    @Autowired
    private DeviceConstants deviceConstants;
    @Autowired
    private SystemMessageDao systemMessageDao;

    /**
     * Default constructor..
     */
    public DeviceMessageDispatcher() {
        super();
        setProcessorId("devicemsg");
    }

    @Override
    public int processMessages() {
        try {
            //mark messages for processing
            dao.markDeviceMessagesForProcess(getProcessorId(), getBatchLimit());
        } catch (final Throwable e) {
            log.error("Failed to mark messages for process", e);
            return 0;
        }

        final List<DeviceMessage> msgs = dao.getDeviceMessagesForProcess(getProcessorId());
        for (final DeviceMessage m : msgs) {
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
    private void processMessage(final DeviceMessage m) throws RetryableException {
        final Location location = getLocationService().getLocation(
                m.getImei(), m.getStations());
        check00(location);

        log.debug("Location (" + location + ") has detected for message " + m);
        systemMessageDao.sendSystemMessageFor(m, location);
    }

    /**
     * @param loc location.
     * @throws RetryableException
     */
    private void check00(final Location loc) throws RetryableException {
        if ((Math.abs(loc.getLatitude()) + Math.abs(loc.getLongitude())) < 0.000001) {
            final RetryableException exc = new RetryableException("Invalid location has detected by UnwiredLabs: "
                    + loc);
            exc.setCanRetry(true);
            exc.setNumberOfRetry(3);
            throw exc;
        }
    }

    /**
     * @return the locationService
     */
    public LocationService getLocationService() {
        return locationService;
    }
    /**
     * @param locationService the locationService to set
     */
    public void setLocationService(final LocationService locationService) {
        this.locationService = locationService;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setBatchLimit(int)
     */
    @Override
    @Value("${deviceMessages.batchLimit}")
    public void setBatchLimit(final int limit) {
        super.setBatchLimit(limit);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setInactiveTimeOut(long)
     */
    @Override
    @Value("${deviceMessages.inactiveTimeOut}")
    public void setInactiveTimeOut(final long inactiveTimeOut) {
        super.setInactiveTimeOut(inactiveTimeOut);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setProcessorId(java.lang.String)
     */
    @Override
    @Value("${deviceMessages.processorId}")
    public String setProcessorId(final String id) {
        return super.setProcessorId(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setRetryLimit(int)
     */
    @Override
    @Value("${deviceMessages.retryLimit}")
    public void setRetryLimit(final int retryLimit) {
        super.setRetryLimit(retryLimit);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#setRetryTimeOut(long)
     */
    @Override
    @Value("${deviceMessages.retryTimeOut}")
    public void setRetryTimeOut(final long retryTimeOut) {
        super.setRetryTimeOut(retryTimeOut);
    }
}
