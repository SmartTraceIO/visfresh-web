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

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
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
     * @param lat
     *            latitude.
     * @param lon
     *            longitude.
     * @param date
     *            date.
     * @return event.
     */
    private TrackerEvent createEvent(final double lat, final double lon,
            final Date date) {
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

    private TrackerEvent createEvent(final Shipment s, final double lat,
            final double lon, final Date date) {
        final TrackerEvent e = createEvent(lat, lon, date);
        e.setShipment(s);
        e.setDevice(s.getDevice());
        return context.getBean(TrackerEventDao.class).save(e);
    }

    @Test
    public void testAccept() {
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        e.setShipment(new Shipment());

        // ignores with shipment
        assertFalse(rule.accept(new RuleContext(e, new DeviceState())));

        // accept with shipment if INIT message
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

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment shipment = shipmentDao.findOne(shipmentId);
        assertNotNull(shipment);
        assertEquals(e.getTime().getTime(), shipment.getShipmentDate().getTime(), 1000);

        // check not duplicate handle
        assertFalse(rule.accept(c));

        // check old shipment closed
        rule.handle(c);

        // check old shipment closed
        final Shipment old = shipmentDao.findOne(shipmentId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());

        // check new shipment created.
        shipment = shipmentDao.findOne(e.getShipment().getId());
        assertTrue(!shipmentId.equals(shipment.getId()));
        assertTrue(old.getTripCount() < shipment.getTripCount());
        assertEquals(e.getTime().getTime(), shipment.getShipmentDate().getTime(), 1000);
    }

    @Test
    public void testSelectAutoStartShipment() {
        // create locations
        final LocationProfile l1 = createLocationProfile(2, 2, 1000);
        final LocationProfile l2 = createLocationProfile(1, 1, 1000);
        final LocationProfile lok = createLocationProfile(17.14, 18.16, 1000);

        // create shipment templates
        final ShipmentTemplate t1 = createTemplate("t1");
        final ShipmentTemplate t2 = createTemplate("t2");
        final ShipmentTemplate tok = createTemplate("tok");

        // create auto start shipments
        createAutoStartShipment(t1, 1000, l1, l2);
        createAutoStartShipment(t2, 1, l1, lok);
        createAutoStartShipment(tok, 2, l1, lok);

        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment shipment = shipmentDao.findOne(shipmentId);
        assertNotNull(shipment);
        assertEquals(e.getTime().getTime(), shipment.getShipmentDate().getTime(), 1000);
        // check correct start location selected.
        assertEquals(lok.getId(), shipment.getShippedFrom().getId());
        // check created from correct template
        assertEquals(tok.getShipmentDescription(), shipment.getShipmentDescription());

        // check not duplicate handle
        assertFalse(rule.accept(c));

        // check old shipment closed
        rule.handle(c);

        // check old shipment closed
        final Shipment old = shipmentDao.findOne(shipmentId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());

        // check new shipment created.
        shipment = shipmentDao.findOne(e.getShipment().getId());
        assertTrue(!shipmentId.equals(shipment.getId()));
        assertTrue(old.getTripCount() < shipment.getTripCount());
        assertEquals(e.getTime().getTime(), shipment.getShipmentDate().getTime(), 1000);
    }
    @Test
    public void testSelectAutoStartShipmentWithBadLocations() {
        // create locations
        final LocationProfile l1 = createLocationProfile(2, 2, 1000);
        final LocationProfile l2 = createLocationProfile(1, 1, 1000);

        // create shipment templates
        final ShipmentTemplate t1 = createTemplate("t1");
        final ShipmentTemplate t2 = createTemplate("t2");

        // create auto start shipments
        createAutoStartShipment(t1, 1, l1);
        createAutoStartShipment(t2, 2, l2);

        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        final Shipment s = shipmentDao.findOne(shipmentId);
        assertNotNull(s);
        assertEquals(e.getTime().getTime(), s.getShipmentDate().getTime(), 1000);
        // check correct start location selected.
        assertNull(s.getShippedFrom());
        // check created from correct template
        assertEquals(t2.getShipmentDescription(), s.getShipmentDescription());
    }
    @Test
    public void testAutostartAssignedToDevice() {
        // create locations
        final LocationProfile lok = createLocationProfile(17.14, 18.16, 1000);

        // create shipment templates
        final ShipmentTemplate tok = createTemplate("tok");

        // create auto start shipments
        createAutoStartShipment(tok, 2, lok);

        TrackerEvent e = createEvent(17.14, 18.16, new Date());
        RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment s = shipmentDao.findOne(e.getShipment().getId());
        assertNotNull(s.getShippedFrom());

        //assign autostart to device, but not set any locations to this
        //template
        final AutoStartShipment assigned = createAutoStartShipment(
                createTemplate("Assigned"), 2);
        e.getDevice().setAutostartTemplateId(assigned.getId());
        context.getBean(DeviceDao.class).save(e.getDevice());

        e = createEvent(17.14, 18.16, new Date());
        c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        s = shipmentDao.findOne(e.getShipment().getId());
        assertNull(s.getShippedFrom());
    }
    // @Test test has temporary disabled according of comment the logics
    public void testNotReuseNotExpiredPreviousShipment() {
        // create in progress shipment
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress,
                device);
        final LocationProfile l1 = createLocationProfile(17.14, 18.16, 0);
        final LocationProfile l2 = createLocationProfile(18.14, 19.16, 0);
        s.setShippedFrom(l1);
        s.setShippedTo(l2);
        context.getBean(ShipmentDao.class).save(s);

        createEvent(s, 17.14, 18.16, new Date(
                System.currentTimeMillis() - 10000000l));
        createEvent(s, 17.1401, 18.1601, new Date(System.currentTimeMillis()));

        final TrackerEvent e = createEvent(18.14, 19.16, new Date());
        final RuleContext c = new RuleContext(e, new DeviceState());

        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());

        // check not new shipments created
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);
        assertEquals(2, shipmentDao.findAll(null, null, null).size());
    }

    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param radius location radius.
     * @return location profile.
     */
    private LocationProfile createLocationProfile(final double lat,
            final double lon, final int radius) {
        final LocationProfile l = new LocationProfile();
        l.setAddress("Any address");
        l.setCompany(company);
        l.setName("Loc (" + lat + ", " + lon + ")");
        l.setRadius(radius);
        l.getLocation().setLatitude(lat);
        l.getLocation().setLongitude(lon);
        return context.getBean(LocationProfileDao.class).save(l);
    }
    /**
     * @param tpl template.
     * @param priority priority.
     * @param startLocations start locations.
     * @return auto start shipment.
     */
    private AutoStartShipment createAutoStartShipment(final ShipmentTemplate tpl,
            final int priority, final LocationProfile... startLocations) {
        final AutoStartShipment aut = new AutoStartShipment();
        aut.setCompany(company);
        aut.setPriority(priority);
        aut.setTemplate(tpl);
        for (final LocationProfile l : startLocations) {
            aut.getShippedFrom().add(l);
        }
        return context.getBean(AutoStartShipmentDao.class).save(aut);
    }
    /**
     * @param shipmentDescription shipment description.
     * @return shipment template.
     */
    private ShipmentTemplate createTemplate(final String shipmentDescription) {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setCompany(company);
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setName(shipmentDescription);
        s.setShipmentDescription(shipmentDescription);
        s.setShutdownDeviceAfterMinutes(70);
        s.setAddDateShipped(true);
        s.setDetectLocationForShippedFrom(true);
        return context.getBean(ShipmentTemplateDao.class).save(s);
    }
}
