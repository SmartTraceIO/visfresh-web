/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.NotificationConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.entities.UserNotification;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationDaoTest extends BaseCrudTest<NotificationDao, Notification, Notification, Long> {
    private DeviceDao deviceDao;
    private UserDao userDao;
    private ShipmentDao shipmentDao;

    private Device device;
    private ArrivalDao arrivalDao;
    private Arrival arrival;
    private User user;

    /**
     * Default constructor.
     */
    public NotificationDaoTest() {
        super(NotificationDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);
        userDao = getContext().getBean(UserDao.class);
        shipmentDao = getContext().getBean(ShipmentDao.class);
        arrivalDao = getContext().getBean(ArrivalDao.class);

        //create device
        final Device d = new Device();
        d.setCompany(sharedCompany.getCompanyId());
        final String imei = "932487032487";
        d.setImei(imei);
        d.setName("Test Device");
        d.setDescription("JUnit device");

        this.device = deviceDao.save(d);

        //shipped from
        final LocationProfile sfrom = new LocationProfile();
        sfrom.setAddress("Unit test start location");
        sfrom.setName("JUnit from");
        sfrom.setCompany(sharedCompany.getId());
        sfrom.getLocation().setLatitude(1.);
        sfrom.getLocation().setLongitude(1.);
        context.getBean(LocationProfileDao.class).save(sfrom);

        //shipped to
        final LocationProfile sto = new LocationProfile();
        sto.setAddress("Unit test end location");
        sto.setName("JUnit to");
        sto.setCompany(sharedCompany.getId());
        sto.getLocation().setLatitude(1.);
        sto.getLocation().setLongitude(1.);
        context.getBean(LocationProfileDao.class).save(sto);

        //alert profile
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany.getCompanyId());
        ap.setName("JUnit alerts");
        context.getBean(AlertProfileDao.class).save(ap);

        //create shipment
        Shipment s = new Shipment();
        s.setCompany(sharedCompany.getCompanyId());
        s.setShipmentDescription("JUnit shipment");
        s.setDevice(d);
        s.setShippedFrom(sfrom);
        s.setShippedTo(sto);
        s.setAlertProfile(ap);
        s = shipmentDao.save(s);

        //create arrival
        final Arrival a = new Arrival();
        a.setDate(new Date(System.currentTimeMillis() - 1000000l));
        a.setDevice(device);
        a.setShipment(s);
        a.setNumberOfMettersOfArrival(78);
        this.arrival = arrivalDao.save(a);

        //create User
        final User u = new User();
        u.setCompany(sharedCompany.getCompanyId());
        u.setEmail("asuvorov@google.com");
        u.setFirstName("Alexande");
        u.setLastName("Suvorov");
        u.setPassword("alskdj");
        this.user = userDao.save(u);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Notification n) {
        assertTrue(n.getIssue() instanceof Arrival);
        assertNotNull(n.getUser());
        assertEquals(NotificationType.Arrival, n.getType());
        assertTrue(n.isHidden());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Notification> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final Notification n = all.get(0);

        assertTrue(n.getIssue() instanceof Arrival);
        assertNotNull(n.getUser());
        assertEquals(NotificationType.Arrival, n.getType());
        assertTrue(n.isHidden());
    }

    @Test
    public void testFindForUser() {
        final User u = createUser();

        final Notification n1 = createNotification(user);
        dao.save(n1);
        final Notification n2 = createNotification(u);
        dao.save(n2);

        //crete light on alert
        final Notification n3 = new Notification();
        n3.setType(NotificationType.Alert);
        n3.setIssue(arrival);
        n3.setUser(u);

        final Alert lightOn = new Alert();
        lightOn.setType(AlertType.LightOn);
        lightOn.setDevice(arrival.getDevice());
        lightOn.setShipment(arrival.getShipment());
        context.getBean(AlertDao.class).save(lightOn);
        n3.setIssue(lightOn);
        dao.save(n3);

        //check result
        final List<UserNotification> list = dao.findForUser(u, false, null, null, null);
        assertEquals(2, list.size());
        assertEquals(n2.getId(), list.get(0).getId());

        assertEquals(1, dao.findForUser(u, true, null, null, null).size());
    }

    @Test
    public void testAlertFieldsOfFindForUser() {
        final User u = createUser();
        final Shipment s = arrival.getShipment();
        final AlertType alertType = AlertType.LightOn;
        final double temperature = 15.;
        final Date eventTime = new Date(System.currentTimeMillis() - 293487908l);
        final Date issueDate = new Date(System.currentTimeMillis() - 2039845732l);
        final boolean hidden = true;
        final boolean read = true;

        //create tracker event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTemperature(temperature);
        e.setTime(eventTime);
        e.setDevice(s.getDevice());
        e.setShipment(e.getShipment());
        context.getBean(TrackerEventDao.class).save(e);

        final Alert alert = new Alert();
        alert.setType(alertType);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alert.setTrackerEventId(e.getId());
        alert.setDate(issueDate);
        context.getBean(AlertDao.class).save(alert);

        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setIssue(alert);
        n.setUser(u);
        n.setHidden(hidden);
        n.setRead(read);
        dao.save(n);

        //check result
        final UserNotification un = dao.findForUser(u, false, null, null, null).get(0);
        assertEquals(-1, un.getAlertMinutes().intValue()); //-1 default value
        assertNull(un.getAlertRuleTimeOutMinutes());
        assertEquals(alertType, un.getAlertType());
        assertEquals(s.getDevice().getImei(), un.getDevice());
        assertEqualsDates(eventTime, un.getEventTime());
        assertEquals(n.getId(), un.getId());
        assertEqualsDates(issueDate, un.getIssueDate());
        assertEquals(alert.getId(), un.getIssueId());
        assertEquals(0, un.getNumberOfMettersOfArrival());
        assertEquals(s.getShipmentDescription(), un.getShipmentDescription());
        assertEquals(s.getId(), un.getShipmentId());
        assertEquals(s.getTripCount(), un.getShipmentTripCount());
        assertEquals(s.getShippedFrom().getName(), un.getShippedFrom());
        assertEquals(s.getShippedTo().getName(), un.getShippedTo());
        assertEquals(-1., un.getTemperature().doubleValue(), 0.01); // default -1
        assertEquals(e.getId(), un.getTrackerEventId());
        assertEquals(n.getType(), un.getType());
        assertFalse(un.isAlertCumulative());
        assertEquals(hidden, un.isHidden());
        assertEquals(read, un.isRead());
    }
    @Test
    public void testTemperatureAlertFieldsOfFindForUser() {
        final User u = createUser();
        final Shipment s = arrival.getShipment();
        final AlertType alertType = AlertType.CriticalCold;
        final double temperature = 15.;
        final Date eventTime = new Date(System.currentTimeMillis() - 293487908l);
        final Date issueDate = new Date(System.currentTimeMillis() - 2039845732l);
        final boolean hidden = true;
        final boolean read = true;
        final int alertMinuts = 55;
        final int ruleTimeOut = 47;
        final boolean cumulative = true;

        //create tracker event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTemperature(temperature);
        e.setTime(eventTime);
        e.setDevice(s.getDevice());
        e.setShipment(e.getShipment());
        context.getBean(TrackerEventDao.class).save(e);

        //temperature rule
        final TemperatureRule rule = new TemperatureRule();
        rule.setTimeOutMinutes(ruleTimeOut);
        rule.setType(alertType);
        s.getAlertProfile().getAlertRules().add(rule);
        context.getBean(AlertProfileDao.class).save(s.getAlertProfile());

        //alert
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setType(alertType);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alert.setTrackerEventId(e.getId());
        alert.setTemperature(temperature);
        alert.setDate(issueDate);
        alert.setMinutes(alertMinuts);
        alert.setCumulative(cumulative);
        alert.setRuleId(rule.getId());
        context.getBean(AlertDao.class).save(alert);

        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setIssue(alert);
        n.setUser(u);
        n.setHidden(hidden);
        n.setRead(read);
        dao.save(n);

        //check result
        final UserNotification un = dao.findForUser(u, false, null, null, null).get(0);
        assertEquals(alertMinuts, un.getAlertMinutes().intValue());
        assertEquals(ruleTimeOut, un.getAlertRuleTimeOutMinutes().intValue());
        assertEquals(alertType, un.getAlertType());
        assertEquals(s.getDevice().getImei(), un.getDevice());
        assertEqualsDates(eventTime, un.getEventTime());
        assertEquals(n.getId(), un.getId());
        assertEqualsDates(issueDate, un.getIssueDate());
        assertEquals(alert.getId(), un.getIssueId());
        assertEquals(0, un.getNumberOfMettersOfArrival());
        assertEquals(s.getShipmentDescription(), un.getShipmentDescription());
        assertEquals(s.getId(), un.getShipmentId());
        assertEquals(s.getTripCount(), un.getShipmentTripCount());
        assertEquals(s.getShippedFrom().getName(), un.getShippedFrom());
        assertEquals(s.getShippedTo().getName(), un.getShippedTo());
        assertEquals(e.getTemperature(), un.getTemperature().doubleValue(), 0.01);
        assertEquals(e.getId(), un.getTrackerEventId());
        assertEquals(n.getType(), un.getType());
        assertEquals(cumulative, un.isAlertCumulative());
        assertEquals(hidden, un.isHidden());
        assertEquals(read, un.isRead());
    }
    @Test
    public void testArrivalFieldsOfFindForUser() {
        final User u = createUser();
        final Shipment s = arrival.getShipment();
        final double temperature = 15.;
        final Date eventTime = new Date(System.currentTimeMillis() - 293487908l);
        final Date issueDate = new Date(System.currentTimeMillis() - 2039845732l);
        final boolean hidden = true;
        final boolean read = true;

        //create tracker event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTemperature(temperature);
        e.setTime(eventTime);
        e.setDevice(s.getDevice());
        e.setShipment(e.getShipment());
        context.getBean(TrackerEventDao.class).save(e);

        //alert
        final Arrival alert = new Arrival();
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alert.setTrackerEventId(e.getId());
        alert.setDate(issueDate);
        context.getBean(ArrivalDao.class).save(alert);

        final Notification n = new Notification();
        n.setType(NotificationType.Arrival);
        n.setIssue(alert);
        n.setUser(u);
        n.setHidden(hidden);
        n.setRead(read);
        dao.save(n);

        //check result
        final UserNotification un = dao.findForUser(u, false, null, null, null).get(0);
        assertNull(un.getAlertMinutes());
        assertNull(un.getAlertRuleTimeOutMinutes());
        assertNull(un.getAlertType());
        assertEquals(s.getDevice().getImei(), un.getDevice());
        assertEqualsDates(eventTime, un.getEventTime());
        assertEquals(n.getId(), un.getId());
        assertEqualsDates(issueDate, un.getIssueDate());
        assertEquals(alert.getId(), un.getIssueId());
        assertEquals(0, un.getNumberOfMettersOfArrival());
        assertEquals(s.getShipmentDescription(), un.getShipmentDescription());
        assertEquals(s.getId(), un.getShipmentId());
        assertEquals(s.getTripCount(), un.getShipmentTripCount());
        assertEquals(s.getShippedFrom().getName(), un.getShippedFrom());
        assertEquals(s.getShippedTo().getName(), un.getShippedTo());
        assertNull(un.getTemperature());
        assertEquals(e.getId(), un.getTrackerEventId());
        assertEquals(n.getType(), un.getType());
        assertFalse(un.isAlertCumulative());
        assertEquals(hidden, un.isHidden());
        assertEquals(read, un.isRead());
    }
    @Test
    public void testAlertFieldsOfFindForUserNotRule() {
        final User u = createUser();
        final Shipment s = arrival.getShipment();
        final AlertType alertType = AlertType.CriticalCold;
        final double temperature = 15.;
        final Date eventTime = new Date(System.currentTimeMillis() - 293487908l);
        final Date issueDate = new Date(System.currentTimeMillis() - 2039845732l);
        final boolean hidden = true;
        final boolean read = true;
        final int alertMinuts = 55;
        final boolean cumulative = true;

        //create tracker event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTemperature(temperature);
        e.setTime(eventTime);
        e.setDevice(s.getDevice());
        e.setShipment(e.getShipment());
        context.getBean(TrackerEventDao.class).save(e);

        //alert
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setType(alertType);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alert.setTemperature(temperature);
        alert.setTrackerEventId(e.getId());
        alert.setDate(issueDate);
        alert.setMinutes(alertMinuts);
        alert.setCumulative(cumulative);
        context.getBean(AlertDao.class).save(alert);

        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setIssue(alert);
        n.setUser(u);
        n.setHidden(hidden);
        n.setRead(read);
        dao.save(n);

        //check result
        final UserNotification un = dao.findForUser(u, false, null, null, null).get(0);
        assertEquals(alertMinuts, un.getAlertMinutes().intValue());
        assertNull(un.getAlertRuleTimeOutMinutes());
        assertEquals(alertType, un.getAlertType());
        assertEquals(s.getDevice().getImei(), un.getDevice());
        assertEqualsDates(eventTime, un.getEventTime());
        assertEquals(n.getId(), un.getId());
        assertEqualsDates(issueDate, un.getIssueDate());
        assertEquals(alert.getId(), un.getIssueId());
        assertEquals(0, un.getNumberOfMettersOfArrival());
        assertEquals(s.getShipmentDescription(), un.getShipmentDescription());
        assertEquals(s.getId(), un.getShipmentId());
        assertEquals(s.getTripCount(), un.getShipmentTripCount());
        assertEquals(s.getShippedFrom().getName(), un.getShippedFrom());
        assertEquals(s.getShippedTo().getName(), un.getShippedTo());
        assertEquals(e.getTemperature(), un.getTemperature().doubleValue(), 0.01);
        assertEquals(e.getId(), un.getTrackerEventId());
        assertEquals(n.getType(), un.getType());
        assertEquals(cumulative, un.isAlertCumulative());
        assertEquals(hidden, un.isHidden());
        assertEquals(read, un.isRead());
    }
    @Test
    public void testFindForUserEntityCount() {
        final User u = createUser();

        final Notification n1 = createNotification(user);
        dao.save(n1);
        final Notification n2 = createNotification(u);
        dao.save(n2);

        //crete light on alert
        final Notification n3 = new Notification();
        n3.setType(NotificationType.Alert);
        n3.setIssue(arrival);
        n3.setUser(u);

        final Alert lightOn = new Alert();
        lightOn.setType(AlertType.LightOn);
        lightOn.setDevice(arrival.getDevice());
        lightOn.setShipment(arrival.getShipment());
        context.getBean(AlertDao.class).save(lightOn);
        n3.setIssue(lightOn);
        dao.save(n3);

        //check result
        assertEquals(2, dao.getEntityCount(u, false, null));
        assertEquals(1, dao.getEntityCount(u, true, null));
    }

    @Test
    public void testMarkAsReadenByUserAndId() {
        final User u = createUser();

        final Notification n1 = createNotification(user);
        dao.save(n1);
        final Notification n2 = createNotification(u);
        dao.save(n2);
        final Notification n3 = createNotification(u);
        dao.save(n3);

        final Set<Long> ids = new HashSet<Long>();
        ids.add(n1.getId());
        dao.markAsReadenByUserAndId(u, ids);

        assertEquals(3, dao.findAll(null, null, null).size());

        ids.add(n2.getId());
        dao.markAsReadenByUserAndId(u, ids);

        assertEquals(3, dao.findAll(null, null, null).size());

        final Filter filter = new Filter();
        filter.addFilter(NotificationConstants.PROPERTY_CLOSED, Boolean.FALSE);
        assertEquals(2, dao.findAll(filter, null, null).size());
    }
    @Test
    public void testGetForIssues() {
        final User u = createUser();

        final Alert a1 = createAlert();
        final Alert a2 = createAlert();
        final Alert a3 = createAlert();

        createNotification(user, a1);
        createNotification(user, a2);
        createNotification(user, a3);

        final Set<Long> ids = new HashSet<Long>();
        ids.add(a1.getId());
        ids.add(a3.getId());

        assertEquals(2, dao.getForIssues(ids, NotificationType.Alert).size());
        assertEquals(0, dao.getForIssues(ids, NotificationType.Arrival).size());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Notification createTestEntity() {
        final Notification n = createNotification(user);
        n.setHidden(true);
        return n;
    }

    /**
     * @param u user.
     * @return notification.
     */
    private Notification createNotification(final User u) {
        final Notification n = new Notification();
        n.setType(NotificationType.Arrival);
        n.setIssue(arrival);
        n.setUser(u);
        return n;
    }
    /**
     * @param u user.
     * @return notification.
     */
    private Notification createNotification(final User u, final Alert a) {
        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setIssue(a);
        n.setUser(u);
        return dao.save(n);
    }
    private Alert createAlert() {
        final Alert alert = new Alert();
        alert.setType(AlertType.LightOn);
        alert.setDevice(arrival.getDevice());
        alert.setShipment(arrival.getShipment());
        return context.getBean(AlertDao.class).save(alert);
    }
    /**
     * @return
     */
    protected User createUser() {
        User u = new User();
        u.setCompany(sharedCompany.getCompanyId());
        u.setEmail("mkutuzov@google.com");
        u.setFirstName("Michael");
        u.setLastName("Kutuzov");
        u.setPassword("alskdj");
        u = userDao.save(u);
        return u;
    }
    /**
     * @param d1 first date.
     * @param d2 second date.
     */
    private void assertEqualsDates(final Date d1, final Date d2) {
        assertTrue(Math.abs(d1.getTime() - d2.getTime()) < 500l);
    }
}
