/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.impl.services.ShipmentSiblingInfo;
import com.visfresh.lists.ListResult;
import com.visfresh.lists.ListShipmentItem;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentDaoTest extends BaseCrudTest<ShipmentDao, Shipment, Shipment, Long> {
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
    private ShipmentTemplateDao shipmentTemplateDao;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
        shipmentTemplateDao = getContext().getBean(ShipmentTemplateDao.class);

        final AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany.getCompanyId());
        ap.setDescription("JUnit test alert pforile");
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
        p.setCompany(sharedCompany.getCompanyId());
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
        p.setCompany(sharedCompany.getCompanyId());
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
        s.setCompany(sharedCompany.getCompanyId());
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        PersonSchedule ps = new PersonSchedule();
        ps.setFromTime(45);
        ps.setSendApp(true);
        ps.setUser(createUser("asuvorov-1@google.com"));
        ps.setToTime(150);

        s.getSchedules().add(ps);

        alertNotifSched = notificationScheduleDao.save(s);

        s = new NotificationSchedule();
        s.setCompany(sharedCompany.getCompanyId());
        s.setName("Schd-Test");
        s.setDescription("Test schedule");

        ps = new PersonSchedule();
        ps.setFromTime(45);
        ps.setSendApp(true);
        ps.setToTime(150);
        ps.setUser(createUser("asuvorov-2@google.com"));

        s.getSchedules().add(ps);

        arrivalSched = notificationScheduleDao.save(s);

        //device
        deviceDao = getContext().getBean(DeviceDao.class);

        device = createDevice("3984709382475");
    }

    private User createUser(final String email) {
        final User u = new User();
        u.setCompany(this.sharedCompany.getCompanyId());
        u.setFirstName("Alexander");
        u.setLastName("Suvorov");
        u.setEmail(email);
        u.setPhone("11111111117");
        u.setTemperatureUnits(TemperatureUnits.Celsius);
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
        getContext().getBean(UserDao.class).save(u);
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
        d.setCompany(sharedCompany.getCompanyId());
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
        s.setShipmentDate(parseDate("1988-12-12T11:11:11"));
        s.setEta(parseDate("1988-12-15T11:11:11"));
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setSendArrivalReport(false);
        s.setSendArrivalReportOnlyIfAlerts(true);
        s.setCompany(sharedCompany.getCompanyId());
        s.getCustomFields().put("field1", "Custom field 1");
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setPalletId("PalletID");
        s.setAssetNum("PoNum");
        s.setPoNum(329487);
        s.setTripCount(876);
        s.setShipmentDescription("Test Shipment");
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceAfterMinutes(70);
        s.setStatus(ShipmentStatus.InProgress);
        s.getAlertsNotificationSchedules().add(alertNotifSched);
        s.getArrivalNotificationSchedules().add(arrivalSched);
        s.setCommentsForReceiver("commentsForReceiver");
        s.setSiblingCount(11);
        s.getSiblings().add(7l);
        s.getSiblings().add(8l);
        s.setNoAlertsAfterArrivalMinutes(7);
        s.setNoAlertsAfterStartMinutes(77);
        s.setShutDownAfterStartMinutes(9);
        s.setStartDate(parseDate("1988-12-15T12:12:12"));
        s.setCreatedBy("developer");
        return s;
    }

    /**
     * @param source
     * @return
     */
    private Date parseDate(final String source) {
        try {
            return dateFormat.parse(source);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Shipment s) {
        assertEquals(device.getId(), s.getDevice().getId());
        assertNotNull(s.getAlertProfile());
        assertEquals(5, s.getAlertSuppressionMinutes());
        assertEquals(17, s.getArrivalNotificationWithinKm().intValue());
        assertFalse(s.isSendArrivalReport());
        assertTrue(s.isSendArrivalReportOnlyIfAlerts());
        assertEquals(sharedCompany.getId(), s.getCompanyId());
        assertEquals("Custom field 1", s.getCustomFields().get("field1"));
        assertEquals(true, s.isExcludeNotificationsIfNoAlerts());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getAssetNum());
        assertEquals(329487, s.getPoNum());
        assertTrue(s.getTripCount() > 0);
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertNotNull(s.getShipmentDate());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(70, s.getShutdownDeviceAfterMinutes().intValue());
        assertEquals(ShipmentStatus.InProgress, s.getStatus());
        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
        assertEquals("commentsForReceiver", s.getCommentsForReceiver());
        assertEquals(2, s.getSiblings().size());
        assertTrue(s.getSiblings().contains(7l));
        assertTrue(s.getSiblings().contains(8l));
        assertEquals(11, s.getSiblingCount());
        assertEquals(new Integer(7), s.getNoAlertsAfterArrivalMinutes());
        assertEquals(new Integer(77), s.getNoAlertsAfterStartMinutes());
        assertEquals(new Integer(9), s.getShutDownAfterStartMinutes());
        assertEquals("1988-12-12T11:11:11", dateFormat.format(s.getShipmentDate()));
        assertEquals("1988-12-15T11:11:11", dateFormat.format(s.getEta()));
        assertEquals("1988-12-15T12:12:12", dateFormat.format(s.getStartDate()));
        assertEquals("developer", s.getCreatedBy());
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
    public void testCompanyAccess() {
        Shipment s = createTestEntity();
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        s.getCompanyAccess().add(c1);
        s.getCompanyAccess().add(c2);

        dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(2, s.getCompanyAccess().size());

        //attempt to remove schedules
        s.getCompanyAccess().remove(0);
        s = dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(1, s.getCompanyAccess().size());
    }
    @Test
    public void testUserAccess() {
        Shipment s = createTestEntity();
        final User u1 = createUser("u1@smarttrace.com.au");
        final User u2 = createUser("u2@smarttrace.com.au");

        s.getUserAccess().add(u1);
        s.getUserAccess().add(u2);

        dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(2, s.getUserAccess().size());

        //attempt to remove schedules
        s.getUserAccess().remove(0);
        s = dao.save(s);
        s = dao.findOne(s.getId());

        assertEquals(1, s.getUserAccess().size());
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
        assertEquals(17, s.getArrivalNotificationWithinKm().intValue());
        assertFalse(s.isSendArrivalReport());
        assertTrue(s.isSendArrivalReportOnlyIfAlerts());
        assertEquals(sharedCompany.getId(), s.getCompanyId());
        assertEquals("Custom field 1", s.getCustomFields().get("field1"));
        assertEquals(true, s.isExcludeNotificationsIfNoAlerts());
        assertEquals("PalletID", s.getPalletId());
        assertEquals("PoNum", s.getAssetNum());
        assertEquals("Test Shipment", s.getShipmentDescription());
        assertTrue(s.getTripCount() > 0);
        assertNotNull(s.getShipmentDate());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(70, s.getShutdownDeviceAfterMinutes().intValue());
        assertEquals(ShipmentStatus.InProgress, s.getStatus());
        assertEquals(1, s.getAlertsNotificationSchedules().size());
        assertEquals(1, s.getArrivalNotificationSchedules().size());
        assertEquals("commentsForReceiver", s.getCommentsForReceiver());
        assertEquals(new Integer(7), s.getNoAlertsAfterArrivalMinutes());
        assertEquals(new Integer(9), s.getShutDownAfterStartMinutes());
   }

    @Test
    public void testFindLastShipment() {
        //test ignores shipment templates
        createShipmentTemplate();
        assertNull(dao.findLastShipment(device.getImei(), null));

        //check found active
        dao.save(createTestEntity());
        dao.save(createTestEntity());
        dao.save(createTestEntity());

        final Shipment s = createTestEntity();
        dao.save(s);

        //check ignores in complete state
        assertEquals(s.getId(), dao.findLastShipment(device.getImei(), null).getId());

        //check ignores other devices
        final Device d = createDevice("2340982349");
        s.setDevice(d);
        dao.save(s);
        assertNotSame(s.getId(), dao.findLastShipment(device.getImei(), null).getId());
        assertEquals(s.getId(), dao.findLastShipment(d.getImei(), null).getId());
    }
    @Test
    public void testFindLastShipmentWithBaconId() {
        final String beaconId = "device-beracon-ID";

        //test ignores shipment templates
        final Shipment s = createTestEntity();
        s.setBeaconId(beaconId);
        dao.save(s);

        //check ignores in complete state
        assertNull(dao.findLastShipment(device.getImei(), null));
        assertEquals(s.getId(), dao.findLastShipment(device.getImei(), beaconId).getId());
        assertNull(dao.findLastShipment(device.getImei(), "abrakadabra"));
    }
    @Test
    public void testGetShipmentId() {
        dao.save(createTestEntity());
        final Shipment s = createTestEntity();
        dao.save(s);

        assertEquals(s.getId(), dao.getShipmentId(
                Device.getSerialNumber(s.getDevice().getImei()), s.getTripCount()));
    }
    @Test
    public void testFindNextShipmentFor() {
        final Shipment s = createTestEntity();
        dao.save(s);
        assertNull(dao.findNextShipmentFor(s));

        createShipmentTemplate();
        assertNull(dao.findNextShipmentFor(s));

        final Shipment next = dao.save(createTestEntity());
        dao.save(createTestEntity());

        //check ignores in complete state
        assertEquals(next.getId(), dao.findNextShipmentFor(s).getId());
    }
    @Test
    public void testSaveDefaultShipment() {
        final Shipment s = new Shipment();
        s.setCompany(sharedCompany.getCompanyId());
        s.setDevice(device);
        dao.save(s);

        assertNotNull(dao.findOne(s.getId()));
    }
    @Test
    public void testSaveDeviceShutdownDate() {
        final Date time = new Date(System.currentTimeMillis() - 100000000l);

        final Shipment s = new Shipment();
        s.setCompany(sharedCompany.getCompanyId());
        s.setDevice(device);
        s.setDeviceShutdownTime(time);
        dao.save(s);

        final Shipment one = dao.findOne(s.getId());
        assertNotNull(one);
        assertTrue(time.getTime() - one.getDeviceShutdownTime().getTime() < 2000);
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

        assertEquals(1, dao.findByCompany(sharedCompany.getCompanyId(), null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left.getCompanyId(), null, null, null).size());
    }
    @Test
    public void testFindActiveShipments() {
        //create companies
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        //create shipments
        createShipment(c1, ShipmentStatus.InProgress);
        createShipment(c1, ShipmentStatus.Default);
        createShipment(c1, ShipmentStatus.Ended);
        createShipment(c1, ShipmentStatus.Arrived);

        createShipment(c2, ShipmentStatus.InProgress);
        createShipment(c2, ShipmentStatus.Default);
        createShipment(c2, ShipmentStatus.Ended);
        createShipment(c2, ShipmentStatus.Arrived);

        assertEquals(2, dao.findActiveShipments(c1.getId()).size());
    }
    @Test
    public void testGetShipmentSiblingInfo() {
        final Company c1 = createCompany("C1");

        //create shipments
        Shipment s1 = createShipment(c1, ShipmentStatus.InProgress);
        final Shipment s2 = createShipment(c1, ShipmentStatus.Default);
        final Shipment s3 = createShipment(c1, ShipmentStatus.Ended);

        s1.getSiblings().clear();
        s1.getSiblings().add(s2.getId());
        s1.getSiblings().add(s3.getId());
        s1.setSiblingCount(2);

        s1 = dao.save(s1);

        final ShipmentSiblingInfo info = dao.getShipmentSiblingInfo(s1.getId());
        assertEquals(2, info.getSiblings().size());
        assertTrue(info.getSiblings().contains(s2.getId()));
        assertTrue(info.getSiblings().contains(s3.getId()));
    }
    @Test
    public void testFindActiveShipmentsByImei() {
        //create companies
        final Device c1 = createDevice("1029837012897");
        final Device c2 = createDevice("9890832408744");

        //create shipments
        createShipment(c1, ShipmentStatus.InProgress);
        createShipment(c1, ShipmentStatus.Default);
        createShipment(c1, ShipmentStatus.Ended);
        createShipment(c1, ShipmentStatus.Arrived);

        createShipment(c2, ShipmentStatus.InProgress);
        createShipment(c2, ShipmentStatus.Default);
        createShipment(c2, ShipmentStatus.Ended);
        createShipment(c2, ShipmentStatus.Arrived);

        assertEquals(2, dao.findActiveShipments(c1.getImei()).size());
    }
    @Test
    public void testSelectIfNullDevice() {
        final Shipment s = createShipment(sharedCompany, ShipmentStatus.Arrived);
        s.setDevice(null);
        dao.save(s);

        assertNotNull(dao.findOne(s.getId()));
    }
    @Test
    public void testOrderByDeviceSN() {
        final Shipment s1 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment s2 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment s3 = createShipment(sharedCompany, ShipmentStatus.Arrived);

        final Device d1 = createDevice("11111131111113");
        final Device d2 = createDevice("11111121111122");
        final Device d3 = createDevice("11111111111131");

        s3.setDevice(d1);
        s2.setDevice(d2);
        s1.setDevice(d3);

        dao.save(s1);
        dao.save(s2);
        dao.save(s3);

        List<Shipment> result = dao.findAll(null, new Sorting(true, ShipmentConstants.DEVICE_SN), null);

        assertEquals(s3.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s1.getId(), result.get(2).getId());

        result = dao.findAll(null, new Sorting(false, ShipmentConstants.DEVICE_SN), null);

        assertEquals(s1.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s3.getId(), result.get(2).getId());
    }
    @Test
    public void testFilterByDeviceSN() {
        final Shipment s1 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment s2 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment s3 = createShipment(sharedCompany, ShipmentStatus.Arrived);

        final Device d1 = createDevice("11111131111113");
        final Device d2 = createDevice("11111121111122");
        final Device d3 = createDevice("11111111111131");

        s3.setDevice(d1);
        s2.setDevice(d2);
        s1.setDevice(d3);

        dao.save(s1);
        dao.save(s2);
        dao.save(s3);

        final Filter f = new Filter();
        f.addFilter(ShipmentConstants.DEVICE_SN, "111112");
        final List<Shipment> result = dao.findAll(f, null, null);

        assertEquals(1, result.size());
        assertEquals(d2.getImei(), result.get(0).getDevice().getImei());
    }
    @Test
    public void testFindBySnTrip() {
        final String sn = "001111";
        final int trip = 1;

        final Shipment s0 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Device d0 = createDevice("11111130000013");
        s0.setDevice(d0);
        s0.setTripCount(trip);
        dao.save(s0);

        assertNull(dao.findBySnTrip(sharedCompany.getCompanyId(), sn, trip));

        final Shipment s1 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Device d1 = createDevice("1111113" + sn + "3");
        s1.setDevice(d1);
        s1.setTripCount(trip);
        dao.save(s1);

        assertNotNull(dao.findBySnTrip(sharedCompany.getCompanyId(), sn, trip));
        assertNotNull(dao.findBySnTrip(sharedCompany.getCompanyId(), "1111", trip));

        //create other device with same SN
        final Shipment s2 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Device d2 = createDevice("1111112" + sn + "2");
        s2.setDevice(d2);
        s2.setTripCount(trip);
        dao.save(s2);

        try {
            dao.findBySnTrip(sharedCompany.getCompanyId(), sn, trip);
            throw new AssertionFailedError("Runtime exception should be thrown");
        } catch (final RuntimeException e) {
            //normal
        }

        //create other company device with same SN
        final Company left = createCompany("Left");

        final Shipment s3 = createShipment(left, ShipmentStatus.Arrived);
        final Device d3 = createDevice("1111112" + sn + "2");
        d3.setCompany(left.getCompanyId());
        s3.setDevice(d3);
        s3.setTripCount(trip);
        dao.save(s3);

        assertNotNull(dao.findBySnTrip(left.getCompanyId(), sn, trip));
    }
    @Test
    public void testOrderByLocation() {
        final LocationProfile l1 = createLocationProfile("A");
        final LocationProfile l2 = createLocationProfile("B");
        final LocationProfile l3 = createLocationProfile("C");

        final Shipment s1 = createShipment(l1, l3);
        final Shipment s2 = createShipment(l2, l2);
        final Shipment s3 = createShipment(l3, l1);

        //test shipped from.
        List<Shipment> result = dao.findAll(null, new Sorting(true,
                ShipmentConstants.SHIPPED_FROM_LOCATION_NAME), null);

        assertEquals(s1.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s3.getId(), result.get(2).getId());

        result = dao.findAll(null, new Sorting(false,
                ShipmentConstants.SHIPPED_FROM_LOCATION_NAME), null);

        assertEquals(s3.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s1.getId(), result.get(2).getId());

        //test shipped to.
        result = dao.findAll(null, new Sorting(true,
                ShipmentConstants.SHIPPED_TO_LOCATION_NAME), null);

        assertEquals(s3.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s1.getId(), result.get(2).getId());

        result = dao.findAll(null, new Sorting(false,
                ShipmentConstants.SHIPPED_TO_LOCATION_NAME), null);

        assertEquals(s1.getId(), result.get(0).getId());
        assertEquals(s2.getId(), result.get(1).getId());
        assertEquals(s3.getId(), result.get(2).getId());
    }
    @Test
    public void testCreateNewFrom() {
        final ShipmentTemplate tpl = createShipmentTemplate();
        final Shipment s = dao.createNewFrom(tpl);

        assertNotNull(s);
        assertNotSame(tpl.getId(), s.getId());
        assertEquals(tpl.getShipmentDescription(), s.getShipmentDescription());
    }
    @Test
    public void testUpdateSiblingInfo() {
        Shipment s = createTestEntity();
        s.getSiblings().clear();
        s.setSiblingCount(0);
        s = dao.save(s);

        //add siblings
        s.getSiblings().add(1l);
        s.getSiblings().add(2l);

        dao.updateSiblingInfo(s.getId(), s.getSiblings());

        //check sibling group and sibling count updated
        s = dao.findOne(s.getId());
        assertEquals(2, s.getSiblingCount());
        assertEquals(2, s.getSiblings().size());
        assertTrue(s.getSiblings().contains(1l));
        assertTrue(s.getSiblings().contains(2l));
    }
    @Test
    public void testUpdateEta() {
        Shipment s = createShipment(sharedCompany, ShipmentStatus.InProgress);
        final Date eta = new Date(System.currentTimeMillis() - 10000000l);

        dao.updateEta(s, eta);
        s = dao.findOne(s.getId());

        assertEquals(eta.getTime(), s.getEta().getTime(), 1001);
    }
    @Test
    public void testUpdateLastEventDate() {
        Shipment s = createShipment(sharedCompany, ShipmentStatus.InProgress);
        final Date date = new Date(System.currentTimeMillis() - 10000000l);

        dao.updateLastEventDate(s, date);
        s = dao.findOne(s.getId());

        assertEquals(date.getTime(), s.getLastEventDate().getTime(), 1001);
    }
    @Test
    public void testMoveToNewDevice() {
        final Device d1 = createDevice("390248703928740");
        final Device d2 = createDevice("293087098709870");

        final Shipment a = createShipment(d1, ShipmentStatus.Default);

        dao.moveToNewDevice(d1, d2);
        assertEquals(d2.getImei(), dao.findOne(a.getId()).getDevice().getImei());
    }
    @Test
    public void testGetPreliminarySingleShipmentDataSnTrip() {
        final Shipment s = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment sib1 = createShipment(sharedCompany, ShipmentStatus.Arrived);
        final Shipment sib2 = createShipment(sharedCompany, ShipmentStatus.Arrived);

        s.getSiblings().add(sib1.getId());
        s.getSiblings().add(sib2.getId());
        s.setSiblingCount(2);
        dao.save(s);

        final PreliminarySingleShipmentData data = dao.getPreliminarySingleShipmentData(
                null, s.getDevice().getSn(), s.getTripCount());

        assertEquals(s.getId(), data.getShipment());
        assertEquals(s.getCompanyId(), data.getCompany());
        assertTrue(data.getSiblings().contains(sib1.getId()));
        assertTrue(data.getSiblings().contains(sib2.getId()));
    }
    @Test
    public void testCompanyShipmentsCompany() {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        createShipment(c1, ShipmentStatus.Arrived);
        createShipment(c1, ShipmentStatus.Arrived);
        createShipment(c2, ShipmentStatus.Arrived);

        assertEquals(2, dao.getCompanyShipments(c1.getId(), null, null, null).getItems().size());
        assertEquals(1, dao.getCompanyShipments(c2.getId(), null, null, null).getItems().size());
    }
    @Test
    public void testCompanyShipmentsPaging() {
        final Company c1 = createCompany("C1");

        createShipment(c1, ShipmentStatus.Arrived);
        createShipment(c1, ShipmentStatus.Arrived);
        createShipment(c1, ShipmentStatus.Arrived);

        assertEquals(2, dao.getCompanyShipments(c1.getId(), null, new Page(1, 2), null).getItems().size());
        final ListResult<ListShipmentItem> secondPage = dao.getCompanyShipments(
                c1.getId(), null, new Page(2, 2), null);
        assertEquals(1, secondPage.getItems().size());
        assertEquals(3, secondPage.getTotalCount());
    }
    @Test
    public void testCompanyShipmentsMainData() {
        final Shipment s = new Shipment();

        final AlertProfile ap = new AlertProfile();
        ap.setName("ApName");
        ap.setCompany(sharedCompany.getCompanyId());
        context.getBean(AlertProfileDao.class).save(ap);

        final LocationProfile from = createLocationProfile("LocFrom");
        from.getLocation().setLatitude(12.2);
        from.getLocation().setLongitude(13.3);
        from.setCompany(sharedCompany.getCompanyId());
        context.getBean(LocationProfileDao.class).save(from);

        final LocationProfile to = createLocationProfile("LocTo");
        to.getLocation().setLatitude(12.2);
        to.getLocation().setLongitude(13.3);
        to.setCompany(sharedCompany.getCompanyId());
        context.getBean(LocationProfileDao.class).save(to);

        final Date arrivalDate = new Date(System.currentTimeMillis() - 293487l);
        final String assetNum = "asset num";
        final String assetType = "any asset type";
        final Date eta = new Date(System.currentTimeMillis() - 2094587l);
        final String paletId = "AnyPaletID";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 239570987l);
        final String description = "Shipment Description";
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final String beaconId = "beacon-ID";

        s.setAlertProfile(ap);
        s.setBeaconId(beaconId);
        s.setShippedFrom(from);
        s.setShippedTo(to);
        s.setArrivalDate(arrivalDate);
        s.setAssetNum(assetNum);
        s.setAssetType(assetType);
        s.setEta(eta);
        s.setPalletId(paletId);
        s.setShipmentDate(shipmentDate);
        s.setShipmentDescription(description);
        s.setStatus(status);
        s.setDevice(device);
        s.setCompany(sharedCompany.getCompanyId());
        s.setArrivalDate(arrivalDate);

        dao.save(s);

        //first reading
        final double firstReadingLat = 14.5;
        final double firstReadingLon = 13.2;
        final Date firstReadingTime = new Date(System.currentTimeMillis() - 9238470l);
        final int firstReadingBattery = 2554;
        createTrackerEvent(s, firstReadingTime, firstReadingLat, firstReadingLon, firstReadingBattery, 22.33);

        final double lastReadingLat = 14.3;
        final double lastReadingLon = 17.2;
        final Date lastReadingTime = new Date(firstReadingTime.getTime() + 1111111l);
        final int lastReadingBattery = 34987;
        final double lastReadingTemperature = 36.6;
        //last reading
        createTrackerEvent(s, lastReadingTime, lastReadingLat, lastReadingLon,
                lastReadingBattery, lastReadingTemperature);

        final ListShipmentItem item = dao.getCompanyShipments(
                s.getCompanyId(), null, null, null).getItems().get(0);

        //check result
        assertEqualsDates(arrivalDate, item.getActualArrivalDate());
        assertEquals(ap.getId(), item.getAlertProfileId());
        assertEquals(ap.getName(), item.getAlertProfileName());
        assertEquals(assetNum, item.getAssetNum());
        assertEquals(assetType, item.getAssetType());
        assertEquals(device.getImei(), item.getDevice());
        assertEquals(device.getName(), item.getDeviceName());
        assertEquals(device.getSn(), item.getDeviceSN());
        assertEqualsDates(eta, item.getEta());
        assertEquals(firstReadingLat, item.getFirstReadingLat().doubleValue(), 0.001);
        assertEquals(firstReadingLon, item.getFirstReadingLong().doubleValue(), 0.001);
        assertEqualsDates(firstReadingTime, item.getFirstReadingTime());
        assertEquals(lastReadingBattery, item.getLastReadingBattery().intValue());
        assertEquals(lastReadingLat, item.getLastReadingLat().doubleValue(), 0.001);
        assertEquals(lastReadingLon, item.getLastReadingLong().doubleValue(), 0.001);
        assertEquals(lastReadingTemperature, item.getLastReadingTemperature().doubleValue(), 0.001);
        assertEqualsDates(lastReadingTime, item.getLastReadingTime());
        assertEquals(paletId, item.getPalettId());
        assertEqualsDates(shipmentDate, item.getShipmentDate());
        assertEquals(description, item.getShipmentDescription());
        assertEquals(s.getId(), item.getShipmentId());
        assertEquals(from.getName(), item.getShippedFrom());
        assertEquals(from.getLocation().getLatitude(), item.getShippedFromLat().doubleValue(), 0.001);
        assertEquals(from.getLocation().getLongitude(), item.getShippedFromLong().doubleValue(), 0.001);
        assertEquals(to.getName(), item.getShippedTo());
        assertEquals(status, item.getStatus());
        assertEquals(s.getTripCount(), item.getTripCount());
        assertEquals(beaconId, item.getBeaconId());
    }
    /**
     * @param time
     * @param lat
     * @param lon
     * @param battery
     */
    private TrackerEvent createTrackerEvent(final Shipment s, final Date time, final double lat, final double lon,
            final int battery, final double t) {
        final TrackerEvent e = new TrackerEvent();
        e.setTime(time);
        e.setCreatedOn(time);
        e.setBattery(battery);
        e.setDevice(s.getDevice());
        e.setShipment(s);
        e.setTemperature(t);
        e.setType(TrackerEventType.AUT);
        e.setLatitude(lat);
        e.setLongitude(lon);
        return context.getBean(TrackerEventDao.class).save(e);
    }

    /**
     * @param c company.
     * @param status shipment status.
     */
    private Shipment createShipment(final Company c, final ShipmentStatus status) {
        final Shipment s = createTestEntity();
        s.setCompany(c.getCompanyId());
        s.setStatus(status);
        return dao.save(s);
    }
    /**
     * @param shippedFrom shipped from location.
     * @param shippedTo shipped to location.
     * @return shipment.
     */
    private Shipment createShipment(final LocationProfile shippedFrom, final LocationProfile shippedTo) {
        final Shipment s = createTestEntity();
        s.setStatus(ShipmentStatus.InProgress);
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        return dao.save(s);
    }

    /**
     * @param name
     * @return
     */
    private LocationProfile createLocationProfile(final String name) {
        final LocationProfile p = new LocationProfile();
        p.setAddress("Address of " + name);
        p.setCompany(sharedCompany.getCompanyId());
        p.setInterim(true);
        p.setName(name);
        p.setNotes("Any notes");
        p.setRadius(700);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.200);
        p.getLocation().setLongitude(300.400);

        return locationProfileDao.save(p);
    }
    /**
     * @param d device.
     * @param status status.
     */
    private Shipment createShipment(final Device d, final ShipmentStatus status) {
        final Shipment s = createTestEntity();
        s.setCompany(sharedCompany.getCompanyId());
        s.setStatus(status);
        s.setDevice(d);
        return dao.save(s);
    }


    public ShipmentTemplate createShipmentTemplate() {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setCompany(sharedCompany.getCompanyId());
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setName("Shipment-1");
        s.setShipmentDescription("Test Shipment");
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceAfterMinutes(70);
        s.getAlertsNotificationSchedules().add(alertNotifSched);
        s.getArrivalNotificationSchedules().add(arrivalSched);
        s.setAddDateShipped(true);
        s.setDetectLocationForShippedFrom(true);
        return shipmentTemplateDao.save(s);
    }
    /**
     * @param arrivalDate
     * @param actualArrivalDate
     */
    private void assertEqualsDates(final Date arrivalDate, final Date actualArrivalDate) {
        final long diff = Math.abs(arrivalDate.getTime() - actualArrivalDate.getTime());
        if (diff > 500) {
            throw new AssertionFailedError("Dates difference: " +  diff);
        }
    }
}
