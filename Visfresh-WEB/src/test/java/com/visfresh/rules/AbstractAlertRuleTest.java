/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractAlertRuleTest extends AbstractAlertRule {

    private Shipment shipment;
    private TrackerEvent event;
    private long lastID;
    private List<Alert> alerts = new LinkedList<>();
    private Map<Long, NotificationIssue> notifications= new HashMap<>();

    /**
     * Default constructor.
     */
    public AbstractAlertRuleTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractAlertRule#handleInternal(com.visfresh.rules.RuleContext)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        return alerts.toArray(new Alert[alerts.size()]);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractNotificationRule#getName()
     */
    @Override
    public String getName() {
        return "UnitTestForAbstractAlertRule";
    }

    @Before
    public void setUp() {
        final Company c = new Company();
        c.setId(7l);
        c.setName("JUnit company");

        this.shipment = new Shipment();
        shipment.setId(7l);
        shipment.setShipmentDate(new Date());
        shipment.setCompany(c.getCompanyId());

        this.event = new TrackerEvent();
        event.setTime(new Date());
        event.setShipment(shipment);
        event.setType(TrackerEventType.AUT);
    }

    @Test
    public void testAlertProfile() {
        final AlertProfile ap = new AlertProfile();
        shipment.setAlertProfile(ap);

        assertTrue(accept(new RuleContext(event, new SessionHolder(shipment))));

        shipment.setAlertProfile(null);
        assertFalse(accept(new RuleContext(event, new SessionHolder(shipment))));
    }
    @Test
    public void testNoAlertsAfterArrivalMinutes() {
        final AlertProfile ap = new AlertProfile();
        shipment.setAlertProfile(ap);

        final Integer minutes = 10;
        shipment.setNoAlertsAfterArrivalMinutes(minutes);

        shipment.setArrivalDate(new Date(event.getTime().getTime() - (minutes + 1) * 60 * 1000l));
        assertTrue(accept(new RuleContext(event, new SessionHolder(shipment))));

        shipment.setStatus(ShipmentStatus.Arrived);
        assertFalse(accept(new RuleContext(event, new SessionHolder(shipment))));
    }
    @Test
    public void testNoAlertsAfterStartMinutes() {
        final Integer minutes = 10;
        shipment.setAlertProfile(new AlertProfile());
        shipment.setNoAlertsAfterStartMinutes(minutes);

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L - 1l));
        assertFalse(accept(new RuleContext(event, new SessionHolder(shipment))));

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L + 1l));
        assertTrue(accept(new RuleContext(event, new SessionHolder(shipment))));
    }
    @Test
    public void testSuppressAlertsAfterStartMinutes() {
        final int minutes = 10;
        shipment.setAlertProfile(new AlertProfile());
        shipment.setAlertSuppressionMinutes(minutes);

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L - 1l));
        assertTrue(accept(new RuleContext(event, new SessionHolder(shipment))));

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L + 1l));
        assertFalse(accept(new RuleContext(event, new SessionHolder(shipment))));
    }
    @Test
    public void testAlertsSuppressedBySession() {
        shipment.setAlertProfile(new AlertProfile());

        final SessionHolder mgr = new SessionHolder(shipment);
        mgr.getSession(shipment).setAlertsSuppressed(true);

        assertFalse(accept(new RuleContext(event, mgr)));
    }
    @Test
    public void testNotifyAllUsers() {
        final User u1 = createUser();
        final User u2 = createUser();

        final NotificationSchedule sched = createNotificationSchedule(u1, u2);

        shipment.setAlertProfile(new AlertProfile());
        shipment.getAlertsNotificationSchedules().add(sched);

        alerts.add(new Alert());
        assertFalse(handle(new RuleContext(event, new SessionHolder(shipment))));
        assertEquals(2, notifications.size());
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractNotificationRule#sendNotification(com.visfresh.entities.PersonSchedule, com.visfresh.entities.NotificationIssue, com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void sendNotification(final List<PersonSchedule> schedules, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        for (final PersonSchedule s : schedules) {
            notifications.put(s.getId(), issue);
        }
    }
    /**
     * @param users
     * @return
     */
    private NotificationSchedule createNotificationSchedule(final User... users) {
        final NotificationSchedule s = new NotificationSchedule();
        s.setId(lastID++);
        s.setCompany(shipment.getCompanyId());
        s.setName("Junit-" + s.getId());

        for (final User u : users) {
            final PersonSchedule ps = new PersonSchedule();
            ps.setId(lastID++);
            ps.setSendEmail(u.getEmail() != null);
            ps.setSendSms(u.getPhone() != null);
            ps.setSendApp(false);
            for (int i = 0; i < ps.getWeekDays().length; i++) {
                ps.getWeekDays()[i] = true;
            }
            ps.setFromTime(0);
            ps.setToTime(24 * 60 - 1);
            ps.setUser(u);

            s.getSchedules().add(ps);
        }
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractAlertRule#saveAlert(com.visfresh.entities.Alert)
     */
    @Override
    protected void saveAlert(final Alert a) {
        // not save
    }
    /**
     * @return
     */
    protected User createUser() {
        final User user = new User();
        user.setId(lastID++);
        user.setEmail(user.getId() + "-user@junit.com");
        user.setPhone(Long.toString(11111111111l + user.getId()));
        user.setActive(true);
        user.setCompany(shipment.getCompanyId());
        user.setPassword("");
        return user;
    }
}
