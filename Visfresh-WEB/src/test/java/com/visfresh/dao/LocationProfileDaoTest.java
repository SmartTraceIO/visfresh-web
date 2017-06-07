/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProfileDaoTest extends BaseCrudTest<LocationProfileDao, LocationProfile, LocationProfile, Long> {
    private Device device;
    /**
     * Default constructor.
     */
    public LocationProfileDaoTest() {
        super(LocationProfileDao.class);
    }

    @Before
    public void setUp() {
        //create devcie.
        device = new Device();
        device.setName("JUnit device");
        device.setCompany(sharedCompany);
        device.setImei("0891237987987987");
        device.setDescription("Test device");
        device = getContext().getBean(DeviceDao.class).save(device);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected LocationProfile createTestEntity() {
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

        return p;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final LocationProfile lp) {
        assertEquals("Odessa city, Deribasovskaya st. 1, apt. 1", lp.getAddress());
        assertEquals(sharedCompany.getId(), lp.getCompany().getId());
        assertTrue(lp.isInterim());
        assertEquals("Test location", lp.getName());
        assertEquals("Any notes", lp.getNotes());
        assertEquals(700, lp.getRadius());
        assertTrue(lp.isStart());
        assertTrue(lp.isStop());

        assertNotNull(lp.getLocation());
        assertEquals(100.200, lp.getLocation().getLatitude(), 0.000001);
        assertEquals(300.400, lp.getLocation().getLongitude(), 0.000001);
    }
    @Test
    public void testFindByCompany() {
        createAndSaveLocationProfile(sharedCompany);
        createAndSaveLocationProfile(sharedCompany);

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }
    @Test
    public void testGetOwnerShipments() {
        //test null location
        assertEquals(0, dao.getOwnerShipments(null).size());

        createAutoStartShipment();
        final Shipment s2 = createAutoStartShipment();
        final Shipment s3 = createAutoStartShipment();

        final LocationProfile loc = createTestEntity();
        dao.save(loc);

        s2.setShippedFrom(loc);
        s3.setShippedTo(loc);
        getContext().getBean(ShipmentDao.class).save(Arrays.asList(s2, s3));

        assertEquals(2, dao.getOwnerShipments(loc).size());
    }
    /**
     * @return
     */
    private Shipment createAutoStartShipment() {
        final Shipment s = new Shipment();
        s.setCompany(sharedCompany);
        s.setDevice(device);
        this.getContext().getBean(ShipmentDao.class).save(s);
        return s;
    }
    /**
     * @param c
     */
    private LocationProfile createAndSaveLocationProfile(final Company c) {
        final LocationProfile a = createTestEntity();
        a.setCompany(c);
        return dao.save(a);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<LocationProfile> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final LocationProfile lp = all.get(0);
        assertEquals("Odessa city, Deribasovskaya st. 1, apt. 1", lp.getAddress());
        assertEquals(sharedCompany.getId(), lp.getCompany().getId());
        assertTrue(lp.isInterim());
        assertEquals("Test location", lp.getName());
        assertEquals("Any notes", lp.getNotes());
        assertEquals(700, lp.getRadius());
        assertTrue(lp.isStart());
        assertTrue(lp.isStop());

        assertNotNull(lp.getLocation());
        assertEquals(100.200, lp.getLocation().getLatitude(), 0.000001);
        assertEquals(300.400, lp.getLocation().getLongitude(), 0.000001);
    }
}
