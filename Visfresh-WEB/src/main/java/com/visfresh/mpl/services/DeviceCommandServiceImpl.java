/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.Shipment;
import com.visfresh.services.DeviceCommandService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandServiceImpl implements DeviceCommandService {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommandServiceImpl.class);

    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public DeviceCommandServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceCommandService#sendCommand(com.visfresh.entities.DeviceCommand)
     */
    @Override
    public void sendCommand(final DeviceCommand cmd) {
        deviceCommandDao.save(cmd);

        if (cmd.getCommand().equalsIgnoreCase(DeviceCommand.SHUTDOWN)) {
            notifyDeviceShuttingDown(cmd.getDevice().getImei());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceCommandService#shutdownDevice(com.visfresh.entities.Device)
     */
    @Override
    public void shutdownDevice(final Device device) {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand(DeviceCommand.SHUTDOWN);
        cmd.setDevice(device);

        sendCommand(cmd);
    }
    /**
     * @param imei device IMEI.
     */
    private void notifyDeviceShuttingDown(final String imei) {
        final Shipment s = shipmentDao.findActiveShipment(imei);

        if (s != null) {
            log.debug("Device shutdown event has sent for " + imei);
            s.setDeviceShutdownTime(new Date());
            shipmentDao.save(s);
        } else {
            log.warn("Failed to find active shipment for device "
                    + imei + " for set device shutdown time.");
        }
    }
}
