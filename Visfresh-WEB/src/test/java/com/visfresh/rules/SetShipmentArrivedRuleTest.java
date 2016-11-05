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
    public void testNotAcceptNotLeavingStartLocation() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, createSessionHolder(false));

        shipment.setShippedTo(createLocation());
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
        rule.accept(req);
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

        final TrackerEvent e1 = createEventNearLocation(loc);
        e1.setTime(new Date(e.getTime().getTime() - 30 * 60 * 1000l));

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, h);
        assertTrue(rule.accept(req));
        rule.handle(req);

        assertEquals(ShipmentStatus.Arrived, shipment.getStatus());
        assertNotNull(shipment.getArrivalDate());
    }
    @Test
    public void testSendReport() {
        shipment.setArrivalNotificationWithinKm(0);

        final String email = "arrival.developer@visfresh.com";
        final NotificationSchedule sched = createEmailNotificaitonSchedule(email);

        shipment.getArrivalNotificationSchedules().add(sched);

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
        sessionDao.saveSession(shipment, session);

        final TrackerEvent e1 = createEventNearLocation(loc);
        e1.setTime(new Date(e.getTime().getTime() - 30 * 60 * 1000l));

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, h);
        assertTrue(rule.accept(req));
        rule.handle(req);

        final MockEmailService emailer = context.getBean(MockEmailService.class);
        //check notification send
        final List<EmailMessage> emails = emailer.getMessages();
        assertEquals(1, emails.size());
        assertEquals(1, emailer.getAttachments().size());
        assertNotNull(emailer.getAttachments().get(0));

        final EmailMessage msg = emails.get(0);
        assertEquals(1, msg.getEmails().length);
        assertEquals(email, msg.getEmails()[0]);
        assertTrue(msg.getMessage().contains(shipment.getDevice().getSn()));

        //check not send report if already notified.
        emailer.clear();

        rule.handle(req);
        sessionDao.saveSession(shipment, sessionDao.getSession(shipment));

        assertEquals(0, emailer.getMessages().size());
        assertEquals(0, emailer.getAttachments().size());
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
        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
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
        user.setCompany(company);
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
        s.setCompany(company);
        return context.getBean(NotificationScheduleDao.class).save(s);
    }
}
