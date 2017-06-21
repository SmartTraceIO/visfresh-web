/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class UpdateInterimStopRuleTest extends UpdateInterimStopRule {
    private long ids = 0;
    private final Map<Long, Integer> stopMinutes = new HashMap<>();
    private final Map<Long, List<InterimStop>> stops = new HashMap<>();
    private List<LocationProfile> locations;
    private Shipment shipment;
    private Map<Long, List<LocationProfile>> interimLocations = new HashMap<>();

    /**
     * Default constructor.
     */
    public UpdateInterimStopRuleTest() {
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

        saveInterimLocations(shipment, locations);
    }

    @Test
    public void testAccept() {
        //test success accept
        final SessionHolder mgr = createSessionHolder(true);

        final LocationProfile loc = locations.get(0);
        final InterimStopInfo info = createInterimStopInfo(loc);
        setInterimStopState(mgr.getSession(shipment), info);
        assertFalse(accept(new RuleContext(createTrackerEvent(loc), mgr)));

        //test far interim stop
        final InterimStop stop = createInterimStop(shipment, loc);
        info.setId(stop.getId());
        setInterimStopState(mgr.getSession(shipment), info);

        assertFalse(accept(new RuleContext(createTrackerEvent(locations.get(1)), mgr)));
        assertTrue(accept(new RuleContext(createTrackerEvent(loc), mgr)));
    }
    @Test
    public void testHandle() {
        //test success accept
        final SessionHolder mgr = createSessionHolder(true);
        saveInterimLocations(shipment, locations);

        //simulate previous stop
        setInterimStopState(mgr.getSession(shipment), new InterimStopInfo());

        final TrackerEvent e = createTrackerEvent(locations.get(0));

        //check update time
        assertFalse(handle(new RuleContext(e, mgr)));
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
     * @param s shipment.
     * @param locs locations.
     */
    private void saveInterimLocations(final Shipment s, final List<LocationProfile> locs) {
        interimLocations.put(s.getId(), locs);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.InterimStopRule#getInterimLocations(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<LocationProfile> getInterimLocations(final Shipment shipment) {
        return interimLocations.get(shipment.getId());
    }
    /**
     * @param setLeaveStart whether or not should set the leving the start flag.
     * @return session holder.
     */
    private SessionHolder createSessionHolder(final boolean setLeaveStart) {
        final SessionHolder s = new SessionHolder(shipment);
        if (setLeaveStart) {
            LeaveStartLocationRule.setLeavingStartLocation(s.getSession(shipment));
        }
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
     * @param loc location profile.
     * @return interim stop info.
     */
    private InterimStopInfo createInterimStopInfo(final LocationProfile loc) {
        final InterimStopInfo stop = new InterimStopInfo();
        stop.setLatitude(loc.getLocation().getLatitude());
        stop.setLongitude(loc.getLocation().getLongitude());
        stop.setStartTime(System.currentTimeMillis());
        return stop;
    }
    /**
     * @param locationProfile
     * @return
     */
    private InterimStop createInterimStop(final Shipment shipment, final LocationProfile locationProfile) {
        final InterimStop stop = new InterimStop();
        stop.setDate(new Date());
        stop.setId(ids++);
        stop.setLocation(locationProfile);
        stop.setTime(0);

        save(shipment, stop);
        return stop;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractInterimStopRule#getInterimStop(com.visfresh.entities.Shipment, com.visfresh.rules.AbstractInterimStopRule.InterimStopInfo)
     */
    @Override
    protected InterimStop getInterimStop(final Shipment shipment, final InterimStopInfo info) {
        final List<InterimStop> list = stops.get(shipment.getId());
        return list == null || list.size() == 0 ? null : list.get(0);
    }
}
