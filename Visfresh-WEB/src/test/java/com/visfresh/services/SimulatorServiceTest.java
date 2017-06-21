/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.visfresh.controllers.SimulatorController;
import com.visfresh.entities.Device;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.impl.services.DeviceDcsNativeEvent;
import com.visfresh.impl.services.SimulatorServiceImpl;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorServiceTest extends SimulatorServiceImpl {
    private long id = 1;
    private final Map<Long, SimulatorDto> simulators = new HashMap<>();
    private final List<ShortTrackerEvent> events = new LinkedList<>();
    private final List<SystemMessage> messages = new LinkedList<>();
    private final DeviceDcsNativeEventSerializer serializer = new DeviceDcsNativeEventSerializer();

    /**
     * Default constructor.
     */
    public SimulatorServiceTest() {
        super();
    }

    @Test
    public void testStart() {
        //create users
        final User u1 = createUser("abra1@kada.bra");
        final User u2 = createUser("abra2@kada.bra");

        //create devices
        final Device d1 = createDevice("09870934287344");
        final Device d2 = createDevice("09238470989284");

        //create simulator for user
        createSimulator(u2, d1);

        //create tracker events
        final long dt = 10000l;
        final long t0 = System.currentTimeMillis() - 20 * dt;

        createTrackerEvent(d1, new Date(t0 + 1 * dt));
        createTrackerEvent(d2, new Date(t0 + 1 * dt));

        final ShortTrackerEvent e1 = createTrackerEvent(d1, new Date(t0 + 2 * dt));
        createTrackerEvent(d2, new Date(t0 + 2 * dt));

        final ShortTrackerEvent e2 = createTrackerEvent(d1, new Date(t0 + 3 * dt));
        createTrackerEvent(d2, new Date(t0 + 3 * dt));

        createTrackerEvent(d1, new Date(t0 + 4 * dt));
        createTrackerEvent(d2, new Date(t0 + 4 * dt));

        final Date startDate = new Date(t0 + 1 * dt + dt / 2);
        final Date endDate = new Date(t0 + 4 * dt - dt / 2);

        //left user
        final int velosity = 10;
        try {
            startSimulator(u1, startDate, endDate, velosity);
            throw new AssertionFailedError("Exception simulator not created should be thrown");
        } catch (final Exception e) {
            //OK
        }
        assertEquals(0, messages.size());

        //correct user
        startSimulator(u2, startDate, endDate, velosity);
        assertEquals(2, messages.size());

        //check time interval
        final SystemMessage lastMsg = messages.get(1);
        assertEquals((e2.getTime().getTime() - e1.getTime().getTime()) / (double) velosity,
                lastMsg.getTime().getTime() - messages.get(0).getTime().getTime(), 1000);

        //check assigned device
        final DeviceDcsNativeEvent dne = serializer.parseDeviceDcsNativeEvent(
                SerializerUtils.parseJson(lastMsg.getMessageInfo()));
        assertEquals(findSimulator(u2).getTargetDevice(), dne.getImei());
        assertEquals(lastMsg.getTime().getTime(), dne.getDate().getTime(), 1000);

        //check simulator started
        assertTrue(this.findSimulator(u2).isStarted());
    }

    @Test
    public void testStop() {
        //create users
        final User u1 = createUser("abra1@kada.bra");
        final User u2 = createUser("abra2@kada.bra");

        //create devices
        final Device d1 = createDevice("09870934287344");
        final Device d2 = createDevice("09238470989284");

        //create simulator for user
        final SimulatorDto sim1 = createSimulator(u1, d1);
        final SimulatorDto sim2 = createSimulator(u2, d2);

        final String imei1 = sim1.getTargetDevice();
        final String imei2 = sim2.getTargetDevice();

        createDcsNativeSystemMessage(imei1);
        createDcsNativeSystemMessage(imei2);
        sendSystemMessage("any event", SystemMessageType.DeviceCommand, new Date());

        createDcsNativeSystemMessage(imei1);
        createDcsNativeSystemMessage(imei2);
        sendSystemMessage("any event", SystemMessageType.DeviceCommand, new Date());

        createDcsNativeSystemMessage(imei1);
        createDcsNativeSystemMessage(imei2);
        sendSystemMessage("any event", SystemMessageType.DeviceCommand, new Date());

        stopSimulator(u1);

        //check is stopped
        assertFalse(findSimulator(u1).isStarted());
        //check system messages for given device deleted
        assertEquals(6, messages.size());
        //check correct device deleted
        assertEquals(imei2, serializer.parseDeviceDcsNativeEvent(
                SerializerUtils.parseJson(messages.get(0).getMessageInfo())).getImei());
    }

    /**
     * @param device
     */
    private void createDcsNativeSystemMessage(final String device) {
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setImei(device);
        e.setType(TrackerEventType.AUT.name());
        e.setDate(new Date());

        sendSystemMessage(serializer.toJson(e).toString(), SystemMessageType.Tracker, new Date());
    }
    /**
     * @param user user.
     * @param device device.
     * @return
     */
    private SimulatorDto createSimulator(final User user, final Device device) {
        final SimulatorDto sim = new SimulatorDto();
        sim.setUser(user.getEmail());
        sim.setSourceDevice(device.getImei());
        sim.setTargetDevice(createDevice(SimulatorController.generateImei(user.getId())).getImei());
        simulators.put(user.getId(), sim);
        return sim;
    }
    /**
     * @param device
     * @param date
     * @return
     */
    private ShortTrackerEvent createTrackerEvent(final Device device, final Date date) {
        final ShortTrackerEvent te = new ShortTrackerEvent();
        te.setId(id++);
        te.setDeviceImei(device.getImei());
        te.setType(TrackerEventType.AUT);
        te.setTime(date);
        te.setCreatedOn(new Date());
        events.add(te);
        return te;
    }
    /**
     * @param imei
     * @return
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setActive(true);
        d.setImei(imei);
        d.setName("JUnit");
        d.setDescription("JUnit Device");
        return d;
    }
    /**
     * @param email user email.
     * @return user.
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setId(id++);
        u.setEmail(email);
        u.setFirstName("First");
        u.setLastName("Last");
        return u;
    }
    /**
     * @param device
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    protected List<ShortTrackerEvent> getTrackerEvents(final String device,
            final Date startDate, final Date endDate) {
        final List<ShortTrackerEvent> list = new LinkedList<>();
        for (final ShortTrackerEvent e : events) {
            final Date time = e.getTime();
            if (e.getDeviceImei().equals(device)
                    && (startDate == null || !time.before(startDate))
                    && (endDate == null || !time.after(endDate))
                    ) {
                list.add(e);
            }
        }
        return list;
    }
    /**
     * @param user user.
     * @param started started flag.
     */
    @Override
    protected void setSimulatorStarted(final User user, final boolean started) {
        final SimulatorDto dto = simulators.get(user.getId());
        if (dto != null) {
            dto.setStarted(started);
        }
    }
    /**
     * @param msg
     */
    @Override
    protected void deleteSystemMessage(final SystemMessage msg) {
        final Iterator<SystemMessage> iter = messages.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(msg.getId())) {
                iter.remove();
                break;
            }
        }
    }
    /**
     * @return list of tracker system messages
     */
    @Override
    protected List<SystemMessage> findTrackerEventMessages() {
        final List<SystemMessage> list = new LinkedList<>();
        for (final SystemMessage m : messages) {
            if (m.getType() == SystemMessageType.Tracker) {
                list.add(m);
            }
        }

        Collections.reverse(list);
        return list;
    }
    /**
     * @param payload
     * @param type
     * @param time
     */
    @Override
    protected void sendSystemMessage(final String payload, final SystemMessageType type, final Date time) {
        final SystemMessage msg = new SystemMessage();
        msg.setId(id++);
        msg.setMessageInfo(payload);
        msg.setRetryOn(time);
        msg.setTime(time);
        msg.setType(type);
        messages.add(msg);
    }
    /**
     * @param user
     * @return
     */
    @Override
    protected SimulatorDto findSimulator(final User user) {
        return simulators.get(user.getId());
    }
}
