/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Language;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockShipmentShutdownService;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.rules.state.ShipmentSessionManager;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SetShipmentArrivedRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public SetShipmentArrivedRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = context.getBean(RuleEngine.class).getRule(SetShipmentArrivedRule.NAME);
        shipment = createDefaultShipment(ShipmentStatus.InProgress, createDevice("9283470987"));
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
    public void testAcceptInEnteringControl() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final LocationProfile loc = createLocation();
        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder h = createSessionHolder(true);

        //so far
        e.setLatitude(0.);
        e.setLongitude(0.);
        assertFalse(rule.accept(new RuleContext(e, h)));

        //set nearest location
        e.setLatitude(loc.getLocation().getLatitude());
        e.setLongitude(loc.getLocation().getLongitude());
        rule.handle(new RuleContext(e, h));

        e.setLatitude(0.);
        e.setLongitude(0.);
        assertTrue(rule.accept(new RuleContext(e, h)));
    }
    @Test
    public void testClearInEnteringControl() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final LocationProfile loc = createLocation();
        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder h = createSessionHolder(true);

        //so far
        e.setLatitude(loc.getLocation().getLatitude());
        e.setLongitude(loc.getLocation().getLongitude());
        rule.handle(new RuleContext(e, h));

        e.setLatitude(0.);
        e.setLongitude(0.);
        rule.handle(new RuleContext(e, h));
        assertFalse(rule.accept(new RuleContext(e, h)));
    }
    @Test
    public void testAcceptNullStartLocation() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, createSessionHolder(false));

        shipment.setShippedTo(createLocation(10., 10.));
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        e.setLatitude(10.);
        e.setLongitude(10.);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testNotAcceptNotLeavingStartLocation() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, createSessionHolder(false));

        shipment.setShippedFrom(createLocation(10, 10));
        shipment.setShippedTo(createLocation(11, 11));
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        e.setLatitude(10.);
        e.setLongitude(10.);
        assertFalse(rule.accept(req));
    }
    @Test
    public void testShutdownDevice() {
        shipment.setArrivalNotificationWithinKm(1);
        shipment.setShutdownDeviceAfterMinutes(11);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder h = createSessionHolder(true);

        final TrackerEvent e1 = createEventNearLocation(loc);
        e1.setTime(new Date(e.getTime().getTime() - 30 * 60 * 1000l));

        //set nearest location
        final RuleContext req = new RuleContext(e, h);
        rule.handle(req);
        rule.handle(req);

        //check shipment shutdown request has sent
        final Date date = context.getBean(MockShipmentShutdownService.class).getShutdownDate(shipment.getId());
        assertNotNull(date);
    }
    @Test
    public void testHandle() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);
        final SessionHolder h = createSessionHolder(true);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        rule.handle(new RuleContext(e, h));
        assertEquals(ShipmentStatus.InProgress, shipment.getStatus());

        rule.handle(new RuleContext(e, h));
        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());

        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testHandleStpImmediatelly() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);
        e.setType(TrackerEventType.STP);

        final SessionHolder h = createSessionHolder(true);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        rule.handle(new RuleContext(e, h));
        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());

        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testHandleAutodetectedImediatelly() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);
        e.setType(TrackerEventType.AUT);

        final SessionHolder h = createSessionHolder(true);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final RuleContext ctxt = new RuleContext(e, h);
        //mark given context as autodetected
        AutoDetectEndLocationRule.setAutodetected(ctxt);

        rule.handle(ctxt);
        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());

        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testHandleBrtImmediatelly() {
        shipment.setArrivalNotificationWithinKm(1);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);
        e.setType(TrackerEventType.BRT);

        final SessionHolder h = createSessionHolder(true);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        rule.handle(new RuleContext(e, h));
        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());

        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testSendReport() {
        shipment.setArrivalNotificationWithinKm(0);

        final String email1 = "arrival.developer@visfresh.com";
        final NotificationSchedule sched = createEmailNotificaitonSchedule(email1);

        shipment.getArrivalNotificationSchedules().add(sched);
        final String email2 = "arrival.developer@smarttrace.com.au";
        shipment.getArrivalNotificationSchedules().add(createEmailNotificaitonSchedule(email2));

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        //create session manager with peresistence
        final ShipmentSessionDao sessionDao = context.getBean(ShipmentSessionDao.class);
        final ShipmentSession session = new ShipmentSession(shipment.getId());
        final ShipmentSessionManager h = new ShipmentSessionManager() {
            @Override
            public ShipmentSession getSession(final Shipment s) {
                return session;
            }
        };
        LeaveStartLocationRule.setLeavingStartLocation(session);
        sessionDao.saveSession(session);

        final TrackerEvent e1 = createEventNearLocation(loc);
        e1.setTime(new Date(e.getTime().getTime() - 30 * 60 * 1000l));

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, h);
        assertTrue(rule.accept(req));
        rule.handle(req);
        rule.handle(req);

        final MockEmailService emailer = context.getBean(MockEmailService.class);
        //check notification send
        final List<EmailMessage> emails = emailer.getMessages();
        assertEquals(2, emails.size());
        assertEquals(2, emailer.getAttachments().size());
        assertNotNull(emailer.getAttachments().get(0));

        //first message
        EmailMessage msg = emails.get(0);
        assertEquals(1, msg.getEmails().length);
        assertEquals(email1, msg.getEmails()[0]);
        assertTrue(msg.getMessage().contains(shipment.getDevice().getSn()));

        //second message
        msg = emails.get(1);
        assertEquals(1, msg.getEmails().length);
        assertEquals(email2, msg.getEmails()[0]);

        //check not send report if already notified.
        emailer.clear();

        rule.handle(req);
        sessionDao.saveSession(sessionDao.getSession(shipment));

        assertEquals(0, emailer.getMessages().size());
        assertEquals(0, emailer.getAttachments().size());
    }
    @Test
    public void testNotSendReport() {
        shipment.setArrivalNotificationWithinKm(0);

        final NotificationSchedule sched = createEmailNotificaitonSchedule("arrival.developer@visfresh.com");
        shipment.getArrivalNotificationSchedules().add(sched);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        //create session manager with persistence
        final ShipmentSessionDao sessionDao = context.getBean(ShipmentSessionDao.class);
        final ShipmentSession session = new ShipmentSession(shipment.getId());
        final ShipmentSessionManager h = new ShipmentSessionManager() {
            @Override
            public ShipmentSession getSession(final Shipment s) {
                return session;
            }
        };
        LeaveStartLocationRule.setLeavingStartLocation(session);
        sessionDao.saveSession(session);

        final TrackerEvent e1 = createEventNearLocation(loc);
        e1.setTime(new Date(e.getTime().getTime() - 30 * 60 * 1000l));

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, h);
        assertTrue(rule.accept(req));
        shipment.setSendArrivalReport(false);
        rule.handle(req);
        rule.handle(req);

        final MockEmailService emailer = context.getBean(MockEmailService.class);
        //check notification send
        final List<EmailMessage> emails = emailer.getMessages();
        assertEquals(0, emails.size());
        assertEquals(0, emailer.getAttachments().size());

        emailer.clear();

        //check not send arrival report if alerts not triggered
        shipment.setSendArrivalReport(true);
        shipment.setSendArrivalReportOnlyIfAlerts(true);
        rule.handle(req);

        assertEquals(0, emailer.getMessages().size());
        assertEquals(0, emailer.getAttachments().size());

        //check send arrival
        shipment.setSendArrivalReportOnlyIfAlerts(false);
        rule.handle(req);

        assertEquals(1, emailer.getMessages().size());
        assertEquals(1, emailer.getAttachments().size());
    }
    @Test
    public void testPreventOfDoubleHandling() {
        shipment.setArrivalNotificationWithinKm(0);

        final LocationProfile loc = createLocation();
        final TrackerEvent e = createEventNearLocation(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        final SessionHolder mgr = createSessionHolder(true);
        //set nearest location
        final RuleContext req = new RuleContext(e, mgr);
        assertTrue(rule.accept(req));
        rule.handle(req);

        assertFalse(rule.accept(req));
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
        return e;
    }
    /**
     * @return location.
     */
    private LocationProfile createLocation() {
        return createLocation(10, 10);
    }

    /**
     * @param lat
     * @param lon
     * @return
     */
    protected LocationProfile createLocation(final double lat, final double lon) {
        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company.getCompanyId());
        loc.setName("Finish location");
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);
        loc = context.getBean(LocationProfileDao.class).save(loc);
        return loc;
    }
    /**
     * @param leaveStartLocation TODO
     * @return
     */
    private SessionHolder createSessionHolder(final boolean leaveStartLocation) {
        final SessionHolder h = new SessionHolder();
        if (leaveStartLocation) {
            LeaveStartLocationRule.setLeavingStartLocation(h.getSession(shipment));
        }
        return h;
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
}
