/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.NotificationService;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportDaoTest extends BaseDaoTest<ShipmentReportDao> {
    /**
     *
     */
    private static final long ONE_DAY = 24 * 60 * 60 * 1000l;
    private ShipmentDao shipmentDao;
    private Shipment shipment;
    private User user;

    /**
     * Default constructor.
     */
    public ShipmentReportDaoTest() {
        super(ShipmentReportDao.class);
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);

        Device d = new Device();
        d.setImei("9238470983274987");
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);

        user = createUser("Vasily", "Chapaev");
    }
    /**
     * @param firstName
     * @param lastName
     * @return
     */
    private User createUser(final String firstName, final String lastName) {
        final User user = new User();
        user.setActive(true);
        user.setCompany(sharedCompany);
        user.setEmail(firstName + "." + lastName + "@mail.ru");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        user.setTimeZone(TimeZone.getTimeZone("UTC"));

        return context.getBean(UserDao.class).save(user);
    }

    @Test
    public void testGoods() {
        final int tripCount = 12;
        final String shipmentDescription = "2398327 987opwrei u3lk4jh";
        final String palletId = "0239847098";
        final String comments = "Shipment comments for receiver";

        shipment.setTripCount(tripCount);
        shipment.setShipmentDescription(shipmentDescription);
        shipment.setPalletId(palletId);
        shipment.setCommentsForReceiver(comments);

        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment);

        assertEquals(shipment.getDevice().getImei(), report.getDevice());
        assertEquals(tripCount, report.getTripCount());
        assertEquals(shipmentDescription, report.getDescription());
        assertEquals(palletId, report.getPalletId());
    }
    @Test
    public void testShipment() {
        final String locationFromName = "Deribasovskaya";
        final String locationToName = "Myasoedovskaya";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 15 * ONE_DAY);
        final Date arrivalDate = new Date(System.currentTimeMillis() - 10 * ONE_DAY);

        final LocationProfile locTo = new LocationProfile();
        locTo.setAddress("Odessa city, Myasoedovskaya 1, apt.1");
        locTo.setName(locationToName);
        locTo.setCompany(sharedCompany);
        locTo.setRadius(500);
        locTo.getLocation().setLatitude(10.);
        locTo.getLocation().setLongitude(11.);

        final LocationProfile locFrom = new LocationProfile();
        locFrom.setAddress("Odessa city, Deribasovskaya 1, apt.1");
        locFrom.setName(locationFromName);
        locFrom.setCompany(sharedCompany);
        locFrom.setRadius(500);
        locFrom.getLocation().setLatitude(10.);
        locFrom.getLocation().setLongitude(11.);

        shipment.setShippedFrom(locFrom);
        shipment.setShippedTo(locTo);
        shipment.setShipmentDate(shipmentDate);
        shipment.setArrivalDate(arrivalDate);

        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment);

        final DateFormat fmt = DateTimeUtils.createPrettyFormat(
                user.getLanguage(), user.getTimeZone());

        assertEquals(locationFromName, report.getShippedFrom());
        assertEquals(locationToName, report.getShippedTo());
        assertEquals(fmt.format(shipmentDate), fmt.format(report.getDateShipped()));
        assertEquals(fmt.format(arrivalDate), fmt.format(report.getDateArrived()));
    }
    @Test
    public void testTemperature() {
        final AlertProfile ap = createAlertProfile();

        shipment.setAlertProfile(ap);
        shipmentDao.save(shipment);

        final long dt = 10 * 60 * 1000l;
        final long startTime = System.currentTimeMillis() - dt * 1000;

        createTrackerEvent(startTime + 1 * dt, 20.7);
        createTrackerEvent(startTime + 2 * dt, 15.7);
        createTrackerEvent(startTime + 3 * dt, 10);
        createTrackerEvent(startTime + 4 * dt, 8);
        createTrackerEvent(startTime + 5 * dt, 6);
        createTrackerEvent(startTime + 6 * dt, 0);
        createTrackerEvent(startTime + 7 * dt, -11.5);
        createTrackerEvent(startTime + 8 * dt, -20.3);
        createTrackerEvent(startTime + 9 * dt, 0);

        final ShipmentReportBean report = dao.createReport(shipment);

        final List<TrackerEvent> events = context.getBean(TrackerEventDao.class).findAll(
                null, null, null);
        double summ = 0;
        for (final TrackerEvent e : events) {
            summ += e.getTemperature();
        }

        assertEquals(summ / events.size(), report.getAvgTemperature(), 0.0001);
        assertEquals(-20.3, report.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, report.getMaximumTemperature(), 0.0001);
        assertEquals(2 * dt, report.getTimeAboveUpperLimit());
        assertEquals(2 * dt, report.getTimeBelowLowerLimit());
        assertEquals(8 * dt, report.getTotalTime());
    }

    @Test
    public void testTemperatureWithAlertSuppression() {
        final AlertProfile ap = createAlertProfile();

        final long dt = 10 * 60 * 1000l;
        final long startTime = System.currentTimeMillis() - dt * 1000;

        shipment.setShipmentDate(new Date(startTime));
        shipment.setAlertProfile(ap);
        shipment.setAlertSuppressionMinutes((int) (5 * dt / (60 * 1000l)));
        shipmentDao.save(shipment);

        createTrackerEvent(startTime + 1 * dt, 20.7);
        createTrackerEvent(startTime + 2 * dt, 15.7);
        createTrackerEvent(startTime + 3 * dt, 10);
        createTrackerEvent(startTime + 4 * dt, 8);
        createTrackerEvent(startTime + 5 * dt, 6);
        createTrackerEvent(startTime + 6 * dt, 0);
        createTrackerEvent(startTime + 7 * dt, -11.5);
        createTrackerEvent(startTime + 8 * dt, -20.3);
        createTrackerEvent(startTime + 9 * dt, 0);

        final ShipmentReportBean report = dao.createReport(shipment);

        final List<TrackerEvent> events = context.getBean(TrackerEventDao.class).findAll(
                null, null, null).subList(5, 9);
        double summ = 0;
        for (final TrackerEvent e : events) {
            summ += e.getTemperature();
        }

        assertEquals(summ / events.size(), report.getAvgTemperature(), 0.0001);
        assertEquals(-20.3, report.getMinimumTemperature(), 0.0001);
        assertEquals(0, report.getMaximumTemperature(), 0.0001);
        assertEquals(0, report.getTimeAboveUpperLimit());
        assertEquals(2 * dt, report.getTimeBelowLowerLimit());
        assertEquals(3 * dt, report.getTotalTime());
    }

    /**
     * @return
     */
    protected AlertProfile createAlertProfile() {
        final AlertProfile ap = new AlertProfile();

        ap.setName("JUnit Alerts");
        ap.setUpperTemperatureLimit(15.3);
        ap.setLowerTemperatureLimit(-11.2);
        ap.setCompany(sharedCompany);

        context.getBean(AlertProfileDao.class).save(ap);
        return ap;
    }
    /**
     * @return
     */
    @Test
    public void testNoReadings() {
        final ShipmentReportBean report = dao.createReport(shipment);
        assertEquals(0., report.getLowerTemperatureLimit(), 0.001);
        assertEquals(5., report.getUpperTemperatureLimit(), 0.001);

        assertNull(report.getAvgTemperature());
        assertNull(report.getMaximumTemperature());
        assertNull(report.getMinimumTemperature());
        assertNull(report.getStandardDevitation());
    }

    @Test
    public void testFiredAlertRules() {
        final TrackerEvent e = createTrackerEvent(System.currentTimeMillis(), -10);

        final Alert a1 = createAlert(e);
        createAlert(e);

        final TemperatureRule rule = new TemperatureRule();
        rule.setType(a1.getType());
        rule.setTemperature(e.getTemperature());

        final AlertProfile ap = createAlertProfile();
        ap.getAlertRules().add(rule);

        shipment.setAlertProfile(ap);
        shipmentDao.save(shipment);

        final ShipmentSession session = new ShipmentSession();
        AbstractRuleEngine.setProcessedTemperatureRule(session, rule);
        AbstractRuleEngine.setProcessedTemperatureRule(session, rule);
        context.getBean(ShipmentSessionDao.class).saveSession(shipment, session);

        final ShipmentReportBean report = dao.createReport(shipment);
        assertEquals(1, report.getFiredAlertRules().size());
        assertEquals(a1.getType(), report.getFiredAlertRules().get(0).getType());
    }
    @Test
    public void testAlerts() {
        final TrackerEvent e = createTrackerEvent(System.currentTimeMillis(), -10);

        final Alert a1 = createAlert(e);
        createAlert(e);
        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment);
        assertEquals(2, report.getAlerts().size());
        assertEquals(a1.getType(), report.getAlerts().get(0).getType());
    }
    @Test
    public void testDeviceColor() {
        shipment.getDevice().setColor(Color.DarkGoldenrod);
        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment);
        assertNotNull(report.getDeviceColor());
    }
    @Test
    public void testWhoNotified() throws Exception {
        final TrackerEvent e = createTrackerEvent(System.currentTimeMillis(), -10);

        final Alert a1 = createAlert(e);
        final Alert a2 = createAlert(e);

        final User u1 = createUser("U", "1");
        final User u2 = createUser("U", "2");
        createUser("U", "3");

        //create personal schedule for user.
        final PersonSchedule s1 = createSchedule(u1);
        final PersonSchedule s2 = createSchedule(u2);

        final NotificationSchedule ns = new NotificationSchedule();
        ns.setCompany(sharedCompany);
        ns.setName("JUnit NS");
        ns.getSchedules().add(s1);
        ns.getSchedules().add(s2);
        context.getBean(NotificationScheduleDao.class).save(ns);

        shipment.getAlertsNotificationSchedules().add(ns);
        shipmentDao.save(shipment);

        final NotificationService notificator = context.getBean(NotificationService.class);

        notificator.sendNotification(s1, a1, e);
        notificator.sendNotification(s2, a2, e);

        final ShipmentReportBean report = dao.createReport(shipment);
        assertEquals(2, report.getWhoWasNotified().size());
    }
    @Test
    public void testPossibleShippedTo() {
        final LocationProfile loc1 = craeteLocation("Loc 1");
        final LocationProfile loc2 = craeteLocation("Loc 2");

        final AlternativeLocations alts = new AlternativeLocations();
        alts.getTo().add(loc1);
        alts.getTo().add(loc2);

        context.getBean(AlternativeLocationsDao.class).save(shipment, alts);

        final ShipmentReportBean bean = dao.createReport(shipment);
        assertEquals(2, bean.getPossibleShippedTo().size());
        assertEquals(loc1.getName(), bean.getPossibleShippedTo().get(0));
        assertEquals(loc2.getName(), bean.getPossibleShippedTo().get(1));
    }

    /**
     * @param name location name.
     * @return location profile.
     */
    private LocationProfile craeteLocation(final String name) {
        final LocationProfile loc = new LocationProfile();
        loc.setCompany(sharedCompany);
        loc.setName(name);
        loc.setAddress(name + " address");
        loc.setRadius(500);
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        return context.getBean(LocationProfileDao.class).save(loc);
    }

    /**
     * @param user
     * @return
     */
    private PersonSchedule createSchedule(final User user) {
        final PersonSchedule s = new PersonSchedule();
        s.setFromTime(0);
        s.setToTime(24 * 60 - 1);
        s.setUser(user);
        s.setAllWeek();
        return s;
    }

    /**
     * @param e
     * @return
     */
    protected Alert createAlert(final TrackerEvent e) {
        final Alert alert = new Alert();
        alert.setDate(e.getTime());
        alert.setType(AlertType.CriticalCold);
        alert.setDevice(shipment.getDevice());
        alert.setShipment(shipment);
        alert.setTrackerEventId(e.getId());
        return context.getBean(AlertDao.class).save(alert);
    }
    /**
     * @param time reading time.
     * @param temperature temperature.
     */
    private TrackerEvent createTrackerEvent(final long time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setTime(new Date(time));
        e.setCreatedOn(new Date());
        e.setTemperature(temperature);
        e.setShipment(shipment);
        e.setType(TrackerEventType.AUT);
        e.setDevice(shipment.getDevice());

        return context.getBean(TrackerEventDao.class).save(e);
    }
}
