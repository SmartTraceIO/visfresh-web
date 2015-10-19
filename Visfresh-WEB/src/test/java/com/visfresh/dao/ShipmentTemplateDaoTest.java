/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateDaoTest
    extends BaseCrudTest<ShipmentTemplateDao, ShipmentTemplate, Long> {

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
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected ShipmentTemplate createTestEntity() {
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
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final ShipmentTemplate tpl) {
        assertNotNull(tpl.getAlertProfile());
        assertEquals(5, tpl.getAlertSuppressionDuringCoolDown());
        assertEquals(17, tpl.getArrivalNotificationWithIn());
        assertEquals(sharedCompany.getId(), tpl.getCompany().getId());
        assertEquals(true, tpl.isExcludeNotificationsIfNoAlertsFired());
        assertEquals("Shipment-1", tpl.getName());
        assertEquals("Test Shipment", tpl.getShipmentDescription());
        assertNotNull(tpl.getShippedFrom());
        assertNotNull(tpl.getShippedTo());
        assertEquals(70, tpl.getShutdownDeviceTimeOut());
        assertEquals(1, tpl.getAlertsNotificationSchedules().size());
        assertEquals(1, tpl.getArrivalNotificationSchedules().size());
        assertTrue(tpl.isAddDateShipped());
        assertTrue(tpl.isDetectLocationForShippedFrom());
        assertTrue(tpl.isUseCurrentTimeForDateShipped());
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
        assertEquals(5, tpl.getAlertSuppressionDuringCoolDown());
        assertEquals(17, tpl.getArrivalNotificationWithIn());
        assertEquals(sharedCompany.getId(), tpl.getCompany().getId());
        assertEquals(true, tpl.isExcludeNotificationsIfNoAlertsFired());
        assertEquals("Shipment-1", tpl.getName());
        assertEquals("Test Shipment", tpl.getShipmentDescription());
        assertNotNull(tpl.getShippedFrom());
        assertNotNull(tpl.getShippedTo());
        assertEquals(70, tpl.getShutdownDeviceTimeOut());
        assertEquals(1, tpl.getAlertsNotificationSchedules().size());
        assertEquals(1, tpl.getArrivalNotificationSchedules().size());
        assertTrue(tpl.isAddDateShipped());
        assertTrue(tpl.isDetectLocationForShippedFrom());
        assertTrue(tpl.isUseCurrentTimeForDateShipped());
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
    }
}
