/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;

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

    public void testGetProfile() {
        final User user = createTestEntity();
        dao.save(user);

        assertNull(dao.getProfile(user));

        dao.saveProfile(user, new UserProfile());
        assertNotNull(dao.getProfile(user));
    }
    public void testProfileShipments() {
        final User user = createTestEntity();
        dao.save(user);

        dao.saveProfile(user, new UserProfile());
        UserProfile p = dao.getProfile(user);

        p.getShipments().add(createShipment());
        p.getShipments().add(createShipment());
        dao.saveProfile(user, p);

        p = dao.getProfile(user);
        assertEquals(2, p.getShipments());

        p.getShipments().remove(0);
        dao.saveProfile(user, p);
        assertEquals(1, p.getShipments());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected User createTestEntity() {
        final User u = new User();
        u.setFullName("Alexander Suvorov");
        u.setCompany(sharedCompany);
        u.setLogin("asuvorov-" + (++ids));
        u.setPassword("abrakadabra");
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        d.setId(d.getImei() + ".1234");
        d.setSn("456");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return deviceDao.save(d);
    }
    private LocationProfile createLocationProfile() {
        final LocationProfile p = new LocationProfile();

        p.setAddress("Odessa city, Deribasovskaya st. 1, apt. 2");
        p.setCompany(sharedCompany);
        p.setInterim(true);
        p.setName("Test location 2");
        p.setNotes("Any notes");
        p.setRadius(700);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.200);
        p.getLocation().setLongitude(300.400);
        return locationProfileDao.save(p);
    }
    private AlertProfile createAlertProfile() {
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany);
        ap.setCriticalHighTemperature(10);
        ap.setCriticalHighTemperatureForMoreThen(10);
        ap.setCriticalLowTemperature(-20);
        ap.setDescription("JUnit test alert pforile");
        ap.setHighTemperature(5);
        ap.setHighTemperatureForMoreThen(10);
        ap.setLowTemperature(-10);
        ap.setLowTemperatureForMoreThen(7);
        ap.setName("JUnit-Alert");
        ap.setWatchBatteryLow(true);
        ap.setWatchShock(true);
        ap.setWatchEnterDarkEnvironment(true);
        return alertProfileDao.save(ap);
    }

    private Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setDevice(createDevice("234908042398"));
        s.setAlertProfile(createAlertProfile());
        s.setAlertSuppressionDuringCoolDown(5);
        s.setArrivalNotificationWithIn(17);
        s.setCompany(sharedCompany);
        s.getCustomFields().put("field1", "Custom fields");
        s.setExcludeNotificationsIfNoAlertsFired(true);
        s.setName("Shipment-1");
        s.setPalletId("PalletID");
        s.setAssetNum("PoNum");
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date());
        s.setShippedFrom(createLocationProfile());
        s.setShippedTo(createLocationProfile());
        s.setShutdownDeviceTimeOut(70);
        s.setStatus(ShipmentStatus.InProgress);
        return shipmentDao.save(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final User user) {
        assertEquals("asuvorov-1", user.getLogin());
        assertEquals("Alexander Suvorov", user.getFullName());
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());

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
        assertEquals("Alexander Suvorov", user.getFullName());
        assertEquals("abrakadabra", user.getPassword());
        assertEquals(TimeZone.getTimeZone("UTC"), user.getTimeZone());

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
