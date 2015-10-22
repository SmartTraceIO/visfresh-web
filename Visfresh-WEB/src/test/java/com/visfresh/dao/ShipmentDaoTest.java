/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;

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
        d.setId(d.getImei() + ".1234");
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
        s.getDevices().add(device);
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionDuringCoolDown(5);
        s.setArrivalNotificationWithIn(17);
        s.setCompany(sharedCompany);
        s.setCustomFields("Custom fields");
        s.setExcludeNotificationsIfNoAlertsFired(true);
        s.setName("Shipment-1");
        s.setPalletId("PalletID");
        s.setAssetNum("PoNum");
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
        assertEquals("PoNum", s.getAssetNum());
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
        assertEquals("PoNum", s.getAssetNum());
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
        final Device d = createDevice("234980098");

        Shipment s = createTestEntity();
        s.getDevices().clear();
        s.getDevices().add(d);
        s = dao.save(s);

        createTrackerEvent(d, "INIT");

        //Test onlyWithAlerts flag
        List<ShipmentData> data = dao.getShipmentData(sharedCompany,
                new Date(System.currentTimeMillis() - 10000000l),
                new Date(System.currentTimeMillis() + 100000L), true);
        assertEquals(0, data.size());

        data = dao.getShipmentData(sharedCompany,
                new Date(System.currentTimeMillis() - 10000000l),
                new Date(System.currentTimeMillis() + 100000L), false);
        assertEquals(1, data.size());

        createAlert(d, AlertType.CriticalHighTemperature);
        data = dao.getShipmentData(sharedCompany,
                new Date(System.currentTimeMillis() - 10000000l),
                new Date(System.currentTimeMillis() + 100000L), false);
        assertEquals(1, data.size());

        //assert check time interval
        data = dao.getShipmentData(sharedCompany,
                new Date(System.currentTimeMillis() - 10000000l),
                new Date(System.currentTimeMillis() - 100000L), false);
        assertEquals(0, data.size());
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
        s.getDevices().clear();
        final Device d = createDevice("2340982349");
        s.getDevices().add(d);
        dao.save(s);
        assertNull(dao.findActiveShipment(device.getImei()));
    }

    @Test
    public void testGetShipmentDataForLeftCompany() {
        final Company c = createCompany("Left company");

        final Device d = createDevice("234980098");
        d.setCompany(c);
        deviceDao.save(d);

        Shipment s = createTestEntity();
        s.setCompany(c);
        s.getDevices().clear();
        s.getDevices().add(d);
        s = dao.save(s);

        createTrackerEvent(d, "INIT");
        createAlert(d, AlertType.CriticalHighTemperature);

        //Test onlyWithAlerts flag
        final List<ShipmentData> data = dao.getShipmentData(sharedCompany,
                new Date(System.currentTimeMillis() - 10000000l),
                new Date(System.currentTimeMillis() + 100000L), false);
        assertEquals(0, data.size());
    }
    @Test
    public void testSaveDefaultShipment() {
        final Shipment s = new Shipment();
        s.setName("Default profile");
        s.setCompany(sharedCompany);
        s.getDevices().add(device);
        dao.save(s);

        assertNotNull(dao.findOne(s.getId()));
    }
    @Test
    public void testGetShipmentDeviceInfo() {
        final Shipment s1 = createTestEntity();
        final Shipment s2 = createTestEntity();
        dao.save(s1);
        dao.save(s2);

        assertEquals(1, dao.getShipmentDeviceInfo(s1, s1.getDevices().get(0)).getTripCount());
        assertEquals(2, dao.getShipmentDeviceInfo(s2, s2.getDevices().get(0)).getTripCount());
    }

    public ShipmentTemplate createShipmentTemplate() {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionDuringCoolDown(5);
        s.setArrivalNotificationWithIn(17);
        s.setCompany(sharedCompany);
        s.setExcludeNotificationsIfNoAlertsFired(true);
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
    /**
     * @param d device.
     * @param t event type.
     * @return tracker event.
     */
    private TrackerEvent createTrackerEvent(final Device d, final String t) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(d);
        e.setTemperature(5.5);
        e.setTime(new Date());
        e.setType(t);
        return trackerEventDao.save(e);
    }
    /**
     * @param d
     * @param type
     * @return
     */
    private Alert createAlert(final Device d, final AlertType type) {
        Alert a;
        switch (type) {
            case CriticalHighTemperature:
            case CriticalLowTemperature:
            case HighTemperature:
            case LowTemperature:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(10.10);
                ta.setMinutes(20);
                a = ta;
                break;
            default:
                a = new Alert();
        }

        a.setName("Test Alert - " + type);
        a.setType(type);
        a.setDescription("Test aleret");
        a.setDate(new Date());
        a.setDevice(d);
        return a;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        shipmentTemplateDao.deleteAll();
        trackerEventDao.deleteAll();
        alertProfileDao.deleteAll();
        locationProfileDao.deleteAll();
        notificationScheduleDao.deleteAll();
        deviceDao.deleteAll();
    }
}
