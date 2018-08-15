/**
 *
 */
package com.visfresh.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.db.MessageSnapshootDao;
import com.visfresh.mail.mock.MockEmailMessage;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.DeviceMessageType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageServiceTest extends DeviceMessageService {
    private final Map<String, Device> devices = new HashMap<String, Device>();
    private final Map<String, List<DeviceCommand>> commands = new HashMap<>();
    private final List<MockEmailMessage> alerts =  new LinkedList<>();
    private Device device;

    private final Set<String> signatures = new HashSet<>();
    private final List<DataWithGsmInfo<DeviceMessage>> requests = new LinkedList<>();
    private long lastId;

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
        d.setActive(true);

        devices.put(d.getImei(), d);
        commands.put(imei, new LinkedList<DeviceCommand>());
        return d;
    }

    @Test
    public void testProcessOrdinaryMessage() {
        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m1));
        msgs.add(createData(m2));

        process(msgs, "raw data");

        assertEquals(2, requests.size());
    }
    @Test
    public void testInactiveDevice() {
        device.setActive(false);

        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m1));
        msgs.add(createData(m2));

        process(msgs, "raw data");

        assertEquals(0, requests.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testProcessRspMessage() {
        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.RSP);
        final DeviceMessage m3 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m1));
        msgs.add(createData(m2));
        msgs.add(createData(m3));

        process(msgs, "raw data");

        assertEquals(2, requests.size());
    }
    @Test
    public void testDeviceNotRegistered() {
        final DeviceMessage m = createDeviceMessage("123", DeviceMessageType.INIT);

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m));

        process(msgs, "raw data");

        assertEquals(0, requests.size());
    }

    @Test
    public void testIgnoreAlreadyProcessed() {
        final Device device = addDevice("10982734098127304");
        final DeviceMessage m = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m));

        process(msgs, "raw data");
        assertEquals(1, requests.size());

        //check process again
        requests.clear();
        process(msgs, "raw data");
        assertEquals(0, requests.size());
    }
    @Test
    public void testEmptyMessageList() {
        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();

        process(msgs, "raw data");
        assertEquals(0, requests.size());
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

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m2));
        assertNull(process(msgs, "raw data"));

        msgs.clear();
        msgs.add(createData(m1));
        assertNotNull(process(msgs, "raw data"));

        //check deleted command
        assertEquals(1, commands.get(m1.getImei()).size());
        assertEquals(cmd2.getCommand(), commands.get(m1.getImei()).get(0).getCommand());
    }
    @Test
    public void testIgnoreShutdownIfInit() {
        final Device d1 = addDevice("10982734098127304");

        final DeviceMessage m1 = createDeviceMessage(d1.getImei(), DeviceMessageType.INIT);

        commands.get(m1.getImei()).add(createCommand("SHUTDOWN#"));
        commands.get(m1.getImei()).add(createCommand("anycommand"));
        commands.get(m1.getImei()).add(createCommand("SHUTDOWN#"));

        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        msgs.add(createData(m1));
        assertEquals("anycommand", process(msgs, "raw data").getCommand());

        //check deleted command
        assertEquals(0, commands.get(m1.getImei()).size());
    }
    @Test
    public void testNotReturnCommandFor8Batch() {
        final List<DataWithGsmInfo<DeviceMessage>> msgs = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            final DeviceMessage msg = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
            msgs.add(createData(msg));
        }

        final DeviceCommand cmd1 = createCommand("anycommand-1");
        commands.get(device.getImei()).add(cmd1);

        assertNull(process(msgs, "raw data"));
        msgs.remove(msgs.get(0));
        assertNotNull(process(msgs, "raw data"));
    }
    /**
     * @param m
     * @return
     */
    private DataWithGsmInfo<DeviceMessage> createData(final DeviceMessage m) {
        final GsmLocationResolvingRequest gsm = new GsmLocationResolvingRequest();
        gsm.setImei(m.getImei());
        gsm.setRadio("gsm");
        gsm.getStations().add(new StationSignal());

        final DataWithGsmInfo<DeviceMessage> data = new DataWithGsmInfo<>();
        data.setUserData(m);
        data.setGsmInfo(gsm);
        return data;
    }
    /**
     * @param command command text.
     * @return command.
     */
    private DeviceCommand createCommand(final String command) {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand(command);
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
        m.setImei(imei);
        m.setTime(new Date());
        m.setType(type);
        return m;
    }
    /* (non-Javadoc)
     * @see com.visfresh.service.DeviceMessageService#saveLocationResolvingRequest(au.smarttrace.geolocation.DataWithGsmInfo)
     */
    @Override
    protected void saveLocationResolvingRequest(final DataWithGsmInfo<DeviceMessage> info) {
        requests.add(info);
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
    /* (non-Javadoc)
     * @see com.visfresh.service.DeviceMessageService#sendAlert(java.lang.String[], java.lang.String, java.lang.String)
     */
    @Override
    protected void sendAlert(final String subject, final String message) {
        final MockEmailMessage m = new MockEmailMessage();
        m.setAddresses(new String[0]);
        m.setSubject(subject);
        m.setMessage(message);

        alerts.add(m);
    }
    /* (non-Javadoc)
     * @see com.visfresh.service.DeviceMessageService#saveSignature(java.util.Collection)
     */
    @Override
    protected boolean saveSignature(final List<DeviceMessage> msgs) {
        return signatures.add(MessageSnapshootDao.createSignature(msgs));
    }
}
