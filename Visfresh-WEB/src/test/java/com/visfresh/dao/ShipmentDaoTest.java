/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentDaoTest extends BaseCrudTest<ShipmentDao, Shipment, Long> {
    private AlertProfile alertProfile;
    private LocationProfile shippedFrom;
    private LocationProfile shippedTo;
    private NotificationSchedule alertNotifSched;
    private NotificationSchedule arrivalSched;

    private AlertProfileDao alertProfileDao;
    private LocationProfileDao locationProfileDao;
    private NotificationScheduleDao notificationScheduleDao;
    private Device device;
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public ShipmentDaoTest() {
        super(ShipmentDao.class);
    }

    @Before
    public void beforeTest() {
        //create alert profile
        alertProfileDao = getContext().getBean(AlertProfileDao.class);

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
        alertProfile = alertProfileDao.save(ap);

        //create shipped from location
        locationProfileDao = getContext().getBean(LocationProfileDao.class);
        LocationProfile p = new LocationProfile();

        p.setAddress("Odessa city, Deribasovskaya st. 1, apt. 1");
        p.setCompany(sharedCompany);
        p.setInterim(true);
        p.setName("Test location 1");
        p.setNotes("Any notes");
        p.setRadius(700);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.200);
        p.getLocation().setLongitude(300.400);

        shippedFrom = locationProfileDao.save(p);

        p = new LocationProfile();

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
        shippedTo = locationProfileDao.save(p);

        //notification schedules
        notificationScheduleDao = getContext().getBean(NotificationScheduleDao.class);

        NotificationSchedule s = new NotificationSchedule();
        s.setCompany(sharedCompany);
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        PersonalSchedule ps = new PersonalSchedule();
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

        alertNotifSched = notificationScheduleDao.save(s);

        s = new NotificationSchedule();
        s.setCompany(sharedCompany);
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        ps = new PersonalSchedule();
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

        arrivalSched = notificationScheduleDao.save(s);

        //device
        deviceDao = getContext().getBean(DeviceDao.class);

        final Device d = new Device();
        d.setName("Test Device");
        d.setImei("3984709382475");
        d.setId(d.getImei() + ".1234");
        d.setSn("456");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        device = deviceDao.save(d);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Shipment createTestEntity() {
        final Shipment s = new Shipment();
        s.getDevices().add(device);
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionDuringCoolDown(5);
        s.setArrivalNotificationWithIn(17);
        s.setCompany(sharedCompany);
        s.setCustomFields("Custom fields");
        s.setExcludeNotificationsIfNoAlertsFired(true);
        s.setName("Shipment-1");
        s.setPalletId("PalletID");
        s.setPoNum("PoNum");
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDescriptionDate(new Date());
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceTimeOut(70);
        s.setStatus(ShipmentStatus.InProgress);
        s.getAlertsNotificationSchedules().add(alertNotifSched);
        s.getArrivalNotificationSchedules().add(arrivalSched);
        return s;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Shipment s) {
        assertEquals(1, s.getDevices().size());
        assertNotNull(s.getAlertProfile());
        assertEquals(5, s.getAlertSuppressionDuringCoolDown());
        assertEquals(17, s.getArrivalNotificationWithIn());
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Custom fields", s.getCustomFields());
        assertEquals(true, s.isExcludeNotificationsIfNoAlertsFired());
        assertEquals("Shipment-1", s.getName());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getPoNum());
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertNotNull(s.getShipmentDescriptionDate());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(70, s.getShutdownDeviceTimeOut());
        assertEquals(ShipmentStatus.InProgress, s.getStatus());
        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
    }

    @Test
    public void testDeleteSchedules() {
        Shipment s = createTestEntity();
        s.getArrivalNotificationSchedules().add(alertNotifSched);
        s.getAlertsNotificationSchedules().add(arrivalSched);

        dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(2, s.getAlertsNotificationSchedules().size());
        assertEquals(2, s.getArrivalNotificationSchedules().size());

        //attempt to remove schedules
        s.getArrivalNotificationSchedules().remove(0);
        s.getAlertsNotificationSchedules().remove(0);
        s = dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
    }
    @Test
    public void testDeleteRefsExternally() {
        Shipment s = createTestEntity();
        dao.save(s);

        s = dao.findOne(s.getId());
        notificationScheduleDao.delete(s.getAlertsNotificationSchedules().get(0));
        notificationScheduleDao.delete(s.getArrivalNotificationSchedules().get(0));
        deviceDao.delete(s.getDevices().get(0));
        s = dao.findOne(s.getId());

        assertEquals(0, s.getAlertsNotificationSchedules().size());
        assertEquals(0, s.getArrivalNotificationSchedules().size());
        assertEquals(0, s.getDevices().size());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Shipment> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final Shipment s = all.get(0);
        assertEquals(1, s.getDevices().size());
        assertNotNull(s.getAlertProfile());
        assertEquals(5, s.getAlertSuppressionDuringCoolDown());
        assertEquals(17, s.getArrivalNotificationWithIn());
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Custom fields", s.getCustomFields());
        assertEquals(true, s.isExcludeNotificationsIfNoAlertsFired());
        assertEquals("Shipment-1", s.getName());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getPoNum());
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertNotNull(s.getShipmentDescriptionDate());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(70, s.getShutdownDeviceTimeOut());
        assertEquals(ShipmentStatus.InProgress, s.getStatus());
        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
    }

    @Test
    public void testGetShipmentData() {

    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();

        alertProfileDao.deleteAll();
        locationProfileDao.deleteAll();
        notificationScheduleDao.deleteAll();
        deviceDao.deleteAll();
    }
}
