/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.SessionHolder;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalServiceTest extends ArrivalServiceImpl {
    private final List<LocationProfile> locations = new LinkedList<>();
    private Shipment shipment;
    private long id;
    private Company company;

    /**
     * Default constructor.
     */
    public ArrivalServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        this.company = new Company();
        company.setId(id++);

        shipment = createShipment(createDevice("9283470987"));
    }
    @Test
    public void testIsNearLocation() {
        final int radius = 1500;
        final LocationProfile loc = createLocation();
        loc.setRadius(radius);

        final Location l = new Location(loc.getLocation().getLatitude(), 0);
        final double distance = LocationUtils.getLongitudeDiff(l.getLatitude(), radius);

        l.setLongitude(loc.getLocation().getLongitude() + distance + 100);
        assertFalse(isNearLocation(loc, l));

        l.setLongitude(loc.getLocation().getLongitude() + distance / 2.);
        assertTrue(isNearLocation(loc, l));
    }
    @Test
    public void testJustEnteredLocation() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final TrackerEvent e = createEvent(TrackerEventType.AUT,
                l.getLatitude(), l.getLongitude());

        //simple event.
        SessionHolder h = new SessionHolder();
        assertFalse(handleNearLocation(loc, e, h.getSession(shipment)));
        assertEquals(1, getEnteredLocations(h.getSession(shipment)).size());

        //BRT event.
        e.setType(TrackerEventType.BRT);
        h = new SessionHolder();
        assertFalse(handleNearLocation(loc, e, h.getSession(shipment)));
        assertEquals(1, getEnteredLocations(h.getSession(shipment)).size());

        //STP event.
        e.setType(TrackerEventType.STP);
        h = new SessionHolder();
        assertFalse(handleNearLocation(loc, e, h.getSession(shipment)));
        assertEquals(1, getEnteredLocations(h.getSession(shipment)).size());
    }
    @Test
    public void testWithFirstBrt() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.BRT,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final SessionHolder h = new SessionHolder();
        final ShipmentSession session = h.getSession(shipment);

        assertFalse(handleNearLocation(loc, e, session));

        e.setType(TrackerEventType.AUT);
        //check small interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l - 10l));
        assertFalse(handleNearLocation(loc, e, session));

        //check correct interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l + 10l));
        assertTrue(handleNearLocation(loc, e, session));
    }
    @Test
    public void testWithFirstNext() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.AUT,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final SessionHolder h = new SessionHolder();
        final ShipmentSession session = h.getSession(shipment);

        assertFalse(handleNearLocation(loc, e, session));

        e.setType(TrackerEventType.BRT);
        //check small interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l - 10l));
        assertFalse(handleNearLocation(loc, e, session));

        //check correct interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l + 10l));
        assertTrue(handleNearLocation(loc, e, session));
    }
    @Test
    public void testWithStopFirst() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.STP,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final SessionHolder h = new SessionHolder();
        final ShipmentSession session = h.getSession(shipment);

        assertFalse(handleNearLocation(loc, e, session));

        e.setType(TrackerEventType.AUT);
        //check small interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l - 10l));
        assertFalse(handleNearLocation(loc, e, session));

        //check correct interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l + 10l));
        assertTrue(handleNearLocation(loc, e, session));
    }
    @Test
    public void testWithStopNext() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.AUT,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final SessionHolder h = new SessionHolder();
        final ShipmentSession session = h.getSession(shipment);

        assertFalse(handleNearLocation(loc, e, session));

        e.setType(TrackerEventType.STP);
        //check small interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l - 10l));
        assertFalse(handleNearLocation(loc, e, session));

        //check correct interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l + 10l));
        assertFalse(handleNearLocation(loc, e, session));
    }
    @Test
    public void testWithoutBrtOrStop() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.AUT,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final SessionHolder h = new SessionHolder();
        final ShipmentSession session = h.getSession(shipment);

        assertFalse(handleNearLocation(loc, e, session));

        //check small interval
        e.setTime(new Date(arrivalTime + 10 * 60 * 1000l + 10l));
        assertFalse(handleNearLocation(loc, e, session));

        //check correct interval
        e.setTime(new Date(arrivalTime + 20 * 60 * 1000l + 10l));
        assertTrue(handleNearLocation(loc, e, session));
    }
    @Test
    public void testHasEnteredLocations() {
        //create location
        final LocationProfile loc = createLocation();
        loc.setRadius(1500);

        final double distance = LocationUtils.getLongitudeDiff(
                loc.getLocation().getLatitude(), loc.getRadius());

        final Location l = new Location(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude() + distance / 2.);

        final long arrivalTime = System.currentTimeMillis()
                - 2 * 10 * 60 * 1000l - 10l;

        final TrackerEvent e = createEvent(TrackerEventType.AUT,
                l.getLatitude(), l.getLongitude());
        e.setTime(new Date(arrivalTime));

        //simple event.
        final ShipmentSession session = new SessionHolder().getSession(shipment);

        assertFalse(hasEnteredLocations(session));

        handleNearLocation(loc, e, session);
        assertTrue(hasEnteredLocations(session));
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.ArrivalServiceImpl#getLocations(java.util.LinkedList)
     */
    @Override
    protected List<LocationProfile> getLocations(final Collection<Long> locs) {
        final Set<Long> ids = new HashSet<>(locs);

        final List<LocationProfile> list = new LinkedList<>();
        for (final LocationProfile lp : locations) {
            if (ids.contains(lp.getId())) {
                list.add(lp);
            }
        }

        return list;
    }
    /**
     * @param type tracker event type.
     * @param lat latitude.
     * @param lon longitude
     * @return tracker event with given latitude longitude.
     */
    private TrackerEvent createEvent(final TrackerEventType type,
            final double lat, final double lon) {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(type);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setId(id++);
        return e;
    }
    /**
     * @return location.
     */
    private LocationProfile createLocation() {
        final LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        loc.setId(id++);
        this.locations.add(loc);
        return loc;
    }
    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setCompany(company);
        d.setDescription("Test device");
        return d;
    }
    /**
     * @param name shipment name.
     * @return
     */
    protected Shipment createShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(company);
        s.setStatus(ShipmentStatus.InProgress);
        s.setId(id++);
        return s;
    }
}
