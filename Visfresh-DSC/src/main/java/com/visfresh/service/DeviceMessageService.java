/**
 *
 */
package com.visfresh.service;

import java.util.Iterator;
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
import com.visfresh.db.MessageSnapshootDao;

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
    private MessageSnapshootDao snapshootDao;
    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private InactiveDeviceAlertSender alerter;

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
        if (msgs.size() == 0) {
            return null;
        }

        if (!saveSignature(msgs)) {
            log.warn("The message batch is already processed, will ignored: " + msgs);
            return null;
        }

        Device device = null;
        boolean hasInitMessage = false;

        for (final DeviceMessage msg : msgs) {
            //attempt to load device
            if (device == null) {
                device = getDeviceByImei(msg.getImei());
            }

            if (device == null) {
                log.warn("Not found registered device " + msg.getImei());
                break;
            } else if (!device.isActive()) {
                log.debug("Device " + device.getImei() + " is inactive, message(s) ignored");

                sendAlert("Attempt to send message to inactive device " + msg.getImei(),
                        "Message body:\n" + combineMessages(msgs));
                break;
            } else {
                if (msg.getType() == DeviceMessageType.RSP) {
                    //process response to server command
                    log.debug("Device " + msg.getImei() + " has sent command response: " + msg.getMessage());
                } else {
                    if (msg.getType() == DeviceMessageType.INIT) {
                        hasInitMessage = true;
                    }
                    saveDeviceMessage(msg);
                }
            }
        }

        DeviceCommand cmd = null;
        if (device != null && msgs.size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
            final List<DeviceCommand> commands = getCommandsForDevice(device.getImei());
            if (hasInitMessage) {
                //delete all shutdown commands
                final Iterator<DeviceCommand> iter = commands.iterator();
                while (iter.hasNext()) {
                    final DeviceCommand next = iter.next();
                    if (next.getCommand().toLowerCase().contains("shutdown")) {
                        iter.remove();
                        log.debug("shutdown command has ignored because init message");
                        deleteCommand(next);
                    }
                }
            }

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

        return cmd;
    }
    /**
     * @param msgs list of messages.
     * @return
     */
    protected boolean saveSignature(final List<DeviceMessage> msgs) {
        return this.snapshootDao.saveSignature(msgs);
    }

    /**
     * @param subject
     * @param message
     */
    protected void sendAlert(final String subject, final String message) {
        alerter.sendAlert(new String[0], subject, message);
    }
    /**
     * @param msgs
     * @return
     */
    private String combineMessages(final List<DeviceMessage> msgs) {
        final StringBuilder sb = new StringBuilder();
        for (final DeviceMessage m : msgs) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(m.toString());
        }
        return sb.toString();
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
