/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleDaoTest
    extends BaseCrudTest<NotificationScheduleDao, NotificationSchedule, Long> {

    /**
     * Default constructor.
     */
    public NotificationScheduleDaoTest() {
        super(NotificationScheduleDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected NotificationSchedule createTestEntity() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setCompany(sharedCompany);
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        final PersonSchedule ps = new PersonSchedule();
        ps.setCompany("Any Company");
        ps.setEmailNotification("asuvoror");
        ps.setFirstName("First");
        ps.setFromTime(45);
        ps.setLastName("Last");
        ps.setPosition("Manager");
        ps.setPushToMobileApp(true);
        ps.setSmsNotification("11111111118");
        ps.setToTime(150);

        s.getSchedules().add(ps);
        return s;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final NotificationSchedule s) {
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Schd-Test", s.getName());
        assertEquals("Test schedule", s.getDescription());

        assertEquals(1, s.getSchedules().size());

        final PersonSchedule ps = s.getSchedules().get(0);

        assertEquals("Any Company", ps.getCompany());
        assertEquals("asuvoror", ps.getEmailNotification());
        assertEquals("First", ps.getFirstName());
        assertEquals(45, ps.getFromTime());
        assertEquals("Last", ps.getLastName());
        assertEquals("Manager", ps.getPosition());
        assertTrue(ps.isPushToMobileApp());
        assertEquals("11111111118", ps.getSmsNotification());
        assertEquals(150, ps.getToTime());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<NotificationSchedule> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final NotificationSchedule s = all.get(0);
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Schd-Test", s.getName());
        assertEquals("Test schedule", s.getDescription());

        assertEquals(1, s.getSchedules().size());

        final PersonSchedule ps = s.getSchedules().get(0);

        assertEquals("Any Company", ps.getCompany());
        assertEquals("asuvoror", ps.getEmailNotification());
        assertEquals("First", ps.getFirstName());
        assertEquals(45, ps.getFromTime());
        assertEquals("Last", ps.getLastName());
        assertEquals("Manager", ps.getPosition());
        assertTrue(ps.isPushToMobileApp());
        assertEquals("11111111118", ps.getSmsNotification());
        assertEquals(150, ps.getToTime());
    }
    @Test
    public void testFindByCompany() {
        dao.save(createTestEntity());
        dao.save(createTestEntity());

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }
}
