/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationsDataBuilderTest extends BaseBuilderTest {
    private LocationProfileDao dao;

    /**
     * Default constructor.
     */
    public LocationsDataBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        dao = context.getBean(LocationProfileDao.class);
    }

    @Test
    public void testLocationFrom() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        s.setShippedFrom(loc);
        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        sib1.setShippedFrom(loc);
        shipmentDao.save(sib1);

        final Shipment sib2 = createShipment();
        sib2.setShippedFrom(createLocation("Sibling location"));
        shipmentDao.save(sib2);

        setAsSiblings(s, sib1, sib2);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib1, sib2);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getStartLocation());
        assertEqualsLocations(sib1.getShippedFrom(), SingleShipmentTestUtils.getSibling(
                sib1.getId(), data).getStartLocation());
        assertEqualsLocations(sib2.getShippedFrom(), SingleShipmentTestUtils.getSibling(
                sib2.getId(), data).getStartLocation());
    }

    @Test
    public void testLeftLocationFrom() {

    }
    @Test
    public void testLocationTo() {

    }
    @Test
    public void testLeftLocationTo() {

    }
    @Test
    public void testAlternativeLocations() {

    }
    @Test
    public void testInterimStops() {

    }
    private LocationProfile createLocation(final String name) {
        final LocationProfile p = new LocationProfile();
        p.setAddress("Odessa city, Deribasovskaya st. 1, apt. 1");
        p.setCompany(sharedCompany);
        p.setInterim(true);
        p.setName("Test location");
        p.setNotes("Any notes");
        p.setRadius(700);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.200);
        p.getLocation().setLongitude(300.400);
        return dao.save(p);
    }
    /**
     * @return shipment.
     */
    private Shipment createShipment() {
        return shipmentDao.save(createDefaultNotSavedShipment(device));
    }
    /**
     * @param loc
     * @param b
     */
    private void assertEqualsLocations(final LocationProfile loc, final LocationProfileBean b) {
        assertEquals("ID", loc.getId(), b.getId());
        assertEquals("Address", loc.getAddress(), b.getAddress());
        assertEquals("Company name", loc.getCompanyName(), b.getCompanyName());
        assertEquals("Latitude", loc.getLocation().getLatitude(), b.getLocation().getLatitude(), 0.001);
        assertEquals("Longitude", loc.getLocation().getLongitude(), b.getLocation().getLongitude(), 0.001);
        assertEquals("Name", loc.getName(), b.getName());
        assertEquals("Notes", loc.getNotes(), b.getNotes());
        assertEquals("Radius", loc.getRadius(), b.getRadius());
    }
}
