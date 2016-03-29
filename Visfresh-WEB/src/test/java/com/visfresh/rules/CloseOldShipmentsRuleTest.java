/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CloseOldShipmentsRuleTest extends CloseOldShipmentsRule {
    private Map<String, List<Shipment>> activeShipments = new HashMap<String, List<Shipment>>();
    private Shipment shipment;
    private TrackerEvent event;

    /**
     * Default constructor.
     */
    public CloseOldShipmentsRuleTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.CloseOldShipmentsRule#findActiveShipments(java.lang.String)
     */
    @Override
    protected List<Shipment> findActiveShipments(final String imei) {
        final List<Shipment> list = activeShipments.get(imei);
        return list == null ? new LinkedList<Shipment>() : list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.CloseOldShipmentsRule#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment s) {
    }

    @Before
    public void setUp() {
        final Device device = new Device();
        device.setImei("019238790879872");
        device.setName("JUnit device");

        this.shipment = new Shipment();
        shipment.setId(1l);
        shipment.setDevice(device);

        this.event = new TrackerEvent();
        event.setDevice(device);
        event.setShipment(shipment);
        event.setType(TrackerEventType.AUT);
    }

    @Test
    public void testAccept() {
        final ShipmentSession state = new ShipmentSession();
        final RuleContext context = new RuleContext(event, state);

        //check null shipment
        event.setShipment(null);
        assertFalse(accept(context));

        //check success accept
        event.setShipment(shipment);
        assertTrue(accept(context));

        //check denied with state set to processed
        state.setOldShipmentsClean(true);
        assertFalse(accept(context));

        //check denied already processed
        state.setOldShipmentsClean(false);
        context.setProcessed(this);
        assertFalse(accept(context));
    }
    @Test
    public void testHandle() {
        //create active shipment.
        long lastId = shipment.getId();
        final Shipment s = new Shipment();
        s.setId((++lastId));
        s.setDevice(shipment.getDevice());
        s.setStatus(ShipmentStatus.Default);

        final List<Shipment> active = new LinkedList<Shipment>();
        active.add(s);

        activeShipments.put(shipment.getDevice().getImei(), active);

        //handle event
        final ShipmentSession state = new ShipmentSession();
        final RuleContext context = new RuleContext(event, state);
        assertFalse(handle(context));

        //check state
        assertTrue(state.isOldShipmentsClean());
        assertTrue(context.isProcessed(this));

        //check status closed
        assertEquals(ShipmentStatus.Ended, s.getStatus());
    }
}
