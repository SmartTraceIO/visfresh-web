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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.mock.MockEmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BatteryLowAlertRuleTest extends BaseRuleTest {
    private final int batteryOk = BatteryLowAlertRule.LOW_BATTERY_LIMIT + 10;
    private final int batteryLow = BatteryLowAlertRule.LOW_BATTERY_LIMIT - 1;
    private TrackerEventRule rule;
    private Device device;

    /**
     * Default constructor.
     */
    public BatteryLowAlertRuleTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        this.rule = engine.getRule(BatteryLowAlertRule.NAME);
        this.device = createDevice("90324870987");
    }
    @After
    public void tearDown() {
        context.getBean(MockEmailService.class).clear();
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param date date.
     * @return event.
     */
    private TrackerEvent createEvent(final int battery) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setLatitude(25.);
        e.setLongitude(25.);
        e.setTemperature(20.4);
        e.setType(TrackerEventType.AUT);
        e.setDevice(device);
        e.setTime(new Date());
        return context.getBean(TrackerEventDao.class).save(e);
    }
    @Test
    public void testAccept() {
        final TrackerEvent e = createEvent(batteryLow);

        //ignores without shipment
        assertFalse(rule.accept(new RuleContext(e, new SessionHolder())));

        //ignore with battery ok.
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice(), e.getTime());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        e.setBattery(batteryOk);
        assertFalse(rule.accept(new RuleContext(e, new SessionHolder())));

        //accept with battery low.
        e.setBattery(batteryLow);
        assertTrue(rule.accept(new RuleContext(e, new SessionHolder())));

        //set alwais accepted even if handled
        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);
        assertTrue(rule.accept(new RuleContext(e, new SessionHolder())));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(batteryLow);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice(), e.getTime());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        final RuleContext c = new RuleContext(e, new SessionHolder());
        rule.handle(c);

        //check shipment created.
        final List<Alert> alerts = context.getBean(AlertDao.class).findAll(null, null, null);
        assertEquals(1, alerts.size());

        final Alert alert = alerts.get(0);
        assertEquals(AlertType.Battery, alert.getType());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
        assertEquals(e.getId(), alert.getTrackerEventId());
    }

    @Test
    public void testHandleOnce() {
        final TrackerEvent e = createEvent(batteryLow);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice(), e.getTime());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        final SessionHolder mgr = new SessionHolder();
        rule.handle(new RuleContext(e, mgr));

        //check shipment created.
        assertTrue(mgr.getSession(s).isBatteryLowProcessed());
        assertFalse(rule.accept(new RuleContext(e, mgr)));

        mgr.getSession(s).setBatteryLowProcessed(false);
        assertTrue(rule.accept(new RuleContext(e, mgr)));
    }
    @Test
    public void testSendNotificationToCompanyEmail() {
        final TrackerEvent e = createEvent(batteryLow);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice(), e.getTime());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        final SessionHolder mgr = new SessionHolder();
        rule.handle(new RuleContext(e, mgr));

        //check notification sent
        final List<EmailMessage> emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(1, emails.size());

        final EmailMessage email = emails.get(0);
        assertEquals(company.getEmail(), email.getEmails()[0]);
    }

    @Test
    public void testSendNotificationToPersonSchedule() {
        final TrackerEvent e = createEvent(batteryLow);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice(), e.getTime());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        //configure notification schedule
        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(company);
        sched.setDescription("JUnit");
        sched.setName("JUnit");
        s.getAlertsNotificationSchedules().add(sched);

        final PersonSchedule ps = new PersonSchedule();
        ps.setFromTime(1);
        ps.setSendEmail(true);
        ps.setToTime(24 * 60 - 1);
        Arrays.fill(ps.getWeekDays(), true);

        final User user = new User();
        user.setCompany(company);
        user.setActive(true);
        user.setEmail("abra@kada.bra");
        user.setLanguage(Language.English);
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setMeasurementUnits(MeasurementUnits.Metric);
        user.setTemperatureUnits(TemperatureUnits.Celsius);
        user.setTimeZone(TimeZone.getTimeZone("UTC"));

        context.getBean(UserDao.class).save(user);

        ps.setUser(user);
        sched.getSchedules().add(ps);
        context.getBean(NotificationScheduleDao.class).save(sched);
        context.getBean(ShipmentDao.class).save(s);

        final SessionHolder mgr = new SessionHolder();
        rule.handle(new RuleContext(e, mgr));

        //check notification sent
        final List<EmailMessage> emails = context.getBean(MockEmailService.class).getMessages();
        assertEquals(1, emails.size());

        final EmailMessage email = emails.get(0);
        assertEquals(user.getEmail(), email.getEmails()[0]);
    }

    /**
     * @param d device.
     * @return shipment by enabled bettery low alert.
     */
    private Shipment createShipmentWithEnabledAlert(final Device d, final Date shipmentDate) {
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress, d);
        s.setShipmentDate(shipmentDate);
        //enable battery low alerts
        final AlertProfile p = new AlertProfile();
        p.setName("BatteryLow alert rule");
        p.setCompany(s.getCompany());
        p.setWatchBatteryLow(true);
        context.getBean(AlertProfileDao.class).save(p);

        s.setAlertProfile(p);
        context.getBean(ShipmentDao.class).save(s);

        return s;
    }
}
