/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationsDataBuilderTest extends BaseBuilderTest {
    private LocationProfileDao dao;
    private AlternativeLocationsDao altLocDao;
    private InterimStopDao inStopDao;

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
        altLocDao = context.getBean(AlternativeLocationsDao.class);
        inStopDao = context.getBean(InterimStopDao.class);
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
    public void testLeftLocationFromTo() {
        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress("Odessa. Deribasovskaya 1, 1");
        loc.setCompany(s.getCompany());
        loc.setCompanyName("Company name");
        loc.setInterim(true);
        loc.setStart(true);
        loc.setStop(true);
        loc.setName("Location Name");
        loc.setNotes("Location notes");
        loc.setRadius(1500);
        dao.save(loc);

        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        shipmentDao.save(sib1);

        setAsSiblings(s, sib1);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib1);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertNull(data.getBean().getStartLocation());
    }
    @Test
    public void testLocationTo() {
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

        s.setShippedTo(loc);
        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        sib1.setShippedTo(loc);
        shipmentDao.save(sib1);

        final Shipment sib2 = createShipment();
        sib2.setShippedTo(createLocation("Sibling location"));
        shipmentDao.save(sib2);

        setAsSiblings(s, sib1, sib2);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib1, sib2);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getEndLocation());
        assertEqualsLocations(sib1.getShippedTo(), SingleShipmentTestUtils.getSibling(
                sib1.getId(), data).getEndLocation());
        assertEqualsLocations(sib2.getShippedTo(), SingleShipmentTestUtils.getSibling(
                sib2.getId(), data).getEndLocation());
    }
    @Test
    public void testAlternativeLocations() {
        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        //create alternative locations for main shipment
        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(createLocation("From1"));
        sLocs.getFrom().add(createLocation("From2"));
        sLocs.getTo().add(createLocation("To1"));
        sLocs.getTo().add(createLocation("To2"));
        sLocs.getInterim().add(createLocation("Stp1"));
        sLocs.getInterim().add(createLocation("Stp2"));

        altLocDao.save(s, sLocs);

        //create alternative locations for sibling.
        final AlternativeLocations sib1Locs = new AlternativeLocations();
        sib1Locs.getFrom().add(createLocation("SibFrom1"));
        sib1Locs.getTo().add(createLocation("SibTo1"));
        sib1Locs.getInterim().add(createLocation("SibStp1"));

        altLocDao.save(sib1, sib1Locs);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib1, sib2);
        builder.fetchData();
        builder.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getStartLocationAlternatives().size());
        assertEquals(2, data.getBean().getEndLocationAlternatives().size());
        assertEquals(2, data.getBean().getInterimLocationAlternatives().size());

        //first sibling
        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);

        assertEquals(1, sibBean1.getStartLocationAlternatives().size());
        assertEquals(sib1Locs.getFrom().get(0).getId(), sibBean1.getStartLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean1.getEndLocationAlternatives().size());
        assertEquals(sib1Locs.getTo().get(0).getId(), sibBean1.getEndLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean1.getInterimLocationAlternatives().size());
        assertEquals(sib1Locs.getInterim().get(0).getId(), sibBean1.getInterimLocationAlternatives().get(0).getId());

        //test second sibling
        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getStartLocationAlternatives().size());
        assertEquals(0, sibBean2.getEndLocationAlternatives().size());
        assertEquals(0, sibBean2.getInterimLocationAlternatives().size());
    }
    @Test
    public void testAlternativeLocationStart() {
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

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(loc);

        altLocDao.save(s, sLocs);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getStartLocationAlternatives().get(0));
    }
    @Test
    public void testAlternativeLocationEnd() {
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

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getTo().add(loc);

        altLocDao.save(s, sLocs);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getEndLocationAlternatives().get(0));
    }
    @Test
    public void testAlternativeLocationInterim() {
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

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getInterim().add(loc);

        altLocDao.save(s, sLocs);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s);
        builder.fetchData();
        builder.build(ctxt);
        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getInterimLocationAlternatives().get(0));
    }
    @Test
    public void testOneLocationForAllAlternatives() {
        final Shipment s = createShipment();
        final Shipment sib = createShipment();

        setAsSiblings(s, sib);

        final LocationProfile loc = createLocation("From1");

        //create alternative locations for main shipment
        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(loc);
        sLocs.getTo().add(loc);
        sLocs.getInterim().add(loc);

        altLocDao.save(s, sLocs);

        //create alternative locations for sibling.
        final AlternativeLocations sibLocs = new AlternativeLocations();
        sibLocs.getFrom().add(loc);
        sibLocs.getTo().add(loc);
        sibLocs.getInterim().add(loc);

        altLocDao.save(sib, sibLocs);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib);
        builder.fetchData();
        builder.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getStartLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getStartLocationAlternatives().get(0).getId());

        assertEquals(1, data.getBean().getEndLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getEndLocationAlternatives().get(0).getId());

        assertEquals(1, data.getBean().getInterimLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getInterimLocationAlternatives().get(0).getId());

        //first sibling
        final SingleShipmentBean sibBean = SingleShipmentTestUtils.getSibling(sib.getId(), data);

        assertEquals(1, sibBean.getStartLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getStartLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean.getEndLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getEndLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean.getInterimLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getInterimLocationAlternatives().get(0).getId());
    }
    @Test
    public void testInterimStops() {
        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        final LocationProfile loc1 = createLocation("Loc1");
        final LocationProfile loc2 = createLocation("Loc1");

        inStopDao.save(s, createInterimStop(loc1));
        inStopDao.save(s, createInterimStop(loc2));
        inStopDao.save(sib1, createInterimStop(loc2));

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s, sib1, sib2);
        builder.fetchData();
        builder.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getInterimStops().size());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(1, sibBean1.getInterimStops().size());
        assertEquals(loc2.getId(), sibBean1.getInterimStops().get(0).getLocation().getId());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getInterimStops().size());
    }
    @Test
    public void testInterimStopsStop() {
        final Shipment s = createShipment();

        setAsSiblings(s);

        final LocationProfile loc = createLocation("Loc");
        final InterimStop stp = createInterimStop(loc);
        inStopDao.save(s, stp);

        final LocationsDataBuilder builder = new LocationsDataBuilder(jdbc, s.getId());
        final SingleShipmentBuildContext ctxt = SingleShipmentTestUtils.createContextWithMainData(s);
        builder.fetchData();
        builder.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getInterimStops().size());

        //check equals stops
        final InterimStopBean stpBean = data.getBean().getInterimStops().get(0);
        assertEquals(stpBean.getId(), stp.getId());
        assertEquals(stpBean.getTime(), stp.getTime());
        assertTrue(Math.abs(stpBean.getStopDate().getTime() - stp.getDate().getTime()) < 1000l);
        assertEqualsLocations(loc, stpBean.getLocation());
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
     * @param loc location.
     * @return unsaved interim stop.
     */
    private InterimStop createInterimStop(final LocationProfile loc) {
        final InterimStop stp = new InterimStop();
        stp.setDate(new Date());
        stp.setLocation(loc);
        stp.setTime(12);
        return stp;
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
