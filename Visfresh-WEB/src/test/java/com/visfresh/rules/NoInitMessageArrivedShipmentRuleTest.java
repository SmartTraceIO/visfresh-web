/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.ShipmentSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoInitMessageArrivedShipmentRuleTest extends
        NoInitMessageArrivedShipmentRule {
    private Shipment autoStartedShipment;
    private List<String> sentMessages = new LinkedList<>();
    private List<Shipment> savedShipments = new LinkedList<>();
    private List<TrackerEvent> events = new LinkedList<>();
    private List<TrackerEvent> savedEvents = new LinkedList<>();

    private Device device;
    private ShipmentSessionManager manager;
    private long id = 1;

    /**
     * Default constructor.
     */
    public NoInitMessageArrivedShipmentRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        this.device = new Device();
        device.setActive(true);
        device.setName("JUnit");
        device.setImei("23490870239487203");


        manager = new SessionHolder();
    }

    @Test
    public void testAccept() {
        final TrackerEvent e = createCorrectEventWithShipment();
        final Shipment s = e.getShipment();
        final Date shutdownTime = s.getDeviceShutdownTime();

        //test accept
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept without shipment
        e.setShipment(null);
        assertFalse(accept(new RuleContext(e, manager)));

        e.setShipment(s);
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept not arrived
        s.setStatus(ShipmentStatus.Default);
        assertFalse(accept(new RuleContext(e, manager)));

        s.setStatus(ShipmentStatus.Arrived);
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept init message
        e.setType(TrackerEventType.INIT);
        assertFalse(accept(new RuleContext(e, manager)));

        e.setType(TrackerEventType.AUT);
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept without shutdown time
        s.setDeviceShutdownTime(null);
        assertFalse(accept(new RuleContext(e, manager)));

        s.setDeviceShutdownTime(shutdownTime);
        assertTrue(accept(new RuleContext(e, manager)));

        //test device shutdown time out not exceed
        s.setDeviceShutdownTime(new Date(e.getTime().getTime() - CHECK_SHUTDOWN_TIMEOUT + 2000));
        assertFalse(accept(new RuleContext(e, manager)));
    }
    @Test
    public void testAutostartNewShipment() {
        final TrackerEvent e = createCorrectEventWithShipment();

        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setAutostart(true);
        s.setId(id++);
        autoStartedShipment = s;

        assertTrue(handle(new RuleContext(e, this.manager)));
        assertEquals(1, savedEvents.size());
        assertEquals(e.getId(), savedEvents.get(0).getId());
        assertEquals(s.getId(), e.getShipment().getId());
    }
    @Test
    public void testSetNullShipmentToEventsAfterShutdownTimeOut() {
        final TrackerEvent e = createCorrectEventWithShipment();

        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setAutostart(true);
        s.setId(id++);
        autoStartedShipment = s;

        //add events
        //add event before shutdown timeout reached
        final TrackerEvent e1 = createCorrectEvent(e.getShipment());
        e1.setTime(new Date(e1.getShipment().getDeviceShutdownTime().getTime() + CHECK_SHUTDOWN_TIMEOUT / 2));
        events.add(e1);

        //add event after shutdown timeout reached
        final TrackerEvent e2 = createCorrectEvent(e.getShipment());
        e2.setTime(new Date(e2.getShipment().getDeviceShutdownTime().getTime() + CHECK_SHUTDOWN_TIMEOUT + 1000l));
        events.add(e2);

        //add given event
        events.add(e);

        assertTrue(handle(new RuleContext(e, this.manager)));

        //check not touch given event
        assertNotNull(e.getShipment());

        //check event before shutdown timeout reached
        assertNotNull(e1.getShipment());

        //check event after shutdown timeout reached
        assertNull(e2.getShipment());
    }

    /**
     * @return
     */
    private TrackerEvent createCorrectEventWithShipment() {
        final Shipment s = new Shipment();
        s.setId(id++);
        s.setStatus(ShipmentStatus.Arrived);
        s.setDevice(device);
        s.setDeviceShutdownTime(new Date(System.currentTimeMillis() - CHECK_SHUTDOWN_TIMEOUT - 1000));
        return createCorrectEvent(s);
    }
    /**
     * @param s
     * @return
     */
    private TrackerEvent createCorrectEvent(final Shipment s) {
        final TrackerEvent e = createEvent(new Date());
        e.setShipment(s);
        return e;
    }
    /**
     * @return
     */
    protected TrackerEvent createEvent(final Date time) {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(device);
        e.setId(id++);
        e.setTime(time);
        e.setType(TrackerEventType.AUT);
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageRule#autoStartNewShipment(com.visfresh.entities.Device, double, double, java.util.Date)
     */
    @Override
    protected Shipment autoStartNewShipment(final Device device, final double latitude,
            final double longitude, final Date time) {
        return autoStartedShipment;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageRule#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment s) {
        savedShipments.add(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageArrivedShipmentRule#saveEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void saveEvent(final TrackerEvent evt) {
        savedEvents.add(evt);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageRule#sendMessageToSupport(java.lang.String, java.lang.String)
     */
    @Override
    protected void sendMessageToSupport(final String subject, final String msg) {
        sentMessages.add(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageArrivedShipmentRule#getEventsAfterDate(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    protected List<TrackerEvent> getEventsAfterDate(final Shipment s, final Date date) {
        final List<TrackerEvent> list = new LinkedList<>();
        for (final TrackerEvent e : events) {
            if (e.getShipment() != null && e.getShipment().getId().equals(s.getId()) && e.getTime().after(date)) {
                list.add(e);
            }
        }
        return list;
    }
}
