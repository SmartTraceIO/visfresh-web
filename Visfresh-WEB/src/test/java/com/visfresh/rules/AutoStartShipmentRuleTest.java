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

import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
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
        final Device d = device;
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setTemperature(20.4);
        e.setType(TrackerEventType.AUT);
        e.setDevice(d);
        e.setTime(date);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    private TrackerEvent createEvent(final Shipment s, final double lat, final double lon, final Date date) {
        final TrackerEvent e = createEvent(lat, lon, date);
        e.setShipment(s);
        e.setDevice(s.getDevice());
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
    @Test
    public void testReuseInProgressShipment() {
        //create in progress shipment
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress, device);

        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new DeviceState());

        rule.handle(c);

        //check shipment created.
        assertNotNull(e.getShipment());
        assertEquals(s.getId(), e.getShipment().getId());

        //check not new shipments created
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);
        assertEquals(1, shipmentDao.findAll(null, null, null).size());
    }
    @Test
    public void testReuseExpiredPreviousShipment() {
        //create in progress shipment
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress, device);
        final LocationProfile l1 = createLocationProfile(17.14, 18.16);
        final LocationProfile l2 = createLocationProfile(18.14, 19.16);
        s.setShippedFrom(l1);
        s.setShippedTo(l2);
        context.getBean(ShipmentDao.class).save(s);

        createEvent(s, 17.14, 18.16, new Date(System.currentTimeMillis() - 10000000l));
        createEvent(s, 18.139, 19.159, new Date(System.currentTimeMillis() - 10000000l + 60000));

        final TrackerEvent e = createEvent(18.139, 19.159, new Date());
        final RuleContext c = new RuleContext(e, new DeviceState());

        rule.handle(c);

        //check shipment created.
        assertNotNull(e.getShipment());
        assertEquals(s.getId(), e.getShipment().getId());

        //check not new shipments created
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);
        assertEquals(1, shipmentDao.findAll(null, null, null).size());
    }
    //@Test test has temporary disabled according of comment the logics
    public void testNotReuseNotExpiredPreviousShipment() {
        //create in progress shipment
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress, device);
        final LocationProfile l1 = createLocationProfile(17.14, 18.16);
        final LocationProfile l2 = createLocationProfile(18.14, 19.16);
        s.setShippedFrom(l1);
        s.setShippedTo(l2);
        context.getBean(ShipmentDao.class).save(s);

        createEvent(s, 17.14, 18.16, new Date(System.currentTimeMillis() - 10000000l));
        createEvent(s, 17.1401, 18.1601, new Date(System.currentTimeMillis()));

        final TrackerEvent e = createEvent(18.14, 19.16, new Date());
        final RuleContext c = new RuleContext(e, new DeviceState());

        rule.handle(c);

        //check shipment created.
        assertNotNull(e.getShipment());

        //check not new shipments created
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);
        assertEquals(2, shipmentDao.findAll(null, null, null).size());
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     * @return location profile.
     */
    private LocationProfile createLocationProfile(final double lat, final double lon) {
        final LocationProfile l = new LocationProfile();
        l.setAddress("Any address");
        l.setCompany(company);
        l.setName("Loc (" + lat + ", " + lon + ")");
        l.getLocation().setLatitude(lat);
        l.getLocation().setLongitude(lon);
        return context.getBean(LocationProfileDao.class).save(l);
    }
}
