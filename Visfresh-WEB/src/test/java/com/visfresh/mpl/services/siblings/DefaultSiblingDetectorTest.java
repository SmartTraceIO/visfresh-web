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

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.CollectionUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultSiblingDetectorTest extends DefaultSiblingDetector {
    private final Map<Long, List<Shipment>> activeShipments = new HashMap<>();
    private final Map<Long, Shipment> savedShipments = new HashMap<>();
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

        assertTrue(isSiblings(sibling, masterEvents.toArray(new TrackerEvent[masterEvents.size()])));
        assertFalse(isSiblings(notSibling, masterEvents.toArray(new TrackerEvent[masterEvents.size()])));
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

        assertEquals(1l, master.getSiblingGroup().longValue());
        assertEquals(1l, sibling.getSiblingGroup().longValue());
        assertEquals(3l, notSibling.getSiblingGroup().longValue());

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

        List<Shipment> shipments = activeShipments.get(company.getId());
        if (shipments == null) {
            shipments = new LinkedList<Shipment>();
            activeShipments.put(company.getId(), shipments);
        }
        shipments.add(s);

        return s;
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
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        e.setTime(new Date(time));
        events.add(e);
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
        final List<Shipment> list = activeShipments.get(company.getId());
        CollectionUtils.sortById(list);
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment shipment) {
        savedShipments.put(shipment.getId(), shipment);
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#getTrackeEvents(com.visfresh.entities.Shipment)
     */
    @Override
    protected TrackerEvent[] getTrackeEvents(final Shipment shipment) {
        final List<TrackerEvent> events = trackerEvents.get(shipment.getId());
        if (events == null) {
            return new TrackerEvent[0];
        }
        return events.toArray(new TrackerEvent[events.size()]);
    }
}
