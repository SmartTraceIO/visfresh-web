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
        d.setSn("12345");

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

        final List<Notification> list = dao.findForUser(u, null, null, null);
        assertEquals(1, list.size());
        assertEquals(n2.getId(), list.get(0).getId());
    }
    @Test
    public void testDeleteByUserAndId() {
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
        assertEquals(2, dao.findAll(null, null, null).size());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Notification createTestEntity() {
        return createNotification(user);
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
}
