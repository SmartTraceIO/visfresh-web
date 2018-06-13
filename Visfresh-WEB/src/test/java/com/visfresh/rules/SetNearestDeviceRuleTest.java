/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SetNearestDeviceRuleTest extends SetNearestDeviceRule {
    private final Map<Long, String> nearestGateways = new HashMap<>();
    private RuleContext context;
    private final List<Device> devices = new LinkedList<>();

    /**
     * Default constructor.
     */
    public SetNearestDeviceRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        final Device device = new Device();
        device.setImei("111111111111111");
        device.setModel(DeviceModel.BT04);
        device.setCompany(1l);

        final Shipment s = new Shipment();
        s.setId(1l);
        s.setCompany(device.getCompanyId());
        s.setDevice(device);

        final TrackerEvent event = new TrackerEvent();
        event.setId(1l);
        event.setDevice(device);
        event.setShipment(s);

        context = new RuleContext(event, null);
    }

    @Test
    public void testNotAcceptIfEventHasNotGateway() {
        context.setGatewayDevice(null);
        assertFalse(accept(context));
    }
    @Test
    public void testNotAcceptNoShipment() {
        context.setGatewayDevice("2222222222222222");
        context.getEvent().setShipment(null);
        assertFalse(accept(context));
    }
    @Test
    public void testNotAcceptIfGivenGatewayAlreadySet() {
        context.setGatewayDevice("2222222222222222");
        context.getEvent().getShipment().setNearestTracker(context.getGatewayDevice());
        assertFalse(accept(context));
    }
    @Test
    public void testNotAcceptIfDeviceIsNotBeacon() {
        context.setGatewayDevice("2222222222222222");
        context.getEvent().getDevice().setModel(DeviceModel.SmartTrace);
        assertFalse(accept(context));
    }
    @Test
    public void testAccept() {
        context.setGatewayDevice("2222222222222222");
        assertTrue(accept(context));
    }
    @Test
    public void testHandleAndGatewayHasDifferentCompany() {
        final String gatewayImei = "2222222222222222";
        final Device gateway = new Device();
        gateway.setImei(gatewayImei);
        gateway.setCompany(8888888888l);
        devices.add(gateway);

        context.setGatewayDevice(gatewayImei);

        assertTrue(accept(context));
        assertFalse(handle(context));
        assertNull(gatewayImei, nearestGateways.get(context.getEvent().getDevice().getImei()));

        //test not double processing
        assertFalse(accept(context));
    }
    @Test
    public void testHandle() {
        final String gatewayImei = "2222222222222222";
        final Device gateway = new Device();
        gateway.setImei(gatewayImei);
        gateway.setCompany(context.getEvent().getDevice().getCompanyId());
        devices.add(gateway);

        context.setGatewayDevice(gatewayImei);

        assertTrue(accept(context));
        assertFalse(handle(context));
        assertEquals(gatewayImei, nearestGateways.get(context.getEvent().getShipment().getId()));

        //test not double processing
        assertFalse(accept(context));
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.SetNearestDeviceRule#saveNearestDeviceFor(com.visfresh.entities.Device, com.visfresh.entities.Device)
     */
    @Override
    protected void saveNearestTrackerFor(final Shipment s, final Device gateway) {
        nearestGateways.put(s.getId(), gateway.getImei());
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.SetNearestDeviceRule#findDevice(java.lang.String)
     */
    @Override
    protected Device findDevice(final String device) {
        for (final Device d : devices) {
            if (d.getImei().equals(device)) {
                return d;
            }
        }
        return null;
    }
}
