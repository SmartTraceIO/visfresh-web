/**
 *
 */
package com.visfresh.mpl.services.siblings;

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
import com.visfresh.utils.LocationTestUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultSiblingDetectorTest extends DefaultSiblingDetector {
    private final List<Shipment> shipments = new LinkedList<>();
    private final Map<Long, List<TrackerEvent>> trackerEvents = new HashMap<>();

    private Company company;
    /**
     * Default constructor.
     */
    public DefaultSiblingDetectorTest() {
        super(0);
    }

    @Before
    public void setUp() {
        company = new Company();
        company.setId(1l);
        company.setName("JUnit Company");
    }

    @Test
    public void testIsSiblings() {
        final Shipment master = creaeShipment(1l);
        final Shipment sibling = creaeShipment(2l);
        final Shipment notSibling = creaeShipment(3l);

        //crete master event list
        final List<TrackerEvent> masterEvents = new LinkedList<TrackerEvent>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        addEvent(masterEvents, master, x0, y0, t0);
        addEvent(masterEvents, master, x0 + 0.01, y0 + 0.01, t0 + dt);
        addEvent(masterEvents, master, x0 + 0.02, y0 + 0.02, t0 + 2 * dt);

        //add tracker events for given shipment
        addEvent(trackerEvents, sibling, x0, y0, t0);
        addEvent(trackerEvents, sibling, x0 + 0.015, y0 + 0.015, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, sibling, x0 + 0.025, y0 + 0.025, t0 + 2 * dt + 60 * 1000l);

        //add tracker events for given shipment
        addEvent(trackerEvents, notSibling, x0, y0, t0);
        addEvent(trackerEvents, notSibling, x0 - 0.15, y0 - 0.15, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, notSibling, x0 - 0.25, y0 - 0.25, t0 + 2 * dt + 60 * 1000l);

        assertTrue(isSiblings(getTrackeEvents(sibling), masterEvents));
        assertFalse(isSiblings(getTrackeEvents(notSibling), masterEvents));
    }
    @Test
    public void testNotIntersectingByTime() {
        final Shipment s1 = creaeShipment(1l);
        final Shipment s2 = creaeShipment(2l);

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
        final double dlon = LocationTestUtils.getLongitudeDiff(
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
        final double dlon = LocationTestUtils.getLongitudeDiff(
                lat, MAX_DISTANCE_AVERAGE) / 5.;
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEvent> l1 = new LinkedList<TrackerEvent>();
        final List<TrackerEvent> l2 = new LinkedList<TrackerEvent>();

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

//        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//
//        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//
//        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

//        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
//        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

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
        assertFalse(isSiblings(l2, l1));
    }

    @Test
    public void testIncludeOldSiblings() {
        final Shipment master = creaeShipment(11l);
        final Shipment sibling = creaeShipment(12l);
        final Shipment oldSibling = creaeShipment(3l);
        oldSibling.setStatus(ShipmentStatus.Arrived);
        master.getSiblings().add(oldSibling.getId());

        //crete master event list
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        addEvent(trackerEvents, master, x0, y0, t0);
        addEvent(trackerEvents, master, x0 + 0.01, y0 + 0.01, t0 + dt);
        addEvent(trackerEvents, master, x0 + 0.02, y0 + 0.02, t0 + 2 * dt);

        //add tracker events for given shipment
        addEvent(trackerEvents, sibling, x0, y0, t0);
        addEvent(trackerEvents, sibling, x0 + 0.015, y0 + 0.015, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, sibling, x0 + 0.025, y0 + 0.025, t0 + 2 * dt + 60 * 1000l);

        //add tracker events for given shipment
        addEvent(trackerEvents, oldSibling, x0, y0, t0);
        addEvent(trackerEvents, oldSibling, x0 + 0.015, y0 + 0.015, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, oldSibling, x0 + 0.025, y0 + 0.025, t0 + 2 * dt + 60 * 1000l);

        this.updateShipmentSiblingsForCompany(company);

        //check sibling group
        assertTrue(master.getSiblings().contains(sibling.getId()));
        assertTrue(master.getSiblings().contains(oldSibling.getId()));

        //check sibling count
        assertEquals(2, master.getSiblingCount());
        assertEquals(1, sibling.getSiblingCount());
        assertEquals(0, oldSibling.getSiblingCount());
    }

    @Test
    public void testUpdateShipmentSiblingsForCompany() {
        final Shipment master = creaeShipment(1l);
        final Shipment sibling = creaeShipment(2l);
        final Shipment notSibling = creaeShipment(3l);

        //crete master event list
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        addEvent(trackerEvents, master, x0, y0, t0);
        addEvent(trackerEvents, master, x0 + 0.01, y0 + 0.01, t0 + dt);
        addEvent(trackerEvents, master, x0 + 0.02, y0 + 0.02, t0 + 2 * dt);

        //add tracker events for given shipment
        addEvent(trackerEvents, sibling, x0, y0, t0);
        addEvent(trackerEvents, sibling, x0 + 0.015, y0 + 0.015, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, sibling, x0 + 0.025, y0 + 0.025, t0 + 2 * dt + 60 * 1000l);

        //add tracker events for given shipment
        addEvent(trackerEvents, notSibling, x0, y0, t0);
        addEvent(trackerEvents, notSibling, x0 - 0.15, y0 - 0.15, t0 + dt + 60 * 1000l);
        addEvent(trackerEvents, notSibling, x0 - 0.25, y0 - 0.25, t0 + 2 * dt + 60 * 1000l);

        this.updateShipmentSiblingsForCompany(company);

        assertTrue(master.getSiblings().contains(sibling.getId()));
        assertFalse(master.getSiblings().contains(notSibling.getId()));

        assertTrue(sibling.getSiblings().contains(master.getId()));
        assertFalse(sibling.getSiblings().contains(notSibling.getId()));

        assertEquals(0, notSibling.getSiblings().size());

        //check sibling count
        assertEquals(1, getSiblingCount(master));
        assertEquals(1, getSiblingCount(sibling));
        assertEquals(0, getSiblingCount(notSibling));
    }

    /**
     * @param id shipment ID.
     * @return
     */
    protected Shipment creaeShipment(final long id) {
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
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#getShipments(java.util.Set)
     */
    @Override
    protected List<Shipment> getShipments(final Set<Long> ids) {
        final List<Shipment> list = new LinkedList<>();
        for (final Shipment s : this.shipments) {
            if (ids.contains(s.getId())) {
                list.add(s);
            }
        }
        return list;
    }
}
