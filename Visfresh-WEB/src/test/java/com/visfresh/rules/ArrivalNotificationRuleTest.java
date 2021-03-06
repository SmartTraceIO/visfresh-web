/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Language;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.mock.MockEmailService;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalNotificationRuleTest extends BaseRuleTest {
    private ArrivalNotificationRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public ArrivalNotificationRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = context.getBean(ArrivalNotificationRule.class);
        shipment = createDefaultShipment(ShipmentStatus.InProgress, createDevice("9283470987"));

        //create shipment session for avoid of NullPointerException
        final ShipmentSessionDao ssd = context.getBean(ShipmentSessionDao.class);
        ssd.saveSession(new ShipmentSession(shipment.getId()));
    }

    @Test
    public void testAccept() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, createSessionHolder(true));
        //final location not set
        assertFalse(rule.accept(req));

        final LocationProfile loc = createLocation();

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //so far
        assertFalse(rule.accept(req));

        //set nearest location
        e.setLatitude(10.);
        e.setLongitude(10.);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testAcceptInControl() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final LocationProfile loc = createLocation();

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder holder = createSessionHolder(true);

        e.setLatitude(loc.getLocation().getLatitude());
        e.setLongitude(loc.getLocation().getLongitude());
        rule.handle(new RuleContext(e, holder));

        e.setLatitude(0.);
        e.setLongitude(0.);
        assertTrue(rule.accept(new RuleContext(e, holder)));
    }
    @Test
    public void testNotAcceptNotLeavingStart() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final LocationProfile loc = createLocation();

        shipment.setShippedFrom(loc);
        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        e.setLatitude(10.);
        e.setLongitude(10.);
        assertFalse(rule.accept(new RuleContext(e, createSessionHolder(false))));
    }

    /**
     * @param setLeaveStart whether or not should set the leving the start flag.
     * @return session holder.
     */
    private SessionHolder createSessionHolder(final boolean setLeaveStart) {
        final SessionHolder s = new SessionHolder(shipment);
        if (setLeaveStart) {
            LeaveStartLocationRule.setLeavingStartLocation(s.getSession(shipment));
        }
        return s;
    }
    @Test
    public void testHandle() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final SessionHolder holder = createSessionHolder(true);

        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        rule.handle(new RuleContext(e, holder));
        //check arrival created
        final List<Arrival> arrivals = context.getBean(ArrivalDao.class).findAll(null, null, null);
        assertEquals(1, arrivals.size());

        final Arrival arrival = arrivals.get(0);
        assertEquals(shipment.getId(), arrival.getShipment().getId());
        assertEquals(e.getId(), arrival.getTrackerEventId());
    }
    @Test
    public void testClearInControlState() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);
        final double lat = e.getLatitude();
        final double lon = e.getLongitude();

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final SessionHolder holder = createSessionHolder(true);

        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        //initiate the flush of state
        e.setLatitude(lat + 10);
        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        //revert coordinates back
        e.setLatitude(lat);
        e.setLongitude(lon);

        //check arrival created
        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        rule.handle(new RuleContext(e, holder));
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());

        rule.handle(new RuleContext(e, holder));
        assertEquals(1, context.getBean(ArrivalDao.class).findAll(null, null, null).size());
    }
    @Test
    public void testSendNotification() {
        final String email = "arrival.developer@visfresh.com";
        final NotificationSchedule sched = createEmailNotificaitonSchedule(email);

        shipment.getArrivalNotificationSchedules().add(sched);
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final SessionHolder holder = createSessionHolder(true);

        //need three continuous events
        rule.handle(new RuleContext(e, holder));
        rule.handle(new RuleContext(e, holder));
        rule.handle(new RuleContext(e, holder));

        final MockEmailService emailer = context.getBean(MockEmailService.class);
        //check notification send
        final List<EmailMessage> emails = emailer.getMessages();
        assertEquals(1, emails.size());

        final EmailMessage msg = emails.get(0);
        assertEquals(1, msg.getEmails().length);
        assertEquals(email, msg.getEmails()[0]);
        assertTrue(msg.getMessage().contains(shipment.getDevice().getSn()));

        //check not report sent
        assertNull(emailer.getAttachments().get(0));
    }

    @Test
    public void testNotification() {
        shipment.setArrivalNotificationWithinKm(0);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        final String email = "arrival.developer@visfresh.com";
        final NotificationSchedule sched = createEmailNotificaitonSchedule(email);

        shipment.setShippedTo(loc);
        shipment.getArrivalNotificationSchedules().add(sched);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, createSessionHolder(true));
        assertTrue(rule.accept(req));
        rule.handle(req);
        rule.handle(req);
        rule.handle(req);

        //check notification send
        final List<EmailMessage> emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(1, emails.size());

        final EmailMessage msg = emails.get(0);
        assertEquals(1, msg.getEmails().length);
        assertEquals(email, msg.getEmails()[0]);
        assertTrue(msg.getMessage().contains(shipment.getDevice().getSn()));
    }
    @Test
    public void testExcludeNotificationIfNotAlerts() {
        shipment.setArrivalNotificationWithinKm(0);
        shipment.setExcludeNotificationsIfNoAlerts(true);

        //set location profile
        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        final String email = "arrival.developer@visfresh.com";
        final NotificationSchedule sched = createEmailNotificaitonSchedule(email);

        shipment.setShippedTo(loc);

        //set alert profile
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(company.getCompanyId());
        ap.setName("JUnit AP");
        ap.setDescription("JUnit AP");
        context.getBean(AlertProfileDao.class).save(ap);
        shipment.setAlertProfile(ap);

        shipment.getArrivalNotificationSchedules().add(sched);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, createSessionHolder(true));
        rule.handle(req);

        //check notification send
        List<EmailMessage> emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(0, emails.size());

        //create not temperature alert
        Alert a = new Alert();
        a.setDate(new Date());
        a.setType(AlertType.LightOn);
        a.setDevice(shipment.getDevice());
        a.setShipment(shipment);
        context.getBean(AlertDao.class).save(a);

        rule.handle(req);
        emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(0, emails.size());

        a = new TemperatureAlert();
        a.setDate(new Date());
        a.setType(AlertType.CriticalHot);
        a.setDevice(shipment.getDevice());
        a.setShipment(shipment);
        context.getBean(AlertDao.class).save(a);

        rule.handle(req);
        emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(1, emails.size());
    }
    @Test
    public void testArrivalWith0KmConfigured() {
        shipment.setArrivalNotificationWithinKm(0);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, createSessionHolder(true));
        assertTrue(rule.accept(req));
    }
    @Test
    public void testPreventOfDoubleHandling() {
        shipment.setArrivalNotificationWithinKm(0);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder state = createSessionHolder(true);
        //set nearest location
        final RuleContext req = new RuleContext(e, state);
        assertTrue(rule.accept(req));
        rule.handle(req);
        rule.handle(req);
        rule.handle(req);

        assertFalse(rule.accept(new RuleContext(e, state)));
    }

    /**
     * @param loc location.
     * @return tracker event with latitude/longitude near the given location.
     */
    private TrackerEvent createEventNearLocation(final LocationProfile loc) {
        final TrackerEvent e = createEvent(loc.getLocation().getLatitude(), loc.getLocation().getLongitude());
        return e;
    }
    /**
     * @param lat latitude.
     * @param lon longitude
     * @return tracker event with given latitude longitude.
     */
    private TrackerEvent createEvent(final double lat, final double lon) {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setLatitude(lat);
        e.setLongitude(lon);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @return location.
     */
    private LocationProfile createLocation() {
        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company.getCompanyId());
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        loc = context.getBean(LocationProfileDao.class).save(loc);
        return loc;
    }
    /**
     * @param email email.
     * @return notification schedule.
     */
    private NotificationSchedule createEmailNotificaitonSchedule(final String email) {
        //create user
        final User user = new User();
        user.setCompany(company.getCompanyId());
        user.setPassword("password");
        user.setEmail(email);
        user.setActive(true);
        user.setLanguage(Language.English);
        user.setTimeZone(TimeZone.getDefault());
        user.setFirstName("Arrival");
        user.setLastName("Developer");
        context.getBean(UserDao.class).save(user);

        //create notification schedule
        final PersonSchedule schedule = new PersonSchedule();
        schedule.setSendEmail(true);
        schedule.setFromTime(0);
        Arrays.fill(schedule.getWeekDays(), true);
        schedule.setToTime(60 * 24 - 1);
        schedule.setUser(user);

        final NotificationSchedule s = new NotificationSchedule();
        s.setName("Arrival Notification");
        s.getSchedules().add(schedule);
        s.setCompany(company.getCompanyId());
        return context.getBean(NotificationScheduleDao.class).save(s);
    }
    @After
    public void tearDown() {
        context.getBean(MockEmailService.class).clear();
    }
}
