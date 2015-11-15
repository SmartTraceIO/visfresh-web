/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleDaoTest
    extends BaseCrudTest<NotificationScheduleDao, NotificationSchedule, Long> {

    private User user;
    /**
     * Default constructor.
     */
    public NotificationScheduleDaoTest() {
        super(NotificationScheduleDao.class);
    }

    @Before
    public void setUp() {
        this.user = createUser();
    }

    public User createUser() {
        final User u = new User();
        u.setCompany(this.sharedCompany);
        u.setEmail("asuvorov@mail.ru");
        u.setFirstName("Alexander");
        u.setLastName("Suvorov");
        u.setLogin("asuvorov");
        u.setPhone("11111111117");
        u.setTemperatureUnits(TemperatureUnits.Celsius);
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
        getContext().getBean(UserDao.class).save(u);
        return u;
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
        ps.setUser(user);
        ps.setFromTime(45);
        ps.setPushToMobileApp(true);
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

        assertEquals(45, ps.getFromTime());
        assertTrue(ps.isPushToMobileApp());
        assertEquals(150, ps.getToTime());
        assertNotNull(ps.getUser());
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

        assertEquals(45, ps.getFromTime());
        assertTrue(ps.isPushToMobileApp());
        assertEquals(150, ps.getToTime());
        assertNotNull(ps.getUser());
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

    @After
    public void tearDown() {
        this.getContext().getBean(UserDao.class).deleteAll();
    }
}
