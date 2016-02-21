/**
 *
 */
package com.visfresh.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageServiceTest extends DeviceMessageService {
    private long lastId;

    private final Map<String, Device> devices = new HashMap<String, Device>();
    private final Map<String, List<DeviceCommand>> commands = new HashMap<>();
    private final List<DeviceMessage> messages = new LinkedList<>();
    private Device device;

    /**
     * Default constructor.
     */
    public DeviceMessageServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        this.device = addDevice("1289374698773");
    }

    /**
     * @param imei
     */
    private Device addDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setName("JUnit-" + imei);
        d.setDescription("Test device");

        devices.put(d.getImei(), d);
        commands.put(imei, new LinkedList<DeviceCommand>());
        return d;
    }

    @Test
    public void testProcessOrdinaryMessage() {
        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DeviceMessage> msgs = new LinkedList<DeviceMessage>();
        msgs.add(m1);
        msgs.add(m2);

        process(msgs);

        assertEquals(2, messages.size());
        assertEquals(m1.getId(), messages.get(0).getId());
        assertEquals(m2.getId(), messages.get(1).getId());
    }
    @Test
    public void testProcessRspMessage() {
        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.RSP);
        final DeviceMessage m3 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DeviceMessage> msgs = new LinkedList<DeviceMessage>();
        msgs.add(m1);
        msgs.add(m2);
        msgs.add(m3);

        process(msgs);

        assertEquals(2, messages.size());
        assertEquals(m1.getId(), messages.get(0).getId());
        assertEquals(m3.getId(), messages.get(1).getId());
    }
    @Test
    public void testDeviceNotRegistered() {
        final DeviceMessage m = createDeviceMessage("123", DeviceMessageType.INIT);

        final List<DeviceMessage> msgs = new LinkedList<DeviceMessage>();
        msgs.add(m);

        process(msgs);

        assertEquals(0, messages.size());
    }
    @Test
    public void testReturnCommand() {
        final Device d1 = addDevice("10982734098127304");
        final Device d2 = addDevice("10982734324898344");

        final DeviceMessage m1 = createDeviceMessage(d1.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(d2.getImei(), DeviceMessageType.INIT);

        final DeviceCommand cmd1 = createCommand("anycommand-1");
        final DeviceCommand cmd2 = createCommand("anycommand-2");

        commands.get(m1.getImei()).add(cmd1);
        commands.get(m1.getImei()).add(cmd2);

        final List<DeviceMessage> msgs = new LinkedList<DeviceMessage>();
        msgs.add(m2);
        assertNull(process(msgs));

        msgs.clear();
        msgs.add(m1);
        assertNotNull(process(msgs));

        //check deleted command
        assertEquals(1, commands.get(m1.getImei()).size());
        assertEquals(cmd2.getCommand(), commands.get(m1.getImei()).get(0).getCommand());
    }
    @Test
    public void testNotReturnCommandFor8Batch() {
        final List<DeviceMessage> msgs = new LinkedList<DeviceMessage>();
        for (int i = 0; i < 8; i++) {
            msgs.add(createDeviceMessage(device.getImei(), DeviceMessageType.INIT));
        }

        final DeviceCommand cmd1 = createCommand("anycommand-1");
        commands.get(device.getImei()).add(cmd1);

        assertNull(process(msgs));
        msgs.remove(0);
        assertNotNull(process(msgs));
    }

    /**
     * @param command command text.
     * @return command.
     */
    private DeviceCommand createCommand(final String command) {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand("anycommand");
        cmd.setId(++lastId);
        return cmd;
    }
    /**
     * @param imei device IMEI.
     * @param type device message type.
     * @return message.
     */
    private DeviceMessage createDeviceMessage(final String imei, final DeviceMessageType type) {
        final DeviceMessage m = new DeviceMessage();
        m.setId(++lastId);
        m.setImei(imei);
        m.setTime(new Date());
        m.setType(type);
        m.setTypeString(type.name());
        return m;
    }
    /**
     * @param msg the device message.
     */
    @Override
    protected void saveDeviceMessage(final DeviceMessage msg) {
        messages.add(msg);
    }
    /**
     * @param cmd the device command.
     */
    @Override
    protected void deleteCommand(final DeviceCommand cmd) {
        for (final Map.Entry<String, List<DeviceCommand>> e : commands.entrySet()) {
            e.getValue().remove(cmd);
        }
    }
    /**
     * @param imei device IMEI.
     * @return
     */
    @Override
    protected List<DeviceCommand> getCommandsForDevice(final String imei) {
        return new LinkedList<DeviceCommand>(commands.get(imei));
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    @Override
    protected Device getDeviceByImei(final String imei) {
        return devices.get(imei);
    }
}
