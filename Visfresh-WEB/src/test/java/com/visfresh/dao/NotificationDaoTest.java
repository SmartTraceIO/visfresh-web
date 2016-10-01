/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.NotificationConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationDaoTest extends BaseCrudTest<NotificationDao, Notification, Long> {
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
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setName("Test Device");
        d.setDescription("JUnit device");

        this.device = deviceDao.save(d);

        //create arrival
        Shipment s = new Shipment();
        s.setCompany(sharedCompany);
        s.setDevice(d);
        s = shipmentDao.save(s);

        final Arrival a = new Arrival();
        a.setDate(new Date(System.currentTimeMillis() - 1000000l));
        a.setDevice(device);
        a.setShipment(s);
        a.setNumberOfMettersOfArrival(78);
        this.arrival = arrivalDao.save(a);

        //create User
        final User u = new User();
        u.setCompany(sharedCompany);
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
        User u = new User();
        u.setCompany(sharedCompany);
        u.setEmail("mkutuzov@google.com");
        u.setFirstName("Michael");
        u.setLastName("Kutuzov");
        u.setPassword("alskdj");
        u = userDao.save(u);

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
        final List<Notification> list = dao.findForUser(u, false, null, null, null);
        assertEquals(2, list.size());
        assertEquals(n2.getId(), list.get(0).getId());

        assertEquals(1, dao.findForUser(u, true, null, null, null).size());
    }
    @Test
    public void testFindForUserEntityCount() {
        User u = new User();
        u.setCompany(sharedCompany);
        u.setEmail("mkutuzov@google.com");
        u.setFirstName("Michael");
        u.setLastName("Kutuzov");
        u.setPassword("alskdj");
        u = userDao.save(u);

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
        User u = new User();
        u.setCompany(sharedCompany);
        u.setEmail("mkutuzov@google.com");
        u.setFirstName("Michael");
        u.setLastName("Kutuzov");
        u.setPassword("alskdj");
        u = userDao.save(u);

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
        //craete user
        User u = new User();
        u.setCompany(sharedCompany);
        u.setEmail("mkutuzov@google.com");
        u.setFirstName("Michael");
        u.setLastName("Kutuzov");
        u.setPassword("alskdj");
        u = userDao.save(u);

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
}
