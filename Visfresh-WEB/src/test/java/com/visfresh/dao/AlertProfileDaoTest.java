/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.TemperatureIssue;

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
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(c);
        ap.setDescription("JUnit test alert pforile");
        ap.setName("JUnit-Alert");

        final int normalTemperature = 3;
        TemperatureIssue criticalHot = new TemperatureIssue(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 15);
        criticalHot.setTimeOutMinutes(0);
        ap.getTemperatureIssues().add(criticalHot);

        criticalHot = new TemperatureIssue(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 14);
        criticalHot.setTimeOutMinutes(1);
        ap.getTemperatureIssues().add(criticalHot);

        TemperatureIssue criticalLow = new TemperatureIssue(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -15.);
        criticalLow.setTimeOutMinutes(0);
        ap.getTemperatureIssues().add(criticalLow);

        criticalLow = new TemperatureIssue(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -14.);
        criticalLow.setTimeOutMinutes(1);
        ap.getTemperatureIssues().add(criticalLow);

        TemperatureIssue hot = new TemperatureIssue(AlertType.Hot);
        hot.setTemperature(normalTemperature + 3);
        hot.setTimeOutMinutes(0);
        ap.getTemperatureIssues().add(hot);

        hot = new TemperatureIssue(AlertType.Hot);
        hot.setTemperature(normalTemperature + 4.);
        hot.setTimeOutMinutes(2);
        ap.getTemperatureIssues().add(hot);

        TemperatureIssue low = new TemperatureIssue(AlertType.Cold);
        low.setTemperature(normalTemperature -10.);
        low.setTimeOutMinutes(40);
        ap.getTemperatureIssues().add(low);

        low = new TemperatureIssue(AlertType.Cold);
        low.setTemperature(normalTemperature-8.);
        low.setTimeOutMinutes(55);
        ap.getTemperatureIssues().add(low);

        ap.setWatchBatteryLow(true);
        ap.setWatchMovementStart(true);
        ap.setWatchMovementStop(true);
        ap.setWatchEnterDarkEnvironment(true);

        return ap;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final AlertProfile p) {
        assertEquals(sharedCompany.getId(), p.getCompany().getId());
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchMovementStart());
        assertTrue(p.isWatchMovementStop());
        assertTrue(p.isWatchEnterDarkEnvironment());

        assertEquals(8, p.getTemperatureIssues().size());
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
        assertEquals("JUnit test alert pforile", p.getDescription());
        assertEquals("JUnit-Alert", p.getName());
        assertTrue(p.isWatchBatteryLow());
        assertTrue(p.isWatchMovementStart());
        assertTrue(p.isWatchMovementStop());
        assertTrue(p.isWatchEnterDarkEnvironment());

        assertEquals(8, p.getTemperatureIssues().size());
        //test ID not null
        assertNotNull(p.getTemperatureIssues().get(0).getId());
    }
    @Test
    public void testSaveTemperatureIssue() {
        AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany);
        ap.setDescription("JUnit test alert pforile");
        ap.setName("JUnit-Alert");

        final TemperatureIssue expected = new TemperatureIssue(AlertType.CriticalHot);
        expected.setTemperature(15);
        expected.setTimeOutMinutes(0);
        ap.getTemperatureIssues().add(expected);

        dao.save(ap);
        ap = dao.findOne(ap.getId());

        final TemperatureIssue actual = ap.getTemperatureIssues().get(0);
        assertEquals(expected.getTemperature(), actual.getTemperature(), 0.001);
        assertEquals(expected.getTimeOutMinutes(), actual.getTimeOutMinutes());
        assertEquals(expected.getType(), actual.getType());
    }
}
