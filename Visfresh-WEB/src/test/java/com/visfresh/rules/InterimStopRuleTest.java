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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopRuleTest extends InterimStopRule {
    private long ids = 0;
    private final Map<Long, Integer> stopMinutes = new HashMap<>();
    private final Map<Long, List<InterimStop>> stops = new HashMap<>();
    private List<LocationProfile> locations;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public InterimStopRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        final Shipment s = createShipment();
        final LocationProfile l1 = createLocation(1, 2);
        final LocationProfile l2 = createLocation(3, 4);
        final LocationProfile l3 = createLocation(5, 6);

        final List<LocationProfile> locs = new LinkedList<>();
        locs.add(l1);
        locs.add(l2);
        locs.add(l3);

        this.locations = locs;
        this.shipment = s;
    }

    @Test
    public void testAccept() {
        //test success accept
        final SessionHolder mgr = new SessionHolder();

        //test not accept with not saved interim locations
        assertFalse(accept(new RuleContext(createTrackerEvent(shipment, 1, 2), mgr)));

        //test accept correct request
        saveInterimLocations(mgr.getSession(shipment), locations);
        assertTrue(accept(new RuleContext(createTrackerEvent(shipment, 1, 2), mgr)));

        //test not accept far location
        assertFalse(accept(new RuleContext(createTrackerEvent(shipment, 7, 8), mgr)));

        //test not accept null shipment
        assertFalse(accept(new RuleContext(createTrackerEvent(null, 7, 8), mgr)));

        //test not accept shipment in final status
        shipment.setStatus(ShipmentStatus.Arrived);
        assertFalse(accept(new RuleContext(createTrackerEvent(shipment, 7, 8), mgr)));

        shipment.setStatus(ShipmentStatus.InProgress);

        //test accept far location but in interim stop state
        setInterimStopState(mgr.getSession(shipment), new InterimStopInfo());
        assertTrue(accept(new RuleContext(createTrackerEvent(shipment, 7, 8), mgr)));
    }

    @Test
    public void testHandleFirstReading() {
        //test success accept
        final SessionHolder mgr = new SessionHolder();
        saveInterimLocations(mgr.getSession(shipment), locations);

        final TrackerEvent e = createTrackerEvent(locations.get(0));
        assertFalse(handle(new RuleContext(e, mgr)));

        //check interim stop info supplied at state
        assertNotNull(getInterimStop(mgr.getSession(shipment)));

        //but not interim stop created
        assertEquals(0, stops.size());
    }
    @Test
    public void testHandleFirstReadingWithInit() {
        //test success accept
        final SessionHolder mgr = new SessionHolder();
        saveInterimLocations(mgr.getSession(shipment), locations);

        final TrackerEvent e = createTrackerEvent(locations.get(0));
        e.setType(TrackerEventType.STP);
        assertFalse(handle(new RuleContext(e, mgr)));

        //check interim stop info supplied at state
        assertNotNull(getInterimStop(mgr.getSession(shipment)));

        //and interim stop created too
        assertEquals(1, stops.size());
    }

    @Test
    public void testHandleSecondReading() {
        //test success accept
        final SessionHolder state = new SessionHolder();
        saveInterimLocations(state.getSession(shipment), locations);

        //simulate previous stop
        setInterimStopState(state.getSession(shipment), new InterimStopInfo());

        final TrackerEvent e = createTrackerEvent(locations.get(0));
        assertFalse(handle(new RuleContext(e, state)));

        //and interim stop created too
        assertEquals(1, stops.size());
    }
    @Test
    public void testHandleEndOfStop() {
        //test success accept
        final SessionHolder mgr = new SessionHolder();
        saveInterimLocations(mgr.getSession(shipment), locations);

        //simulate prevous stop
        setInterimStopState(mgr.getSession(shipment), new InterimStopInfo());

        final TrackerEvent e = createTrackerEvent(shipment, 100, 100);
        assertFalse(handle(new RuleContext(e, mgr)));

        //and interim stop created too
        assertEquals(0, stops.size());
        assertNull(getInterimStop(mgr.getSession(shipment)));
    }
    @Test
    public void testUpdateStopTime() {
        //test success accept
        final SessionHolder mgr = new SessionHolder();
        saveInterimLocations(mgr.getSession(shipment), locations);

        //simulate previous stop
        setInterimStopState(mgr.getSession(shipment), new InterimStopInfo());

        final TrackerEvent e = createTrackerEvent(locations.get(0));
        assertFalse(handle(new RuleContext(e, mgr)));

        //and interim stop created too
        assertEquals(1, stops.size());
        assertEquals(0, this.stopMinutes.size());

        //check update time
        assertFalse(handle(new RuleContext(e, mgr)));
        assertEquals(1, stops.size());
        assertEquals(1, this.stopMinutes.size());
    }

    /**
     * @param l
     * @return
     */
    private TrackerEvent createTrackerEvent(final LocationProfile l) {
        return createTrackerEvent(shipment, l.getLocation().getLatitude(), l.getLocation().getLongitude());
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

    /* (non-Javadoc)
     * @see com.visfresh.rules.InterimStopRule#save(com.visfresh.entities.Shipment, com.visfresh.entities.InterimStop)
     */
    @Override
    protected Long save(final Shipment shipment, final InterimStop stop) {
        List<InterimStop> list = stops.get(shipment.getId());
        if (list == null) {
            list = new LinkedList<>();
            stops.put(shipment.getId(), list);
        }
        list.add(stop);

        stop.setId(++ids);
        return stop.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.InterimStopRule#updateStopTime(com.visfresh.rules.InterimStopRule.InterimStopInfo, int)
     */
    @Override
    protected void updateStopTime(final InterimStopInfo stop, final int minutes) {
        stopMinutes.put(stop.getId(), minutes);
    }
    /**
     * @return
     */
    private Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setId(++ids);
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000l));
        s.setStatus(ShipmentStatus.InProgress);
        return s;
    }
    /**
     * @param loc the location.
     * @return location profile.
     */
    protected LocationProfile createLocation(final Location loc) {
        return createLocation(loc.getLatitude(), loc.getLongitude());
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
}
