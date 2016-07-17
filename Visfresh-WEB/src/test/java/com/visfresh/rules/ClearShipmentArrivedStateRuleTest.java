/**
 *
 */
package com.visfresh.rules;

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
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.mpl.services.ArrivalServiceImpl;
import com.visfresh.utils.LocationTestUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ClearShipmentArrivedStateRuleTest extends
        ClearShipmentArrivedStateRule {
    private final List<LocationProfile> locations = new LinkedList<>();
    private Shipment shipment;
    private long id;
    private Company company;

    /**
     * Default constructor.
     */
    public ClearShipmentArrivedStateRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        this.arrivalService = new ArrivalServiceImpl() {
            /* (non-Javadoc)
             * @see com.visfresh.mpl.services.ArrivalServiceImpl#getLocations(java.util.Collection)
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
        };

        this.company = new Company();
        company.setId(id++);

        shipment = createShipment(createDevice("9283470987"));
    }

    @Test
    public void testNotAcceptWithoutHistory() {
        final SessionHolder h = new SessionHolder(shipment);

        final double lon = 10;
        final double lat = 10;
        final TrackerEvent e = createEvent(TrackerEventType.AUT, lat, lon);
        final LocationProfile loc = createLocation(lat, lon);

        arrivalService.handleNearLocation(loc, e, h.getSession(shipment));
        arrivalService.clearLocationHistory(loc, h.getSession(shipment));

        assertFalse(accept(new RuleContext(e, h)));
    }
    @Test
    public void testNotAcceptWithoutShipment() {
        final SessionHolder h = new SessionHolder(shipment);

        final double lon = 10;
        final double lat = 10;
        final TrackerEvent e = createEvent(TrackerEventType.AUT, lat, lon);
        final LocationProfile loc = createLocation(lat, lon);

        arrivalService.handleNearLocation(loc, e, h.getSession(shipment));
        e.setShipment(null);

        assertFalse(accept(new RuleContext(e, h)));
    }
    @Test
    public void testNotAcceptWithoutCoordinates() {
        final SessionHolder h = new SessionHolder(shipment);

        final double lon = 10;
        final double lat = 10;
        final TrackerEvent e = createEvent(TrackerEventType.AUT, lat, lon);
        final LocationProfile loc = createLocation(lat, lon);

        arrivalService.handleNearLocation(loc, e, h.getSession(shipment));
        e.setLatitude(null);

        assertFalse(accept(new RuleContext(e, h)));
    }
    @Test
    public void testAccept() {
        final SessionHolder h = new SessionHolder(shipment);

        final double lon = 10;
        final double lat = 10;
        final TrackerEvent e = createEvent(TrackerEventType.AUT, lat, lon);
        final LocationProfile loc = createLocation(lat, lon);

        arrivalService.handleNearLocation(loc, e, h.getSession(shipment));

        assertTrue(accept(new RuleContext(e, h)));
    }
    @Test
    public void testHandle() {
        final SessionHolder h = new SessionHolder(shipment);

        final double lon = 10;
        final double lat = 10;
        final TrackerEvent e = createEvent(TrackerEventType.AUT, lat, lon);
        final LocationProfile loc = createLocation(lat, lon);

        arrivalService.handleNearLocation(loc, e, h.getSession(shipment));
        assertTrue(arrivalService.hasEnteredLocations(h.getSession(shipment)));

        //test near location
        assertFalse(handle(new RuleContext(e, h)));
        assertTrue(arrivalService.hasEnteredLocations(h.getSession(shipment)));

        //test out of location
        final double distance = LocationTestUtils.getLongitudeDiff(
                loc.getLocation().getLongitude(), loc.getRadius() + 300);
        e.setLatitude(e.getLatitude() + distance);

        assertFalse(handle(new RuleContext(e, h)));
        assertFalse(arrivalService.hasEnteredLocations(h.getSession(shipment)));
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
    private LocationProfile createLocation(final double lat, final double lon) {
        final LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);
        loc.setId(id++);
        loc.setRadius(500);
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
