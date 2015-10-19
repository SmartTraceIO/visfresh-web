/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
        p.setCriticalHighTemperature(10.10);
        p.setCriticalHighTemperatureForMoreThen(10);
        p.setCriticalLowTemperature(-20.20);
        p.setDescription("JUnit test alert pforile");
        p.setHighTemperature(5.45);
        p.setHighTemperatureForMoreThen(10);
        p.setLowTemperature(-10.45);
        p.setLowTemperatureForMoreThen(7);
        p.setName("JUnit-Alert");
        p.setWatchBatteryLow(true);
        p.setWatchShock(true);
        p.setWatchEnterDarkEnvironment(true);

        return p;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final AlertProfile p) {
        assertEquals(sharedCompany.getId(), p.getCompany().getId());
        assertEquals(10.10, p.getCriticalHighTemperature(), 0.00001);
        assertEquals(10, p.getCriticalHighTemperatureForMoreThen());
        assertEquals(-20.20, p.getCriticalLowTemperature(), 0.00001);
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals(5.45, p.getHighTemperature(), 0.00001);
        assertEquals(10, p.getHighTemperatureForMoreThen());
        assertEquals(-10.45, p.getLowTemperature(), 0.0001);
        assertEquals(7, p.getLowTemperatureForMoreThen());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchShock());
        assertTrue(p.isWatchEnterDarkEnvironment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<AlertProfile> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final AlertProfile p = all.get(0);

        assertEquals(sharedCompany.getId(), p.getCompany().getId());
        assertEquals(10.10, p.getCriticalHighTemperature(), 0.00001);
        assertEquals(10, p.getCriticalHighTemperatureForMoreThen());
        assertEquals(-20.20, p.getCriticalLowTemperature(), 0.00001);
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals(5.45, p.getHighTemperature(), 0.00001);
        assertEquals(10, p.getHighTemperatureForMoreThen());
        assertEquals(-10.45, p.getLowTemperature(), 0.0001);
        assertEquals(7, p.getLowTemperatureForMoreThen());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchShock());
        assertTrue(p.isWatchEnterDarkEnvironment());
    }
}
