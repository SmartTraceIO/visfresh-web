/**
 *
 */
package com.visfresh.dao;

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
}
