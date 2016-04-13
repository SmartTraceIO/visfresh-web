/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class NoInitMessageRuleTest extends NoInitMessageRule {
    private Shipment autoStartedShipment;
    private List<String> sentMessages = new LinkedList<>();
    private List<Shipment> savedShipments = new LinkedList<>();
    private Shipment lastShipment;
    private Device device;
    private ShipmentSessionManager manager;

    /**
     * Default constructor.
     */
    public NoInitMessageRuleTest() {
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
        final TrackerEvent e = createEvent();

        //test accept
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept with shipment
        e.setShipment(new Shipment());
        assertFalse(accept(new RuleContext(e, manager)));
        e.setShipment(null);
        assertTrue(accept(new RuleContext(e, manager)));

        //test not accept INIT message
        e.setType(TrackerEventType.INIT);
        assertFalse(accept(new RuleContext(e, manager)));
    }

    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent();

        //create last shipment
        lastShipment = new Shipment();
        lastShipment.setId(77l);
        lastShipment.setDevice(device);
        lastShipment.setStatus(ShipmentStatus.Arrived);
        lastShipment.setDeviceShutdownTime(new Date());

        //set up autostart shipment
        autoStartedShipment = new Shipment();
        autoStartedShipment.setId(7l);

        assertTrue(handle(new RuleContext(e, manager)));
        assertEquals(1, savedShipments.size());
        assertEquals(1, sentMessages.size());
        assertEquals(autoStartedShipment.getId(), e.getShipment().getId());
    }
    @Test
    public void testHandleNotLastShipment() {
        final TrackerEvent e = createEvent();

        assertFalse(handle(new RuleContext(e, manager)));
        assertNull(e.getShipment());
        assertEquals(0, savedShipments.size());
        assertEquals(0, sentMessages.size());
    }

    @Test
    public void testLastShipmentNotShutdown() {
        final TrackerEvent e = createEvent();
        lastShipment = new Shipment();
        lastShipment.setId(77l);
        lastShipment.setDevice(device);
        lastShipment.setStatus(ShipmentStatus.Arrived);
        lastShipment.setDeviceShutdownTime(null);

        assertFalse(handle(new RuleContext(e, manager)));
        assertNull(e.getShipment());
        assertEquals(0, savedShipments.size());
        assertEquals(0, sentMessages.size());
    }

    /**
     * @return
     */
    protected TrackerEvent createEvent() {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(device);
        e.setId(7l);
        e.setTime(new Date());
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
     * @see com.visfresh.rules.NoInitMessageRule#findLastShipment(java.lang.String)
     */
    @Override
    protected Shipment findLastShipment(final String imei) {
        return lastShipment;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageRule#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment s) {
        savedShipments.add(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.NoInitMessageRule#sendMessageToSupport(java.lang.String, java.lang.String)
     */
    @Override
    protected void sendMessageToSupport(final String subject, final String msg) {
        sentMessages.add(msg);
    }
}
