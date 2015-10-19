/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProfileDaoTest extends BaseCrudTest<LocationProfileDao, LocationProfile, Long> {
    /**
     * Default constructor.
     */
    public LocationProfileDaoTest() {
        super(LocationProfileDao.class);
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
