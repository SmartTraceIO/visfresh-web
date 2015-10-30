/**
 *
 */
package com.visfresh.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AssignShipmentRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private TrackerEvent event;

    /**
     *
     */
    public AssignShipmentRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = engine.getRule(AssignShipmentRule.NAME);
        final Device device = createDevice("90324870987");

        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(13.14);
        e.setLongitude(15.16);
        e.setTemperature(20.4);
        e.setTime(new Date());
        e.setType("INIT");
        e.setDevice(device);
        this.event = e;
    }

    @Test
    public void testAccept() {
        //not accepts because shipment not found
        final TrackerEventRequest req = new TrackerEventRequest(event);
        assertFalse(rule.accept(req));

        //create shipment but in final state
        final Shipment s = createDefaultShipment("Test Shipment", ShipmentStatus.Complete, event.getDevice());
        assertFalse(rule.accept(req));

        s.setStatus(ShipmentStatus.InProgress);
        context.getBean(ShipmentDao.class).save(s);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testHandle() {
        final Shipment s = createDefaultShipment("Test Shipment", ShipmentStatus.InProgress, event.getDevice());
        final TrackerEventRequest req = new TrackerEventRequest(event);

        //try accept because should cache shipment in accept method.
        rule.accept(req);
        rule.handle(req);
        assertEquals(s.getId(), event.getShipment().getId());
        //next accept should be false
        assertFalse(rule.accept(req));
    }
}
