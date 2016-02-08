/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Device device;
    /**
     * Default constructor.
     */
    public AutoStartShipmentRuleTest() {
        super();
    }
    @Before
    public void setUp() {
        this.rule = engine.getRule(AutoStartShipmentRule.NAME);
        this.device = createDevice("90324870987");
    }

    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param date date.
     * @return event.
     */
    private TrackerEvent createEvent(final double lat, final double lon, final Date date) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setTemperature(20.4);
        e.setType(TrackerEventType.AUT);
        e.setDevice(device);
        e.setTime(date);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    @Test
    public void testAccept() {
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        e.setShipment(new Shipment());

        //ignores with shipment
        assertFalse(rule.accept(new RuleContext(e, new DeviceState())));

        //accept with shipment if INIT message
        e.setType(TrackerEventType.INIT);
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));

        e.setShipment(null);
        e.setType(TrackerEventType.AUT);
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        //check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        assertNotNull(shipmentDao.findOne(shipmentId));

        //check not duplicate handle
        assertFalse(rule.accept(c));

        //check old shipment closed
        rule.handle(c);

        //check old shipment closed
        final Shipment old = shipmentDao.findOne(shipmentId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());

        //check new shipment created.
        assertTrue(!shipmentId.equals(e.getShipment().getId()));
        assertTrue(old.getTripCount() < e.getShipment().getTripCount());
    }
}
