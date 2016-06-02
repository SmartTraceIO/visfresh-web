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
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.mock.MockShipmentShutdownService;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SetShipmentArrivedRuleTest extends BaseRuleTest {
    private SetShipmentArrivedRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public SetShipmentArrivedRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = (SetShipmentArrivedRule) context.getBean(RuleEngine.class).getRule(SetShipmentArrivedRule.NAME);
        shipment = createDefaultShipment(ShipmentStatus.InProgress, createDevice("9283470987"));
    }

    @Test
    public void testAccept() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, new SessionHolder());
        //final location not set
        assertFalse(rule.accept(req));

        final LocationProfile loc = createLocation();

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //so far
        assertFalse(rule.accept(req));

        //set nearest location
        e.setLatitude(10.);
        e.setLongitude(10.);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testShutdownDevice() {
        shipment.setArrivalNotificationWithinKm(1);
        shipment.setShutdownDeviceAfterMinutes(11);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, new SessionHolder());
        rule.accept(req);
        rule.handle(req);

        //check shipment shutdown request has sent
        final Date date = context.getBean(MockShipmentShutdownService.class).getShutdownDate(shipment.getId());
        assertNotNull(date);
    }
    @Test
    public void testHandle() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, new SessionHolder());
        assertTrue(rule.accept(req));
        rule.handle(req);

        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());
        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testPreventOfDoubleHandling() {
        shipment.setArrivalNotificationWithinKm(0);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder mgr = new SessionHolder();
        //set nearest location
        final RuleContext req = new RuleContext(e, mgr);
        assertTrue(rule.accept(req));
        rule.handle(req);

        assertFalse(rule.accept(new RuleContext(e, mgr)));
    }

    /**
     * @param loc location.
     * @return tracker event with latitude/longitude near the given location.
     */
    private TrackerEvent createEventNearLocation(final LocationProfile loc) {
        final TrackerEvent e = createEvent(loc.getLocation().getLatitude(), loc.getLocation().getLongitude());
        return e;
    }
    /**
     * @param lat latitude.
     * @param lon longitude
     * @return tracker event with given latitude longitude.
     */
    private TrackerEvent createEvent(final double lat, final double lon) {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setLatitude(lat);
        e.setLongitude(lon);
        return e;
    }
    /**
     * @return location.
     */
    private LocationProfile createLocation() {
        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        loc = context.getBean(LocationProfileDao.class).save(loc);
        return loc;
    }
}
