/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LeaveStartLocationRuleTest extends LeaveStartLocationRule {
    private long ids;
    private Shipment shipment;
    private final List<TrackerEvent> events = new LinkedList<>();

    /**
     * Default constructor.
     */
    public LeaveStartLocationRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        this.shipment = createShipment();
    }

    @Test
    public void testNotAcceptWithoutShipment() {
        final TrackerEvent e = createEventOutOfStartLocation(shipment);
        assertTrue(accept(new RuleContext(e, new SessionHolder())));

        e.setShipment(null);
        assertFalse(accept(new RuleContext(e, new SessionHolder())));
    }
    @Test
    public void testNotAcceptFinishedShipment() {
        final TrackerEvent e = createEventOutOfStartLocation(shipment);
        assertTrue(accept(new RuleContext(e, new SessionHolder())));

        e.getShipment().setStatus(ShipmentStatus.Arrived);
        assertFalse(accept(new RuleContext(e, new SessionHolder())));

        e.getShipment().setStatus(ShipmentStatus.Ended);
        assertFalse(accept(new RuleContext(e, new SessionHolder())));
    }
    @Test
    public void testNotAcceptWithoutStartLocation() {
        final TrackerEvent e = createEventOutOfStartLocation(shipment);
        assertTrue(accept(new RuleContext(e, new SessionHolder())));

        e.getShipment().setShippedFrom(null);
        assertFalse(accept(new RuleContext(e, new SessionHolder())));
    }

    @Test
    public void testAcceptNotYetInWatch() {
        final LocationProfile loc = shipment.getShippedFrom();
        final TrackerEvent e = createTrackerEvent(shipment,
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude());

        final SessionHolder h = new SessionHolder();
        assertTrue(accept(new RuleContext(e, h)));

        setWatchStarted(h.getSession(shipment));
        assertFalse(accept(new RuleContext(e, h)));
    }
    @Test
    public void testNotAcceptAlreadyLeaving() {
        final SessionHolder h = new SessionHolder();
        final TrackerEvent e = createEventOutOfStartLocation(shipment);

        assertTrue(accept(new RuleContext(e, h)));

        setLeavingStartLocation(h.getSession(shipment));
        assertFalse(accept(new RuleContext(e, h)));
    }
    @Test
    public void testAcceptOutOfStartLocation() {
        final SessionHolder h = new SessionHolder();
        final TrackerEvent e = createEventOutOfStartLocation(shipment);

        assertTrue(accept(new RuleContext(e, h)));
    }

    @Test
    public void testHandleLeaving() {
        final SessionHolder h = new SessionHolder();
        final TrackerEvent e = createEventOutOfStartLocation(shipment);
        setWatchStarted(h.getSession(shipment));

        assertTrue(handle(new RuleContext(e, h)));
        assertTrue(isLeavingStartLocation(shipment, h.getSession(shipment)));
    }
    @Test
    public void testHandleNotStartCheck() {
        final SessionHolder h = new SessionHolder();
        final LocationProfile loc = shipment.getShippedFrom();
        final TrackerEvent e = createTrackerEvent(shipment,
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude());

        assertFalse(handle(new RuleContext(e, h)));
        assertTrue(isStartWatch(h.getSession(shipment)));
        assertFalse(isLeavingStartLocation(shipment, h.getSession(shipment)));
    }
    @Test
    public void testHandleNotStartCheckAndLeaving() {
        final SessionHolder h = new SessionHolder();
        final TrackerEvent e = createEventOutOfStartLocation(shipment);

        assertTrue(handle(new RuleContext(e, h)));
        assertTrue(isStartWatch(h.getSession(shipment)));
        assertTrue(isLeavingStartLocation(shipment, h.getSession(shipment)));
    }
    @Test
    public void testHandleNotStartCheckAndLeavingInPast() {
        events.add(createEventOutOfStartLocation(shipment));
        final LocationProfile loc = shipment.getShippedFrom();
        final TrackerEvent e = createTrackerEvent(shipment,
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude());
        events.add(e);

        final SessionHolder h = new SessionHolder();

        assertTrue(handle(new RuleContext(e, h)));
        assertTrue(isStartWatch(h.getSession(shipment)));
        //check leaving in past.
        assertTrue(isLeavingStartLocation(shipment, h.getSession(shipment)));
    }

    /**
     * @return
     */
    private Shipment createShipment() {
        final Device d = new Device();
        d.setImei("239087023987423");
        d.setActive(true);
        d.setImei("Test device");

        final Shipment s = new Shipment();
        s.setId(++ids);
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000l));
        s.setStatus(ShipmentStatus.InProgress);

        //add start location
        s.setShippedFrom(createLocation(50, 50));

        return s;
    }

    /**
     * @param lat latitude
     * @param lon longitude
     * @return
     */
    protected LocationProfile createLocation(final double lat, final double lon) {
        final LocationProfile p = new LocationProfile();
        p.setId(++ids);
        p.setName("JUnit location: " + lat + ", " + lon);
        p.setAddress("Any JUnit place");
        p.setStart(true);
        p.getLocation().setLatitude(lat);
        p.getLocation().setLongitude(lon);
        p.setRadius(500);
        return p;
    }
    /**
     * @param s shipment.
     * @param lat latitude.
     * @param lon longitude.
     * @return
     */
    private TrackerEvent createTrackerEvent(final Shipment s, final double lat, final double lon) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(++ids);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setShipment(s);
        if (s != null) {
            e.setDevice(s.getDevice());
        } else {
            e.setDevice(shipment.getDevice());
        }
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        return e;
    }
    /**
     * @param s shipment.
     * @return event out of start location of given shipment.
     */
    private TrackerEvent createEventOutOfStartLocation(final Shipment s) {
        final LocationProfile loc = s.getShippedFrom();
        final double lat = loc.getLocation().getLatitude();
        final double lon = loc.getLocation().getLongitude();

        final TrackerEvent e = createTrackerEvent(s, lat, lon
                + LocationUtils.getLongitudeDiff(lat, lon
                + loc.getRadius() + CONTROL_DISTANCE + 10));

        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.LeaveStartLocationRule#getTrackerEvents(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<TrackerEvent> getTrackerEvents(final Shipment s) {
        final List<TrackerEvent> result = new LinkedList<>();
        for (final TrackerEvent e : events) {
            if (s.getId().equals(e.getShipment().getId())) {
                result.add(e);
            }
        }
        return result;
    }
}
