/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.mpl.services.arrival.DefaultArrivalEstimationCalculator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultArrivalEstimationServiceTest extends DefaultArrivalEstimationService {
    private final Map<Long, Location> locations = new HashMap<>();
    private final Map<Long, Date> etas = new HashMap<>();
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public DefaultArrivalEstimationServiceTest() {
        super();
        this.calculator = new DefaultArrivalEstimationCalculator();
    }
    @Before
    public void setUp() {
        final Location from = new Location(-33.75471658008509, 150.67508697509766);
        final Location to = new Location(-38.75471658008509, 158.67508697509766);

        this.shipment = createShipment(from, to);
        shipment.setId(7L);
    }

    @Test
    public void testUpdateEtaForShipment() {
        final LocationProfile from = shipment.getShippedFrom();
        final LocationProfile to = shipment.getShippedTo();
        final Location loc = new Location(-33.0, 151.0);

        locations.put(shipment.getId(), loc);

        //test without shipped from location
        shipment.setShippedFrom(null);
        updateEtaForShipment(shipment);
        assertNull(shipment.getEta());
        assertNull(etas.get(shipment.getId()));

        shipment.setShippedFrom(from);

        //test update with end location
        shipment.setShippedTo(null);
        updateEtaForShipment(shipment);
        assertNull(shipment.getEta());
        assertNull(etas.get(shipment.getId()));

        shipment.setShippedTo(to);

        //test without last reading
        locations.clear();
        updateEtaForShipment(shipment);
        assertNull(shipment.getEta());
        assertNull(etas.get(shipment.getId()));

        locations.put(shipment.getId(), loc);

        //test ok
        updateEtaForShipment(shipment);
        assertNotNull(shipment.getEta());
        assertNotNull(etas.get(shipment.getId()));
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.DefaultArrivalEstimationService#getCurrentLocation(com.visfresh.entities.Shipment)
     */
    @Override
    protected Location getCurrentLocation(final Shipment s) {
        return locations.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.DefaultArrivalEstimationService#updateEta(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    protected void updateEta(final Shipment s, final Date eta) {
        etas.put(s.getId(), eta);
    }
    /**
     * @param from
     * @param to
     * @return
     */
    private Shipment createShipment(final Location from, final Location to) {
        final Shipment s = new Shipment();
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000l));
        s.setStatus(ShipmentStatus.InProgress);

        //add locations
        s.setShippedFrom(createLocation(from));
        s.setShippedTo(createLocation(to));

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
     * @param lat
     * @param lon
     * @return
     */
    protected LocationProfile createLocation(final double lat, final double lon) {
        final LocationProfile p = new LocationProfile();
        p.setName("JUnit location: " + lat + ", " + lon);
        p.setAddress("Any JUnit place");
        p.setStart(true);
        p.getLocation().setLatitude(lat);
        p.getLocation().setLongitude(lon);
        return p;
    }
}
