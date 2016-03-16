/**
 *
 */
package com.visfresh.mpl.services.arrival;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultArrivalEstimationCalculatorTest extends DefaultArrivalEstimationCalculator {

    /**
     * Default constructor.
     */
    public DefaultArrivalEstimationCalculatorTest() {
        super();
    }

    @Test
    public void testSomeLocation() {
        final Location loc = new Location(10, 20);
        final Shipment s = createShipment(loc, loc);

        final Date currentDate = new Date(System.currentTimeMillis() - 1000000L);

        final Date est = estimateArrivalDate(s, loc,
                new Date(currentDate.getTime() - 10000), currentDate);
        assertEquals(currentDate, est);
    }

    @Test
    public void testEstimateArrivalDate() {
        final Location loc1 = new Location(-33.75471658008509, 150.67508697509766);
        final Location loc2 = new Location(-33.75471658008509, 150.67508697509766);
        final Location currentLoc = new Location(-33.0, 151.0);

        final Shipment s = createShipment(loc1, loc2);
        final Date startDate = new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000l);
        s.setShipmentDate(startDate);

        final Date currentDate = new Date(System.currentTimeMillis() - 100000L);

        final Date est = estimateArrivalDate(s, currentLoc, startDate, currentDate);
        assertTrue(startDate.before(est));
    }
    @Test
    //the dates have been got from estimation bug
    public void testEstimationBug() throws ParseException {
        final Date shipmentDate = parseDate("2016-01-16 18:46:00");

        final Location currentLocation = new Location(-33.8885, 151.1625);
        final Date currentTime = parseDate("2016-01-17 06:26:00");

        final Shipment s = new Shipment();
        s.setShipmentDate(shipmentDate);
        s.setShippedFrom(createLocation(-33.886327594596395, 151.16012692451477));
        s.setShippedTo(createLocation(-37.704684495316705, 145.162353515625));

        final Date eta = estimateArrivalDate(
                s, currentLocation, shipmentDate, currentTime);

        assertTrue(eta.after(parseDate("2016-01-16 18:46:00")));
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

    private static Date parseDate(final String dateStr) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
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
