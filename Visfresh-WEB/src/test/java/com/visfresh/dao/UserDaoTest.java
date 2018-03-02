/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserDaoTest extends BaseCrudTest<UserDao, User, User, Long> {
    private int ids;
    private NotificationSchedule notificationSchedule;
    private Alert alert;

    /**
     * Default constructor.
     */
    public UserDaoTest() {
        super(UserDao.class);
    }

    @Before
    public void beforeTest() {
        final NotificationSchedule ns = new NotificationSchedule();
        ns.setCompany(sharedCompany.getCompanyId());
        ns.setDescription("Test");
        ns.setName("JUnit");
        notificationSchedule = getContext().getBean(NotificationScheduleDao.class).save(ns);

        Device d = new Device();
        d.setName("Test Device");
        d.setImei("2938479898989834");
        d.setCompany(sharedCompany.getCompanyId());
        d.setDescription("Test device");
        d = getContext().getBean(DeviceDao.class).save(d);

        Shipment s = new Shipment();
        s.setCompany(sharedCompany.getCompanyId());
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(d);
        s.setShipmentDescription("Created by autostart shipment rule");
        s = getContext().getBean(ShipmentDao.class).save(s);

        //create alert
        final Alert a = new Alert();
        a.setDevice(d);
        a.setShipment(s);
        a.setType(AlertType.LightOff);
        alert = getContext().getBean(AlertDao.class).save(a);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected User createTestEntity() {
        final User u = new User();
        u.setFirstName("Alexande");
        u.setLastName("Suvorov");
        u.setPosition("Manager");
        u.setPhone("1111111117");
        u.setCompany(sharedCompany.getCompanyId());
        u.setEmail("asuvorov-" + (++ids) + "@google.com");
        u.setPassword("abrakadabra");
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
        u.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        u.setExternalCompany("Mocrosoft");
        u.getSettings().put("key", "value");
        return u;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final User user) {
        assertNotNull(user.getId());
        assertEquals("Alexande", user.getFirstName());
        assertEquals("Suvorov", user.getLastName());
        assertEquals("Manager", user.getPosition());
        assertEquals("1111111117", user.getPhone());
        assertTrue(user.getEmail().endsWith("@google.com"));
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());
        assertEquals(TemperatureUnits.Fahrenheit, user.getTemperatureUnits());
        assertEquals("Mocrosoft", user.getExternalCompany());
        assertEquals("value", user.getSettings().get("key"));

        //test company
        final Long c = user.getCompanyId();
        assertEquals(sharedCompany.getId(), c);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<User> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        //check first entity
        final User user = all.get(0);

        assertNotNull(user.getId());
        assertEquals("Alexande", user.getFirstName());
        assertEquals("Suvorov", user.getLastName());
        assertEquals("Manager", user.getPosition());
        assertEquals("1111111117", user.getPhone());
        assertTrue(user.getEmail().endsWith("@google.com"));
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());
        assertEquals(TemperatureUnits.Fahrenheit, user.getTemperatureUnits());
        assertEquals("Mocrosoft", user.getExternalCompany());
        assertEquals("value", user.getSettings().get("key"));

        //test company
        final Long c = user.getCompanyId();
        assertEquals(sharedCompany.getId(), c);
    }
    @Test
    public void testgetDbReferences() {
        final User u = dao.save(createTestEntity());
        createNotification(u);
        createNotification(u);
        createNotification(u);

        createPersonSchedule(u);
        createPersonSchedule(u);

        assertEquals(5, dao.getDbReferences(u.getId()).size());
    }

    /**
     * @param u user the user.
     * @return notification for given user.
     */
    private Notification createNotification(final User u) {
        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setUser(u);
        n.setIssue(alert);
        return getContext().getBean(NotificationDao.class).save(n);
    }
    /**
     * @param u
     * @return personal schedule.
     */
    private PersonSchedule createPersonSchedule(final User u) {
        final PersonSchedule ps = new PersonSchedule();
        ps.setUser(u);

        notificationSchedule.getSchedules().add(ps);
        getContext().getBean(NotificationScheduleDao.class).save(notificationSchedule);
        return ps;
    }
}
