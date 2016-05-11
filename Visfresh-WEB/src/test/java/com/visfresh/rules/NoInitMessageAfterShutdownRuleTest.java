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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.ShipmentSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoInitMessageAfterShutdownRuleTest extends
        NoInitMessageAfterShutdownRule {
    private Shipment autoStartedShipment;
    private List<String> sentMessages = new LinkedList<>();
    private List<Shipment> savedShipments = new LinkedList<>();
    private List<TrackerEvent> savedEvents = new LinkedList<>();
    private Map<String, Date> shutdowns = new HashMap<>();
    private Shipment lastShipment;

    private Device device;
    private ShipmentSessionManager manager;
    private long id = 1;

    /**
     * Default constructor.
     */
    public NoInitMessageAfterShutdownRuleTest() {
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
    public void testNotAcceptAlreadyProcessed() {
        final TrackerEvent e = createCorrectEventWithShipment();

        //test accept
        final RuleContext context = createRuleContext(e);
        assertTrue(accept(context));

        context.setProcessed(this);
        assertFalse(accept(context));
    }
    @Test
    public void testAcceptWithPreviousShipment() {
        final TrackerEvent e = createCorrectEventWithShipment();
        final RuleContext context = createRuleContext(e);

        assertTrue(accept(context));

        final Shipment s = e.getShipment();
        e.setShipment(null);
        assertFalse(accept(context));

        // create last shipment
        lastShipment = new Shipment();
        lastShipment.setId(77l);
        lastShipment.setDevice(device);
        lastShipment.setStatus(ShipmentStatus.Arrived);
        lastShipment.setDeviceShutdownTime(s.getDeviceShutdownTime());
        assertTrue(accept(context));
    }

    @Test
    public void testAcceptOnlyWithShutdownTime() {
        final TrackerEvent e = createCorrectEventWithShipment();
        final Shipment s = e.getShipment();

        //test not accept without shutdown time
        s.setDeviceShutdownTime(null);
        assertFalse(accept(createRuleContext(e)));
    }
    @Test
    public void testAcceptWithCurrentShipment() {
        final TrackerEvent e = createCorrectEventWithShipment();
        final Shipment s = e.getShipment();

        //test device shutdown time out not exceed
        s.setDeviceShutdownTime(new Date(e.getTime().getTime() - CHECK_SHUTDOWN_TIMEOUT + 2000));
        assertFalse(accept(createRuleContext(e)));
    }
    @Test
    public void testAcceptOnlyFinalShipmentStatus() {
        final TrackerEvent e = createCorrectEventWithShipment();
        final Shipment s = e.getShipment();

        //test not accept not final
        s.setStatus(ShipmentStatus.Default);
        assertFalse(accept(createRuleContext(e)));

        s.setStatus(ShipmentStatus.Arrived);
        assertTrue(accept(createRuleContext(e)));

        s.setStatus(ShipmentStatus.Ended);
        assertTrue(accept(createRuleContext(e)));
    }
    @Test
    public void testAcceptOnlyNotInitMessage() {
        final TrackerEvent e = createCorrectEventWithShipment();

        //test not accept init message
        e.setType(TrackerEventType.INIT);
        assertFalse(accept(createRuleContext(e)));

        e.setType(TrackerEventType.AUT);
        assertTrue(accept(createRuleContext(e)));
    }
    @Test
    public void testAutostartAfterShutdownRepeat() {
        final TrackerEvent e = createCorrectEventWithShipment();

        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setAutostart(true);
        s.setId(id++);
        autoStartedShipment = s;

        final RuleContext context = createRuleContext(e);
        setShutDownRepeatTime(context.getDeviceState(), e.getShipment().getDeviceShutdownTime());

        assertTrue(handle(context));
        assertEquals(1, savedEvents.size());
        assertEquals(e.getId(), savedEvents.get(0).getId());
        assertEquals(s.getId(), e.getShipment().getId());
        assertEquals(1, this.sentMessages.size());
        assertEquals(1, savedShipments.size());
        assertNull(getShutDownRepeatTime(context.getDeviceState()));
    }
    @Test
    public void testAutostartAfterShutdownExpired() {
        final TrackerEvent e = createCorrectEventWithShipment();

        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setAutostart(true);
        s.setId(id++);
        autoStartedShipment = s;

        final RuleContext context = createRuleContext(e);
        e.getShipment().setDeviceShutdownTime(new Date(e.getTime().getTime() - 2 * CHECK_SHUTDOWN_TIMEOUT - 100));

        assertTrue(handle(context));
        assertEquals(1, savedEvents.size());
        assertEquals(e.getId(), savedEvents.get(0).getId());
        assertEquals(s.getId(), e.getShipment().getId());
        assertEquals(1, this.sentMessages.size());
        assertEquals(1, savedShipments.size());
        assertNull(getShutDownRepeatTime(context.getDeviceState()));
    }
    @Test
    public void testSendShutdownRepeat() {
        final TrackerEvent e = createCorrectEventWithShipment();

        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setAutostart(true);
        s.setId(id++);
        autoStartedShipment = s;

        final RuleContext context = createRuleContext(e);

        assertFalse(handle(context));
        assertEquals(0, savedEvents.size());
        assertEquals(0, this.sentMessages.size());
        assertEquals(0, savedShipments.size());
        assertNotNull(getShutDownRepeatTime(context.getDeviceState()));
        assertNotNull(shutdowns.get(e.getDevice().getImei()));
    }

    /**
     * @param e
     * @return
     */
    protected RuleContext createRuleContext(final TrackerEvent e) {
        final RuleContext ctxt = new RuleContext(e, manager);
        ctxt.setDeviceState(new DeviceState());
        return ctxt;
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
     * @see com.visfresh.rules.AbstractNoInitMessageRule#autoStartShipmentByService(com.visfresh.entities.Device, double, double, java.util.Date)
     */
    @Override
    protected Shipment autoStartShipmentByService(final Device device,
            final double latitude, final double longitude, final Date time) {
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
     * @see com.visfresh.rules.NoInitMessageAfterShutdownRule#findLastShipment(java.lang.String)
     */
    @Override
    protected Shipment findLastShipment(final String imei) {
        return lastShipment;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageAfterShutdownRule#shutDownDevice(com.visfresh.entities.Device, java.util.Date)
     */
    @Override
    protected void shutDownDevice(final Device device, final Date date) {
        shutdowns.put(device.getImei(), date);
    }
}
