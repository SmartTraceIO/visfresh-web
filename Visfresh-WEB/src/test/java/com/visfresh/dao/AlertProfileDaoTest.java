/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;

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
        final Company c = sharedCompany;
        return createAlertProfile(c);
    }

    /**
     * @param c
     * @return
     */
    protected AlertProfile createAlertProfile(final Company c) {
        final AlertProfile p = new AlertProfile();
        p.setCompany(c);
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
        p.setWatchMovementStart(true);
        p.setWatchMovementStop(true);
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
        assertEquals(10, p.getCriticalHighTemperatureForMoreThen().intValue());
        assertEquals(-20.20, p.getCriticalLowTemperature(), 0.00001);
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals(5.45, p.getHighTemperature(), 0.00001);
        assertEquals(10, p.getHighTemperatureForMoreThen().intValue());
        assertEquals(-10.45, p.getLowTemperature(), 0.0001);
        assertEquals(7, p.getLowTemperatureForMoreThen().intValue());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchMovementStart());
        assertTrue(p.isWatchMovementStop());
        assertTrue(p.isWatchEnterDarkEnvironment());
    }
    @Test
    public void testFindByCompany() {
        createAndSaveAlertProfile(sharedCompany);
        createAndSaveAlertProfile(sharedCompany);

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }
    @Test
    public void testTemperature2() {
        AlertProfile a = createAlertProfile(sharedCompany);

        final Double criticalHighTemperature2 = 1.;
        final Integer criticalHighTemperatureForMoreThen2 = 2;
        final Double criticalLowTemperature2 = 3.;
        final Integer criticalLowTemperatureForMoreThen2 = 4;
        final Double highTemperature2 = 5.;
        final Integer highTemperatureForMoreThen2 = 6;
        final Double lowTemperature2 = 7.;
        final Integer lowTemperatureForMoreThen2 = 8;

        a.setCriticalHighTemperature2(criticalHighTemperature2);
        a.setCriticalHighTemperatureForMoreThen2(criticalHighTemperatureForMoreThen2);

        a.setCriticalLowTemperature2(criticalLowTemperature2);
        a.setCriticalLowTemperatureForMoreThen2(criticalLowTemperatureForMoreThen2);

        a.setHighTemperature2(highTemperature2);
        a.setHighTemperatureForMoreThen2(highTemperatureForMoreThen2);

        a.setLowTemperature2(lowTemperature2);
        a.setLowTemperatureForMoreThen2(lowTemperatureForMoreThen2);

        final Long id = dao.save(a).getId();
        a = dao.findOne(id);

        assertEquals(criticalHighTemperature2, a.getCriticalHighTemperature2());
        assertEquals(criticalHighTemperatureForMoreThen2, a.getCriticalHighTemperatureForMoreThen2());

        assertEquals(criticalLowTemperature2, a.getCriticalLowTemperature2());
        assertEquals(criticalLowTemperatureForMoreThen2, a.getCriticalLowTemperatureForMoreThen2());

        assertEquals(highTemperature2, a.getHighTemperature2());
        assertEquals(highTemperatureForMoreThen2, a.getHighTemperatureForMoreThen2());

        assertEquals(lowTemperature2, a.getLowTemperature2());
        assertEquals(lowTemperatureForMoreThen2, a.getLowTemperatureForMoreThen2());
    }
    public void testNullValues() {
        AlertProfile p = new AlertProfile();
        p.setCompany(sharedCompany);
        p.setName("Name");
        dao.save(p);

        p = dao.findOne(p.getId());

        assertNull(p.getCriticalHighTemperature());
        assertNull(p.getCriticalHighTemperature2());
        assertNull(p.getCriticalHighTemperatureForMoreThen());
        assertNull(p.getCriticalHighTemperatureForMoreThen2());
        assertNull(p.getCriticalLowTemperature());
        assertNull(p.getCriticalLowTemperature2());
        assertNull(p.getCriticalLowTemperatureForMoreThen());
        assertNull(p.getCriticalLowTemperatureForMoreThen2());
        assertNull(p.getDescription());
        assertNull(p.getHighTemperature());
        assertNull(p.getHighTemperature2());
        assertNull(p.getHighTemperatureForMoreThen());
        assertNull(p.getHighTemperatureForMoreThen2());
    }
    /**
     * @param c
     */
    private AlertProfile createAndSaveAlertProfile(final Company c) {
        return dao.save(createAlertProfile(c));
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
        assertEquals(10, p.getCriticalHighTemperatureForMoreThen().intValue());
        assertEquals(-20.20, p.getCriticalLowTemperature(), 0.00001);
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals(5.45, p.getHighTemperature(), 0.00001);
        assertEquals(10, p.getHighTemperatureForMoreThen().intValue());
        assertEquals(-10.45, p.getLowTemperature(), 0.0001);
        assertEquals(7, p.getLowTemperatureForMoreThen().intValue());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchMovementStart());
        assertTrue(p.isWatchMovementStop());
        assertTrue(p.isWatchEnterDarkEnvironment());
    }
}
