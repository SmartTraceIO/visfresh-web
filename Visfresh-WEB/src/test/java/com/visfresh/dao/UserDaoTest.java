/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.TimeZone;

import org.junit.Before;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserDaoTest extends BaseCrudTest<UserDao, User, String> {
    private int ids;
    private ShipmentDao shipmentDao;
    private DeviceDao deviceDao;
    private LocationProfileDao locationProfileDao;
    private AlertProfileDao alertProfileDao;

    /**
     * Default constructor.
     */
    public UserDaoTest() {
        super(UserDao.class);
    }

    @Before
    public void beforeTest() {
        shipmentDao = getContext().getBean(ShipmentDao.class);
        deviceDao = getContext().getBean(DeviceDao.class);
        locationProfileDao = getContext().getBean(LocationProfileDao.class);
        alertProfileDao = getContext().getBean(AlertProfileDao.class);
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
        u.setEmail("suvorov@mail.ru");
        u.setCompany(sharedCompany);
        u.setLogin("asuvorov-" + (++ids));
        u.setPassword("abrakadabra");
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
        u.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        return u;
    }
    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setSn("456");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return deviceDao.save(d);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final User user) {
        assertEquals("asuvorov-1", user.getLogin());
        assertEquals("Alexande", user.getFirstName());
        assertEquals("Suvorov", user.getLastName());
        assertEquals("Manager", user.getPosition());
        assertEquals("1111111117", user.getPhone());
        assertEquals("suvorov@mail.ru", user.getEmail());
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());
        assertEquals(TemperatureUnits.Fahrenheit, user.getTemperatureUnits());

        //test company
        final Company c = user.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
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

        assertNotNull(user.getLogin());
        assertEquals("Alexande", user.getFirstName());
        assertEquals("Suvorov", user.getLastName());
        assertEquals("Manager", user.getPosition());
        assertEquals("1111111117", user.getPhone());
        assertEquals("suvorov@mail.ru", user.getEmail());
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());
        assertEquals(TemperatureUnits.Fahrenheit, user.getTemperatureUnits());

        //test company
        final Company c = user.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();

        shipmentDao.deleteAll();
        deviceDao.deleteAll();
        locationProfileDao.deleteAll();
        alertProfileDao.deleteAll();
    }
}
