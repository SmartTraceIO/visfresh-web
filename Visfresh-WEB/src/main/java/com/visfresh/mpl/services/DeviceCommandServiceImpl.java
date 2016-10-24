/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandServiceImpl implements DeviceCommandService, SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceCommandServiceImpl.class);

    /**
     * IMEI JSON serialized property.
     */
    private static final String IMEI = "imei";
    /**
     * Command JSON serialized property.
     */
    private static final String COMMAND = "command";

    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private MainSystemMessageDispatcher dispatcher;

    /**
     * Default constructor.
     */
    public DeviceCommandServiceImpl() {
        super();
    }

    @PostConstruct
    public void startUp() {
        dispatcher.setSystemMessageHandler(SystemMessageType.DeviceCommand, this);
    }
    @PreDestroy
    public void shutDown() {
        dispatcher.setSystemMessageHandler(SystemMessageType.DeviceCommand, null);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceCommandService#sendCommand(com.visfresh.entities.DeviceCommand)
     */
    @Override
    public void sendCommand(final DeviceCommand cmd, final Date date) {
        final JsonObject json = new JsonObject();
        json.addProperty(COMMAND, cmd.getCommand());
        json.addProperty(IMEI, cmd.getDevice().getImei());

        dispatcherSendSystemMessage(json.toString(), date);
    }
    /**
     * @param messageBody message body.
     * @param date retry on date.
     */
    protected void dispatcherSendSystemMessage(final String messageBody, final Date date) {
        dispatcher.sendSystemMessage(messageBody, SystemMessageType.DeviceCommand, date);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();
        processCommand(json.get(COMMAND).getAsString(), json.get(IMEI).getAsString());
    }

    /**
     * @param cmd
     */
    protected void processCommand(final String command, final String imei) {
        saveCommand(command, imei);
    }

    /**
     * @param command
     * @param imei
     */
    protected void saveCommand(final String command, final String imei) {
        log.debug("Save to send command " + command + " for device " + imei);
        deviceCommandDao.saveCommand(command, imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceCommandService#shutdownDevice(com.visfresh.entities.Device)
     */
    @Override
    public void shutdownDevice(final Device device, final Date date) {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand(DeviceCommand.SHUTDOWN);
        cmd.setDevice(device);

        sendCommand(cmd, date);
    }
}
