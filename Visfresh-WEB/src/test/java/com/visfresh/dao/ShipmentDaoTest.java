/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;

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
    private TrackerEventDao trackerEventDao;
    private ShipmentTemplateDao shipmentTemplateDao;

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
        trackerEventDao = getContext().getBean(TrackerEventDao.class);
        shipmentTemplateDao = getContext().getBean(ShipmentTemplateDao.class);

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
        ap.setWatchMovementStart(true);
        ap.setWatchMovementStop(true);
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

        PersonSchedule ps = new PersonSchedule();
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

        ps = new PersonSchedule();
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

        device = createDevice("3984709382475");
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
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Shipment createTestEntity() {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setCompany(sharedCompany);
        s.getCustomFields().put("field1", "Custom field 1");
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setName("Shipment-1");
        s.setPalletId("PalletID");
        s.setAssetNum("PoNum");
        s.setPoNum(329487);
        s.setTripCount(876);
        s.setShipmentDescription("Test Shipment");
        s.setShipmentDate(new Date());
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
        assertEquals(device.getId(), s.getDevice().getId());
        assertNotNull(s.getAlertProfile());
        assertEquals(5, s.getAlertSuppressionMinutes());
        assertEquals(17, s.getArrivalNotificationWithinKm());
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Custom field 1", s.getCustomFields().get("field1"));
        assertEquals(true, s.isExcludeNotificationsIfNoAlerts());
        assertEquals("Shipment-1", s.getName());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getAssetNum());
        assertEquals(329487, s.getPoNum());
        assertTrue(s.getTripCount() > 0);
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertNotNull(s.getShipmentDate());
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

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Shipment> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final Shipment s = all.get(0);
        assertEquals(device.getId(), s.getDevice().getId());
        assertNotNull(s.getAlertProfile());
        assertEquals(5, s.getAlertSuppressionMinutes());
        assertEquals(17, s.getArrivalNotificationWithinKm());
        assertEquals(sharedCompany.getId(), s.getCompany().getId());
        assertEquals("Custom field 1", s.getCustomFields().get("field1"));
        assertEquals(true, s.isExcludeNotificationsIfNoAlerts());
        assertEquals("Shipment-1", s.getName());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getAssetNum());
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertTrue(s.getTripCount() > 0);
        assertNotNull(s.getShipmentDate());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(70, s.getShutdownDeviceTimeOut());
        assertEquals(ShipmentStatus.InProgress, s.getStatus());
        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
    }

    @Test
    public void testFindActiveShipment() {
        //test ignores shipment templates
        createShipmentTemplate();
        assertNull(dao.findActiveShipment(device.getImei()));

        //check found active
        final Shipment s = createTestEntity();
        s.setStatus(ShipmentStatus.Complete);
        dao.save(s);

        //check ignores in complete state
        assertNull(dao.findActiveShipment(device.getImei()));

        s.setStatus(ShipmentStatus.InProgress);
        dao.save(s);
        assertNotNull(dao.findActiveShipment(device.getImei()));

        //check ignores other devices
        final Device d = createDevice("2340982349");
        s.setDevice(d);
        dao.save(s);
        assertNull(dao.findActiveShipment(device.getImei()));
    }
    @Test
    public void testSaveDefaultShipment() {
        final Shipment s = new Shipment();
        s.setName("Default profile");
        s.setCompany(sharedCompany);
        s.setDevice(device);
        dao.save(s);

        assertNotNull(dao.findOne(s.getId()));
    }
    @Test
    public void testGetShipmentDeviceInfo() {
        final Shipment s1 = createTestEntity();
        final Shipment s2 = createTestEntity();
        dao.save(s1);
        dao.save(s2);

        assertEquals(1, dao.findOne(s1.getId()).getTripCount());
        assertEquals(2, dao.findOne(s2.getId()).getTripCount());
    }
    @Test
    public void testFindByCompany() {
        dao.save(createTestEntity());
        createShipmentTemplate();

        assertEquals(1, dao.findByCompany(sharedCompany).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left).size());
    }

    public ShipmentTemplate createShipmentTemplate() {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setCompany(sharedCompany);
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setName("Shipment-1");
        s.setShipmentDescription("Test Shipment");
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceTimeOut(70);
        s.getAlertsNotificationSchedules().add(alertNotifSched);
        s.getArrivalNotificationSchedules().add(arrivalSched);
        s.setAddDateShipped(true);
        s.setDetectLocationForShippedFrom(true);
        s.setUseCurrentTimeForDateShipped(true);
        return shipmentTemplateDao.save(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        trackerEventDao.deleteAll();
        super.clear();
        shipmentTemplateDao.deleteAll();
        alertProfileDao.deleteAll();
        locationProfileDao.deleteAll();
        notificationScheduleDao.deleteAll();
        deviceDao.deleteAll();
    }
}
