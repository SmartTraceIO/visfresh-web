/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateDaoTest
    extends BaseCrudTest<ShipmentTemplateDao, ShipmentTemplate, ShipmentTemplate, Long> {

    private AlertProfile alertProfile;
    private LocationProfile shippedFrom;
    private LocationProfile shippedTo;
    private NotificationSchedule alertNotifSched;
    private NotificationSchedule arrivalSched;

    private AlertProfileDao alertProfileDao;
    private LocationProfileDao locationProfileDao;
    private NotificationScheduleDao notificationScheduleDao;

    /**
     * Default constructor.
     */
    public ShipmentTemplateDaoTest() {
        super(ShipmentTemplateDao.class);
    }

    @Before
    public void beforeTest() {
        //create alert profile
        alertProfileDao = getContext().getBean(AlertProfileDao.class);

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

        final User user = createUser();

        PersonSchedule ps = new PersonSchedule();
        ps.setFromTime(45);
        ps.setSendApp(true);
        ps.setToTime(150);
        ps.setUser(user);

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
        ps.setUser(user);

        s.getSchedules().add(ps);

        arrivalSched = notificationScheduleDao.save(s);
    }

    private User createUser() {
        final User u = new User();
        u.setCompany(this.sharedCompany.getCompanyId());
        u.setEmail("asuvorov@mail.ru");
        u.setFirstName("Alexander");
        u.setLastName("Suvorov");
        u.setEmail("asuvorov@google.com");
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
    protected ShipmentTemplate createTestEntity() {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setAlertProfile(alertProfile);
        s.setAlertSuppressionMinutes(5);
        s.setArrivalNotificationWithinKm(17);
        s.setSendArrivalReport(false);
        s.setSendArrivalReportOnlyIfAlerts(true);
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
        s.setNoAlertsAfterArrivalMinutes(7);
        s.setNoAlertsAfterStartMinutes(77);
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final ShipmentTemplate tpl) {
        assertNotNull(tpl.getAlertProfile());
        assertEquals(5, tpl.getAlertSuppressionMinutes());
        assertEquals(17, tpl.getArrivalNotificationWithinKm().intValue());
        assertFalse(tpl.isSendArrivalReport());
        assertTrue(tpl.isSendArrivalReportOnlyIfAlerts());
        assertEquals(sharedCompany.getId(), tpl.getCompanyId());
        assertEquals(true, tpl.isExcludeNotificationsIfNoAlerts());
        assertEquals("Shipment-1", tpl.getName());
        assertEquals("Test Shipment", tpl.getShipmentDescription());
        assertNotNull(tpl.getShippedFrom());
        assertNotNull(tpl.getShippedTo());
        assertEquals(70, tpl.getShutdownDeviceAfterMinutes().intValue());
        assertEquals(1, tpl.getAlertsNotificationSchedules().size());
        assertEquals(1, tpl.getArrivalNotificationSchedules().size());
        assertTrue(tpl.isAddDateShipped());
        assertTrue(tpl.isDetectLocationForShippedFrom());
        assertFalse(tpl.isAutostart());
        assertEquals(new Integer(7), tpl.getNoAlertsAfterArrivalMinutes());
        assertEquals(new Integer(77), tpl.getNoAlertsAfterStartMinutes());
    }
    @Test
    public void testFindByCompany() {
        dao.save(createTestEntity());

        assertEquals(1, dao.findByCompany(sharedCompany.getCompanyId(), null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left.getCompanyId(), null, null, null).size());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<ShipmentTemplate> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final ShipmentTemplate tpl = all.get(0);
        assertNotNull(tpl.getAlertProfile());
        assertEquals(5, tpl.getAlertSuppressionMinutes());
        assertEquals(17, tpl.getArrivalNotificationWithinKm().intValue());
        assertFalse(tpl.isSendArrivalReport());
        assertTrue(tpl.isSendArrivalReportOnlyIfAlerts());
        assertEquals(sharedCompany.getId(), tpl.getCompanyId());
        assertEquals(true, tpl.isExcludeNotificationsIfNoAlerts());
        assertEquals("Shipment-1", tpl.getName());
        assertEquals("Test Shipment", tpl.getShipmentDescription());
        assertNotNull(tpl.getShippedFrom());
        assertNotNull(tpl.getShippedTo());
        assertEquals(70, tpl.getShutdownDeviceAfterMinutes().intValue());
        assertEquals(1, tpl.getAlertsNotificationSchedules().size());
        assertEquals(1, tpl.getArrivalNotificationSchedules().size());
        assertTrue(tpl.isAddDateShipped());
        assertTrue(tpl.isDetectLocationForShippedFrom());
        assertFalse(tpl.isAutostart());
    }
    @Test
    public void autoStartShipment() {
        ShipmentTemplate t = createTestEntity();
        t.setAutostart(true);
        t = dao.save(t);

        assertEquals(1, dao.findAll(null, null, null).size());
        assertNotNull(dao.findOne(t.getId()));
    }
}
