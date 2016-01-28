/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.RetryableException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommandServiceTest extends DeviceCommandServiceImpl {
    /**
     * List of saved commands.
     */
    private final List<String> savedCommands = new LinkedList<>();
    private final List<String> deviceShutDownNotifications = new LinkedList<>();
    private final List<SystemMessage> systemMessages = new LinkedList<>();
    private Device device;

    /**
     * Default constructor.
     */
    public DeviceCommandServiceTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.DeviceCommandServiceImpl#saveCommand(java.lang.String, java.lang.String)
     */
    @Override
    protected void saveCommand(final String command, final String imei) {
        savedCommands.add(prepareToSave(command, imei));
    }

    /**
     * @param command
     * @param imei
     * @return
     */
    protected String prepareToSave(final String command, final String imei) {
        return command + ": " + imei;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.DeviceCommandServiceImpl#notifyDeviceShuttingDown(java.lang.String)
     */
    @Override
    protected void notifyDeviceShuttingDown(final String imei) {
        deviceShutDownNotifications.add(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.DeviceCommandServiceImpl#dispatcherSendSystemMessage(java.lang.String, com.visfresh.entities.SystemMessageType, java.util.Date)
     */
    @Override
    protected void dispatcherSendSystemMessage(final String messageBody,
            final Date date) {
        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo(messageBody);
        msg.setRetryOn(date);
        msg.setTime(new Date());
        msg.setType(SystemMessageType.DeviceCommand);

        systemMessages.add(msg);
    }

    @Before
    public void setUp() {
        final Device d = new Device();
        d.setImei("1234567899832470");

        this.device = d;
    }
    //tests
    @Test
    public void testHandleSystemMessage() throws RetryableException {
        final String command = "any";
        final SystemMessage msg = createSystemMessage(command);
        handle(msg);

        assertEquals(1, savedCommands.size());
        assertEquals(prepareToSave(command, device.getImei()), savedCommands.get(0));
    }
    @Test
    public void testHandleDeviceShutdownMessage() throws RetryableException {
        final String command = DeviceCommand.SHUTDOWN;
        final SystemMessage msg = createSystemMessage(command);
        handle(msg);

        assertEquals(1, savedCommands.size());
        assertEquals(1, this.deviceShutDownNotifications.size());
        assertEquals(device.getImei(), this.deviceShutDownNotifications.get(0));
    }
    @Test
    public void testSendDeviceCommand() {
        final String command = "any";
        final Date date = new Date(System.currentTimeMillis() - 1000000000l);

        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand(command);
        cmd.setDevice(device);

        sendCommand(cmd, date);

        //check system message created and sent
        final SystemMessage msg = systemMessages.get(0);
        assertEquals(date, msg.getRetryOn());

        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();
        assertEquals(json.get("command").getAsString(), command);
        assertEquals(json.get("imei").getAsString(), device.getImei());
    }

    /**
     * @param command device command.
     * @return system message for given command.
     */
    private SystemMessage createSystemMessage(final String command) {
        final JsonObject json = new JsonObject();
        json.addProperty("command", command);
        json.addProperty("imei", device.getImei());

        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo(json.toString());
        msg.setRetryOn(new Date());
        msg.setTime(new Date());
        msg.setType(SystemMessageType.DeviceCommand);
        return msg;
    }
}
