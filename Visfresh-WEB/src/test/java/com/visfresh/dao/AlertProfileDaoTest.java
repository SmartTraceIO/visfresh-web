/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.AlertProfile;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileDaoTest extends BaseCrudTest<AlertProfileDao, AlertProfile, Long> {
    /**
     * Default constructor.
     */
    public AlertProfileDaoTest() {
        super(AlertProfileDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected AlertProfile createTestEntity() {
        final AlertProfile p = new AlertProfile();
        p.setCompany(sharedCompany);
        p.setCriticalHighTemperature(10);
        p.setCriticalHighTemperatureForMoreThen(10);
        p.setCriticalLowTemperature(-20);
        p.setDescription("JUnit test alert pforile");
        p.setHighTemperature(5);
        p.setHighTemperatureForMoreThen(10);
        p.setLowTemperature(-10);
        p.setLowTemperatureForMoreThen(7);
        p.setName("JUnit-Alert");
        p.setWatchBatteryLow(true);
        p.setWatchShock(true);
        p.setWatchEnterDarkEnvironment(true);

        return p;
    }
}
