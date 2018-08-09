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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.IncommingRequest;
import com.visfresh.db.MessageSnapshootDao;
import com.visfresh.mail.mock.MockEmailMessage;

import au.smarttrace.geolocation.GeoLocationResponse;
import au.smarttrace.geolocation.Location;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageServiceTest extends DeviceMessageService {
    private final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

    private final Map<String, Device> devices = new HashMap<String, Device>();
    private final Map<String, List<DeviceCommand>> commands = new HashMap<>();
    private final List<DeviceMessage> sentMessages = new LinkedList<>();
    private final List<MockEmailMessage> alerts =  new LinkedList<>();
    private Device device;

    private final Set<String> signatures = new HashSet<>();
    private final Map<DeviceMessage, List<StationSignal>> requests = new HashMap<>();
    private long lastId;

    private final List<GeoLocationResponse> responses = new LinkedList<>();

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

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m1, new LinkedList<>());
        msgs.addMessage(m2, new LinkedList<>());

        process(msgs);

        assertEquals(2, requests.size());
    }
    @Test
    public void testInactiveDevice() {
        device.setActive(false);

        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m1, new LinkedList<>());
        msgs.addMessage(m2, new LinkedList<>());

        process(msgs);

        assertEquals(0, requests.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testProcessRspMessage() {
        final DeviceMessage m1 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);
        final DeviceMessage m2 = createDeviceMessage(device.getImei(), DeviceMessageType.RSP);
        final DeviceMessage m3 = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m1, new LinkedList<>());
        msgs.addMessage(m2, new LinkedList<>());
        msgs.addMessage(m3, new LinkedList<>());

        process(msgs);

        assertEquals(2, requests.size());
    }
    @Test
    public void testDeviceNotRegistered() {
        final DeviceMessage m = createDeviceMessage("123", DeviceMessageType.INIT);

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m, new LinkedList<>());

        process(msgs);

        assertEquals(0, requests.size());
    }
    @Test
    public void testIgnoreAlreadyProcessed() {
        final Device device = addDevice("10982734098127304");
        final DeviceMessage m = createDeviceMessage(device.getImei(), DeviceMessageType.INIT);

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m, new LinkedList<>());

        process(msgs);
        assertEquals(1, requests.size());

        //check process again
        requests.clear();
        process(msgs);
        assertEquals(0, requests.size());
    }
    @Test
    public void testEmptyMessageList() {
        final IncommingRequest msgs = new IncommingRequest();

        process(msgs);
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

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m2, new LinkedList<>());
        assertNull(process(msgs));

        msgs.clear();
        msgs.addMessage(m1, new LinkedList<>());
        assertNotNull(process(msgs));

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

        final IncommingRequest msgs = new IncommingRequest();
        msgs.addMessage(m1, new LinkedList<>());
        assertEquals("anycommand", process(msgs).getCommand());

        //check deleted command
        assertEquals(0, commands.get(m1.getImei()).size());
    }
    @Test
    public void testNotReturnCommandFor8Batch() {
        final IncommingRequest msgs = new IncommingRequest();
        for (int i = 0; i < 8; i++) {
            msgs.addMessage(createDeviceMessage(device.getImei(), DeviceMessageType.INIT), new LinkedList<>());
        }

        final DeviceCommand cmd1 = createCommand("anycommand-1");
        commands.get(device.getImei()).add(cmd1);

        assertNull(process(msgs));
        msgs.remove(msgs.getMessages().get(0));
        assertNotNull(process(msgs));
    }
    @Test
    public void testHandleResolvedLocationsError() {
        final DeviceMessage msg = createDeviceMessage("32490870987978", DeviceMessageType.AUT);

        addResponse(RequestStatus.error, null, msg);
        addResponse(RequestStatus.error, null, msg);

        handleResolvedLocations();

        assertEquals(2, sentMessages.size());

        //check location is null
        assertNull(sentMessages.get(0).getLocation());
        assertNull(sentMessages.get(1).getLocation());
    }
    @Test
    public void testHandleResolvedLocationsSuccess() {
        final DeviceMessage msg = createDeviceMessage("32490870987978", DeviceMessageType.AUT);

        addResponse(RequestStatus.success, new Location(1.0, 2.0), msg);
        addResponse(RequestStatus.success, new Location(1.0, 2.0), msg);

        handleResolvedLocations();

        assertEquals(2, sentMessages.size());

        //test location set
        assertNotNull(sentMessages.get(0).getLocation());
        assertNotNull(sentMessages.get(1).getLocation());
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
        m.setTypeString(type.name());
        return m;
    }
    /* (non-Javadoc)
     * @see com.visfresh.service.DeviceMessageService#saveLocationResolvingRequest(com.visfresh.DeviceMessage, java.util.List)
     */
    @Override
    protected void saveLocationResolvingRequest(final DeviceMessage msg, final List<StationSignal> signals) {
        requests.put(msg, signals);
    }
    /* (non-Javadoc)
     * @see com.visfresh.service.DeviceMessageService#sendResolvedMessages(com.visfresh.DeviceMessage)
     */
    @Override
    protected void sendResolvedMessage(final DeviceMessage msg) {
        sentMessages.add(msg);
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
    private GeoLocationResponse addResponse(final RequestStatus status,
            final Location loc, final DeviceMessage msg) {
        final GeoLocationResponse r = new GeoLocationResponse();
        r.setLocation(loc);
        r.setStatus(status);
        r.setType(ServiceType.UnwiredLabs);
        try {
            r.setUserData(json.writeValueAsString(msg));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        responses.add(r);
        return r;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#getAndRemoveProcessedResponses(java.lang.String, int)
     */
    @Override
    protected List<GeoLocationResponse> getAndRemoveProcessedResponses(final String sender, final int limit) {
        final List<GeoLocationResponse> result = new LinkedList<>();

        final Iterator<GeoLocationResponse> iter = responses.iterator();
        while (iter.hasNext()) {
            if (result.size() >= limit) {
                break;
            }

            final GeoLocationResponse next = iter.next();
            if (next.getStatus() != null) {
                result.add(next);
                iter.remove();
            }
        }

        return result;
    }
}
