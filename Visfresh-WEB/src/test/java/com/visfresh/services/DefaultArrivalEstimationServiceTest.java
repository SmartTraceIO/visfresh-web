/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultArrivalEstimationServiceTest {
    private DefaultArrivalEstimationService service;

    /**
     * Default constructor.
     */
    public DefaultArrivalEstimationServiceTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        service = new DefaultArrivalEstimationService();
    }

    @Test
    public void testSomeLocation() {
        final Location loc = new Location(10, 20);
        final Shipment s = createShipment(loc, loc);

        final Date currentDate = new Date(System.currentTimeMillis() - 1000000L);

        final ArrivalEstimation est = service.estimateArrivalDate(s, loc, currentDate);
        assertEquals(currentDate, est.getArrivalDate());
        assertEquals(100, est.getPercentageComplete());
    }

    @Test
    public void testEstimateArrivalDate() {
//        "startLocation": "Old Pittwater Road, Brookvale, Sydney, New South Wales, 2099, Australia",
//        "startTimeStr": "09:51 16 Dec 15",
//        "startTimeISO": "2015-12-16 09:51",
//        "endLocation": "Nepean Avenue, Penrith, Upper Castlereagh, New South Wales, Australia",
//        "eta": "2015-12-15 11:55",
//        "etaStr": "11:55 15 Dec 15",
//        "currentLocation": "Not determined",
//        "currentLocationForMap": {
//          "latitude": -33.0,
//          "longitude":
//        },
//        "minTemp": -13.869999885559082,
//        "maxTemp": 27.1200008392334,
//        "timeOfFirstReading": "2015-12-16 10:00",
//        "timeOfLastReading": "2015-12-17 07:20",
//        "locations": [

        final Location loc1 = new Location(-33.75471658008509, 150.67508697509766);
        final Location loc2 = new Location(-33.75471658008509, 150.67508697509766);
        final Location currentLoc = new Location(-33.0, 151.0);

        final Shipment s = createShipment(loc1, loc2);
        final Date startDate = new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000l);
        s.setShipmentDate(startDate);

        final Date currentDate = new Date(System.currentTimeMillis() - 100000L);

        final ArrivalEstimation est = service.estimateArrivalDate(s, currentLoc, currentDate);
        assertTrue(startDate.before(est.getArrivalDate()));
        assertTrue(est.getPercentageComplete() > 0);
    }
    /**
     * @param from
     * @param to
     * @return
     */
    private Shipment createShipment(final Location from, final Location to) {
        final Shipment s = new Shipment();
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date());
        s.setStatus(ShipmentStatus.InProgress);

        //add locations
        s.setShippedFrom(createLocation(from));
        s.setShippedTo(createLocation(to));

        return s;
    }

    /**
     * @param loc
     * @return
     */
    protected LocationProfile createLocation(final Location loc) {
        final LocationProfile p = new LocationProfile();
        p.setName("JUnit location");
        p.setAddress("Any JUnit place");
        p.setStart(true);
        p.getLocation().setLatitude(loc.getLatitude());
        p.getLocation().setLongitude(loc.getLongitude());
        return p;
    }
}
