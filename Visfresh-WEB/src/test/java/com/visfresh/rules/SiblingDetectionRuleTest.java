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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectionRuleTest extends SiblingDetectionRule {
    /**
     * Default constructor.
     */
    public SiblingDetectionRuleTest() {
        super();
    }
    private final List<Shipment> shipments = new LinkedList<>();
    private final Map<Long, List<TrackerEvent>> trackerEvents = new HashMap<>();

    private Company company;

    @Before
    public void setUp() {
        company = new Company();
        company.setId(1l);
        company.setName("JUnit Company");
    }

    @Test
    public void testNotAcceptIfNotShipment() {
        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        assertFalse(accept(new RuleContext(e, null)));
    }
    @Test
    public void testNotAcceptIfNotActiveShipment() {
        final Shipment s = createShipment(7l);
        s.setStatus(ShipmentStatus.Arrived);

        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        e.setShipment(s);

        assertFalse(accept(new RuleContext(e, null)));
    }
    @Test
    public void testNotAcceptIfAlreadyHandled() {
        final Shipment s = createShipment(7l);
        s.setStatus(ShipmentStatus.Default);

        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        e.setShipment(s);

        assertTrue(accept(new RuleContext(e, null)));
    }
    @Test
    public void testIsSiblings() {
        final Shipment master = createShipment(1l);
        final Shipment sibling = createShipment(2l);
        final Shipment notSibling = createShipment(3l);

        //crete master event list
        final List<TrackerEvent> masterEvents = new LinkedList<TrackerEvent>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            addEvent(masterEvents, master, x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt);
            addEvent(trackerEvents, sibling, x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l);
            addEvent(trackerEvents, notSibling,
                    x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l);
        }

        assertTrue(isSiblings(getTrackeEvents(sibling), masterEvents));
        assertFalse(isSiblings(getTrackeEvents(notSibling), masterEvents));
    }
    @Test
    public void testExcludeWithSmallPath() {
        final Shipment master = createShipment(1l);
        final Shipment sibling = createShipment(2l);
        final Shipment notSibling = createShipment(3l);

        //crete master event list
        final List<TrackerEvent> masterEvents = new LinkedList<TrackerEvent>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH / 10);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01);
        for (int i = 0; i < count; i++) {
            addEvent(masterEvents, master, x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt);
            addEvent(trackerEvents, sibling, x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l);
            addEvent(trackerEvents, notSibling,
                    x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l);
        }

        assertFalse(isSiblings(getTrackeEvents(sibling), masterEvents));
        assertFalse(isSiblings(getTrackeEvents(notSibling), masterEvents));
    }
    @Test
    public void testNotIntersectingByTime() {
        final Shipment s1 = createShipment(1l);
        final Shipment s2 = createShipment(2l);

        //crete master event list
        final List<TrackerEvent> e2 = new LinkedList<TrackerEvent>();
        final List<TrackerEvent> e1 = new LinkedList<TrackerEvent>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        addEvent(e1, s1, x0, y0, t0);
        addEvent(e1, s1, x0, y0, t0);
        addEvent(e1, s1, x0, y0, t0);

        //add tracker events for given shipment
        addEvent(e2, s2, x0, y0, t0 + dt);
        addEvent(e2, s2, x0, y0, t0 + dt);
        addEvent(e2, s2, x0, y0, t0 + dt);

        assertFalse(isSiblings(e1, e2));
        assertFalse(isSiblings(e2, e1));
    }
    @Test
    public void testIsTimeNotIntersecting() {
        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, MAX_DISTANCE_AVERAGE) / 2.;
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEvent> l1 = new LinkedList<TrackerEvent>();
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        final List<TrackerEvent> l2 = new LinkedList<TrackerEvent>();
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertFalse(isSiblings(l1, l2));
        assertFalse(isSiblings(l2, l1));
    }
    @Test
    public void testIsTimeIntersecting() {
        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, MAX_DISTANCE_AVERAGE) / 5.;
        final double minPath = LocationUtils.getLongitudeDiff(lat, MIN_PATH);
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEvent> l1 = new LinkedList<TrackerEvent>();
        final List<TrackerEvent> l2 = new LinkedList<TrackerEvent>();

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        //intersected time
        final int count = (int) Math.round(minPath / dlon) + 1;
        for (int i = 0; i < count; i++) {
            l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
            l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        }

        //l1 stopped l2 continued
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertTrue(isSiblings(l1, l2));
    }

    @Test
    public void testIncludeOldSiblings() {
        final Shipment master = createShipment(11l);
        final Shipment sibling = createShipment(12l);
        final Shipment oldSibling = createShipment(3l);
        oldSibling.setStatus(ShipmentStatus.Arrived);
        master.getSiblings().add(oldSibling.getId());

        //crete master event list
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        TrackerEvent lastEvent = null;
        for (int i = 0; i < count; i++) {
            lastEvent = addEvent(trackerEvents, master, x0 + 0.01 * i, y0 + 0.01 * i,
                    t0 + i * dt);
            addEvent(trackerEvents, sibling, x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l);
            addEvent(trackerEvents, oldSibling, x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l);
        }

        assertTrue(handle(new RuleContext(lastEvent, null)));

        //check sibling group
        assertTrue(master.getSiblings().contains(sibling.getId()));
        assertTrue(master.getSiblings().contains(oldSibling.getId()));

        //check sibling count
        assertEquals(2, master.getSiblingCount());
    }

    @Test
    public void testUpdateSiblings() {
        final Shipment master = createShipment(1l);
        final Shipment sibling = createShipment(2l);
        final Shipment notSibling = createShipment(3l);

        //crete master event list
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        TrackerEvent lastEvent = null;
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            lastEvent = addEvent(trackerEvents, master, x0 + 0.01 * i, y0 + 0.01 * i,
                    t0 + i * dt);
            addEvent(trackerEvents, sibling, x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l);
            addEvent(trackerEvents, notSibling,
                    x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l);
        }

        assertTrue(handle(new RuleContext(lastEvent, null)));

        assertTrue(master.getSiblings().contains(sibling.getId()));
        assertFalse(master.getSiblings().contains(notSibling.getId()));

        //check sibling count
        assertEquals(1, master.getSiblingCount());
    }
    @Test
    public void testHandleReturnsFalseIfNotSiblings() {
        final Shipment master = createShipment(1l);
        final Shipment notSibling = createShipment(3l);

        //crete master event list
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        TrackerEvent lastEvent = null;
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            lastEvent = addEvent(trackerEvents, master, x0 + 0.01 * i, y0 + 0.01 * i,
                    t0 + i * dt);
            addEvent(trackerEvents, notSibling,
                    x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l);
        }

        assertFalse(handle(new RuleContext(lastEvent, null)));
    }

    /**
     * @param id shipment ID.
     * @return
     */
    protected Shipment createShipment(final long id) {
        final Shipment s = new Shipment();
        s.setId(id);
        s.setCompany(company);
        s.setShipmentDescription("Test_" + id);
        s.setDevice(createDevice(id));
        s.setStatus(ShipmentStatus.InProgress);

        shipments.add(s);
        return s;
    }

    /**
     * @param id
     * @return
     */
    private Device createDevice(final long id) {
        final String imei = Long.toString(1000000000l + id);
        final Device d = new Device();
        d.setCompany(company);
        d.setImei(imei);
        d.setName("JUnit-" + id);
        return d;
    }

    /**
     * @param events event list.
     * @param shipment shipment.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param time event time.
     * @return tracker event.
     */
    private TrackerEvent addEvent(final List<TrackerEvent> events, final Shipment shipment,
            final double latitude, final double longitude, final long time) {
        final TrackerEvent e = createTrackerEvent(latitude, longitude, time);
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        events.add(e);
        return e;
    }
    /**
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    private TrackerEvent createTrackerEvent(final double latitude, final double longitude, final long time) {
        final TrackerEvent e = new TrackerEvent();
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        e.setTime(new Date(time));
        e.setCreatedOn(e.getTime());
        return e;
    }

    /**
     * @param events event map.
     * @param shipment shipment.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param time event time.
     * @return tracker event.
     */
    private TrackerEvent addEvent(final Map<Long, List<TrackerEvent>> events,
            final Shipment shipment, final double latitude, final double longitude, final long time) {
        final Long id = shipment.getId();

        List<TrackerEvent> list = events.get(id);
        if (list == null) {
            list = new LinkedList<TrackerEvent>();
            events.put(id, list);
        }

        return addEvent(list, shipment, latitude, longitude, time);
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#findActiveShipments(com.visfresh.entities.Company)
     */
    @Override
    protected List<Shipment> findActiveShipments(final Company company) {
        final LinkedList<Shipment> list = new LinkedList<>();
        for (final Shipment s : shipments) {
            if (!s.hasFinalStatus()) {
                list.add(s);
            }
        }
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#updateSiblingInfo(com.visfresh.entities.Shipment, java.util.Set)
     */
    @Override
    protected void updateSiblingInfo(final Shipment master, final Set<Long> set) {
        master.setSiblingCount(set.size());
        master.getSiblings().clear();
        master.getSiblings().addAll(set);
        master.setSiblingCount(set.size());
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#getEventsFromDb(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<TrackerEvent> getEventsFromDb(final Shipment shipment) {
        final List<TrackerEvent> events = trackerEvents.get(shipment.getId());
        if (events == null) {
            return new LinkedList<>();
        }
        return new LinkedList<TrackerEvent>(events);
    }
}
