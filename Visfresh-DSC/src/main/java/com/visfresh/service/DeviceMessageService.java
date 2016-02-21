/**
 *
 */
package com.visfresh.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.db.DeviceCommandDao;
import com.visfresh.db.DeviceDao;
import com.visfresh.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceMessageService {
    private static int NUMBER_OF_MESSAGES_IN_CRITICAL_BATH = 8;
    private static final Logger log = LoggerFactory.getLogger(DeviceMessageService.class);

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private DeviceCommandDao deviceCommandDao;

    /**
     * Default constructor.
     */
    public DeviceMessageService() {
        super();
    }

    /**
     * @param msgs
     * @return
     */
    public DeviceCommand process(final List<DeviceMessage> msgs) {
        DeviceCommand cmd = null;
        Device device = null;

        for (final DeviceMessage msg : msgs) {
            //attempt to load device
            if (device == null) {
                device = getDeviceByImei(msg.getImei());
            }

            if (device != null) {
                if (cmd == null) {
                    final List<DeviceCommand> commands = getCommandsForDevice(device.getImei());
                    if (!commands.isEmpty()) {
                        if (msgs.size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
                            cmd = commands.get(0);
                            deleteCommand(cmd);
                        } else {
                            log.warn("Given message batch has " + NUMBER_OF_MESSAGES_IN_CRITICAL_BATH
                                    + " messages, the device command" + commands.get(0) + " will postponed");
                        }
                    }
                }

                if (msg.getType() == DeviceMessageType.RSP) {
                    //process response to server command
                    log.debug("Device " + msg.getImei() + " has sent command response: " + msg.getMessage());
                } else {
                    saveDeviceMessage(msg);
                }
            } else {
                log.warn("Not found registered device " + msg.getImei());
                break;
            }
        }

        return cmd;
    }

    /**
     * @param msg the device message.
     */
    protected void saveDeviceMessage(final DeviceMessage msg) {
        messageDao.create(msg);
    }
    /**
     * @param cmd the device command.
     */
    protected void deleteCommand(final DeviceCommand cmd) {
        deviceCommandDao.delete(cmd);
    }
    /**
     * @param imei device IMEI.
     * @return
     */
    protected List<DeviceCommand> getCommandsForDevice(final String imei) {
        return deviceCommandDao.getFoDevice(imei);
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    protected Device getDeviceByImei(final String imei) {
        return deviceDao.getByImei(imei);
    }
}
