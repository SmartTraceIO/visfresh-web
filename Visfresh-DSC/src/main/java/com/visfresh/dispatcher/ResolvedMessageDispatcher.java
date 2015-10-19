/**
 *
 */
package com.visfresh.dispatcher;

import java.util.List;

import org.opengts.db.StatusCodes;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.Device;
import org.opengts.db.tables.EventData;
import org.opengts.dbtools.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessageType;
import com.visfresh.ResolvedDeviceMessage;
import com.visfresh.config.DeviceConstants;

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
        final String accountID = this.deviceConstants.getAccountId();
        final String imei = m.getImei();

        //load device
        Device device;
        try {
            final Account account = Account.getAccount(accountID);
            device = Device.getDevice(account, imei);

            if (device == null) {
                device = Device.createNewDevice(account, imei, accountID + "/" + imei);
            }
        } catch (final DBException e) {
            throw new RetryableException("Failed to load device " + imei, e);
        }

        final EventData.Key evKey = new EventData.Key(accountID, imei,
                m.getTime().getTime() / 1000l, getStatusCode(m.getType()));
        final EventData evdb = evKey.getDBRecord();
        evdb.setLatitude(m.getLocation().getLatitude());
        evdb.setLongitude(m.getLocation().getLongitude());
        evdb.setBatteryLevel(m.getBattery());
        evdb.setBatteryTemp(m.getTemperature());

        if (device.insertEventData(evdb)) {
            log.debug("Device message has saved for " + imei);
        } else {
            // -- this will display an error if it was unable to store the event
            throw new RetryableException("Failed to save event data for device " + imei);
        }
    }

    /**
     * @param type the device message type.
     * @return the status code.
     * @throws RetryableException
     */
    private int getStatusCode(final DeviceMessageType type) throws RetryableException {
        switch (type) {
            case AUT:
                return StatusCodes.STATUS_LOCATION;
            case BRT:
                return StatusCodes.STATUS_LIGHTING_BRIGHTER;
            case DRK:
                return StatusCodes.STATUS_LIGHTING_DARKER;
            case INIT:
                return StatusCodes.STATUS_INITIALIZED;
            case STP:
                return StatusCodes.STATUS_ALARM_OFF;
            case VIB:
                return StatusCodes.STATUS_VIBRATION_ON;
                default:
                    throw new RetryableException("Undefined message type: " + type);
        }
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
