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
import com.visfresh.dao.ShipmentSessionDao;
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
import com.visfresh.rules.state.ShipmentSession;

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
    private TrackerEvent createEvent(final Double lat, final Double lon,
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

    @Test
    public void testAccept() {
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        e.setShipment(new Shipment());

        // ignores with shipment
        assertFalse(rule.accept(new RuleContext(e, new SessionHolder())));

        // accept with shipment if INIT message
        e.setType(TrackerEventType.INIT);
        assertTrue(rule.accept(new RuleContext(e, new SessionHolder())));
    }

    @Test
    public void testAcceptNullLocation() {
        final TrackerEvent e = createEvent(null, null, new Date());
        e.setShipment(new Shipment());

        // ignores with shipment
        assertFalse(rule.accept(new RuleContext(e, new SessionHolder())));

        // accept with shipment if INIT message
        e.setType(TrackerEventType.INIT);
        assertTrue(rule.accept(new RuleContext(e, new SessionHolder())));
    }

    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(17.14, 18.16, new Date());
        e.setTime(new Date(System.currentTimeMillis() - 1000000l));

        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment shipment = shipmentDao.findOne(shipmentId);
        assertNotNull(shipment);
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);

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
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);
    }
    @Test
    public void testHandleNullLocation() {
        final TrackerEvent e = createEvent(null, null, new Date());
        e.setTime(new Date(System.currentTimeMillis() - 1000000l));

        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment shipment = shipmentDao.findOne(shipmentId);
        assertNotNull(shipment);
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);

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
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);
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

        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        Shipment shipment = shipmentDao.findOne(shipmentId);
        assertTrue(shipment.isAutostart());
        assertNotNull(shipment);
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);
        // check correct start location selected.
        assertEquals(lok.getId(), shipment.getShippedFrom().getId());
        // check created from correct template
        assertTrue(shipment.getShipmentDescription().startsWith(tok.getShipmentDescription()));
        // check not autodetect data saved, because start location is aready assigned
        // according of matches reading's loation
        assertEquals(0, getSession(shipment).getShipmentKeys().size());

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
        assertTrue(e.getTime().getTime() - shipment.getShipmentDate().getTime() < 60000l);
    }
    @Test
    public void testSelectAutoStartShipmentLocFromDuplicates() {
        // create locations
        final LocationProfile loc = createLocationProfile(2, 2, 1000);

        // create shipment templates
        final ShipmentTemplate tpl = createTemplate("t1");
        tpl.setShippedFrom(loc);
        tpl.setShippedTo(loc);
        context.getBean(ShipmentTemplateDao.class).save(tpl);

        // create auto start shipments
        final AutoStartShipment a1 = createAutoStartShipment(tpl, 1000, loc);
        a1.getShippedTo().add(loc);
        context.getBean(AutoStartShipmentDao.class).save(a1);

        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new SessionHolder());
        //handle and check not exception
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
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

        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        // check shipment created.
        assertNotNull(e.getShipment());
        final Long shipmentId = e.getShipment().getId();
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);

        final Shipment s = shipmentDao.findOne(shipmentId);
        assertNotNull(s);
        assertTrue(e.getTime().getTime() - s.getShipmentDate().getTime() < 60000l);
        // check correct start location selected.
        assertNull(s.getShippedFrom());
        // check created from correct template
        assertTrue(s.getShipmentDescription().startsWith(t2.getShipmentDescription()));
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
        RuleContext c = new RuleContext(e, new SessionHolder());
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
        c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        s = shipmentDao.findOne(e.getShipment().getId());
        assertNull(s.getShippedFrom());
    }

    @Test
    public void testSelectEndLocationIfOne() {
        // create locations
        final LocationProfile l1 = createLocationProfile(17.14, 18.16, 1000);
        final LocationProfile l2 = createLocationProfile(27.14, 28.16, 1000);

        // create shipment templates
        final ShipmentTemplate tok = createTemplate("tok");

        // create auto start shipments
        final AutoStartShipmentDao dao = context.getBean(AutoStartShipmentDao.class);

        final AutoStartShipment aut = createAutoStartShipment(tok, 2, l1);
        aut.getShippedTo().add(l1);
        aut.getShippedTo().add(l2);
        dao.save(aut);

        final TrackerEvent e = createEvent(17.14, 18.16, new Date());
        rule.handle(new RuleContext(e, new SessionHolder()));
        assertEquals(1, getSession(e.getShipment()).getShipmentKeys().size());

        // check shipment created.
        assertNull(e.getShipment().getShippedTo());

        aut.getShippedTo().remove(l1);
        dao.save(aut);

        rule.handle(new RuleContext(e, new SessionHolder()));
        assertEquals(l2.getId(), e.getShipment().getShippedTo().getId());
    }
    /**
     * @param shipment shipment.
     * @return
     */
    private ShipmentSession getSession(final Shipment shipment) {
        return context.getBean(ShipmentSessionDao.class).getSession(shipment);
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
        s.setAddDateShipped(true);
        return context.getBean(ShipmentTemplateDao.class).save(s);
    }
}
