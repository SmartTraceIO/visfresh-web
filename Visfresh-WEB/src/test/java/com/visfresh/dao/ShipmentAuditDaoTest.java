/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.ShipmentAuditConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditDaoTest extends BaseCrudTest<ShipmentAuditDao, ShipmentAuditItem, ShipmentAuditItem, Long> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

    private Shipment shipment;
    private User user;

    private Device device;

    /**
     * Default constructor.
     */
    public ShipmentAuditDaoTest() {
        super(ShipmentAuditDao.class);
    }

    @Before
    public void setUp() {
        Device d = new Device();
        d.setImei("9238470983274987");
        d.setName("Test Device");
        d.setCompany(sharedCompany.getCompanyId());
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);
        this.device = d;

        this.shipment = createShipment();
        this.user = createUser("aa@bbb.c");
    }
    @After
    public void tearDown() {
        context.getBean(ShipmentAuditDao.class).deleteAll();
    }

    /**
     * @return shipment.
     */
    protected Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(device.getCompanyId());
        s.setStatus(ShipmentStatus.Arrived);
        return getContext().getBean(ShipmentDao.class).save(s);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected ShipmentAuditItem createTestEntity() {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setAction(ShipmentAuditAction.Autocreated);
        item.setShipmentId(shipment.getId());
        item.setTime(parseDate("2000-05-11T11-11-11"));
        item.setUserId(user.getId());
        item.getAdditionalInfo().put("key1", "value1");
        item.getAdditionalInfo().put("key2", "value2");
        return item;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final ShipmentAuditItem c) {
        assertEquals(ShipmentAuditAction.Autocreated, c.getAction());
        assertEquals(shipment.getId().longValue(), c.getShipmentId());
        assertEquals("2000-05-11T11-11-11", dateFormat.format(c.getTime()));
        assertEquals(user.getId(), c.getUserId());
        assertEquals("value1", c.getAdditionalInfo().get("key1"));
        assertEquals("value2", c.getAdditionalInfo().get("key2"));
    }
    @Test
    public void testFindByUser() {
        final User u1 = createUser("u1@smarttrace.com.au");
        final User u2 = createUser("u2@smarttrace.com.au");

        createItem(u1, shipment);
        createItem(u1, shipment);

        Filter filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.USER_ID, u2.getId());

        assertEquals(0, dao.findAll(filter, null, null).size());

        filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.USER_ID, u1.getId());
        assertEquals(2, dao.findAll(filter, null, null).size());
    }
    @Test
    public void testFindByUserAndCompany() {
        final User user = createUser("u1@smarttrace.com.au");

        createItem(user, shipment);

        final Filter filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.USER_ID, user.getId());

        assertEquals(1, dao.findAll(user.getCompanyId(), filter, null, null).size());
        assertEquals(0, dao.findAll(createCompany("Other").getCompanyId(), filter, null, null).size());
    }
    @Test
    public void testFindByShipment() {
        final Shipment s1 = createShipment();
        final Shipment s2 = createShipment();

        createItem(null, s1);
        createItem(null, s1);

        Filter filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.SHIPMENT_ID, s2.getId());

        assertEquals(0, dao.findAll(filter, null, null).size());

        filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.SHIPMENT_ID, s1.getId());
        assertEquals(2, dao.findAll(filter, null, null).size());
    }
    @Test
    public void testFindByShipmentAndCompany() {
        final Shipment shipment = createShipment();
        createItem(null, shipment);

        final Filter filter = new Filter();
        filter.addFilter(ShipmentAuditConstants.SHIPMENT_ID, shipment.getId());

        assertEquals(1, dao.findAll(shipment.getCompanyId(), filter, null, null).size());
        assertEquals(0, dao.findAll(createCompany("Other").getCompanyId(), filter, null, null).size());
    }
    @Test
    public void testOrderById() {
        final ShipmentAuditItem item1 = createItem(null, shipment);
        final ShipmentAuditItem item2 = createItem(null, shipment);
        final ShipmentAuditItem item3 = createItem(null, shipment);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.ID), null);

        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item3.getId(), items.get(2).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(false, ShipmentAuditConstants.ID), null);

        assertEquals(item3.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item1.getId(), items.get(2).getId());
    }
    @Test
    public void testOrderByAction() {
        final ShipmentAuditItem item2 = createItem(null, shipment);
        item2.setAction(ShipmentAuditAction.Autocreated);
        dao.save(item2);

        final ShipmentAuditItem item1 = createItem(null, shipment);
        item1.setAction(ShipmentAuditAction.AddedNote);
        dao.save(item1);

        final ShipmentAuditItem item3 = createItem(null, shipment);
        item3.setAction(ShipmentAuditAction.DeletedNote);
        dao.save(item3);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.ACTION), null);

        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item3.getId(), items.get(2).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(false, ShipmentAuditConstants.ACTION), null);

        assertEquals(item3.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item1.getId(), items.get(2).getId());
    }

    @Test
    public void testOrderByUser() {
        final User u1 = createUser("u1@smarttrace.com.au");
        final User u2 = createUser("u2@smarttrace.com.au");
        final User u3 = createUser("u3@smarttrace.com.au");

        final ShipmentAuditItem item2 = createItem(u2, shipment);
        final ShipmentAuditItem item1 = createItem(u1, shipment);
        final ShipmentAuditItem item3 = createItem(u3, shipment);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.USER_ID), null);

        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item3.getId(), items.get(2).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(false, ShipmentAuditConstants.USER_ID), null);

        assertEquals(item3.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item1.getId(), items.get(2).getId());
    }

    @Test
    public void testOrderByShipment() {
        final Shipment s1 = createShipment();
        final Shipment s2 = createShipment();
        final Shipment s3 = createShipment();

        final ShipmentAuditItem item2 = createItem(null, s2);
        final ShipmentAuditItem item1 = createItem(null, s1);
        final ShipmentAuditItem item3 = createItem(null, s3);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.SHIPMENT_ID), null);

        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item3.getId(), items.get(2).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(false, ShipmentAuditConstants.SHIPMENT_ID), null);

        assertEquals(item3.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item1.getId(), items.get(2).getId());
    }
    @Test
    public void testOrderByDate() {
        final Date d1 = parseDate("2000-05-11T11-11-11");
        final Date d2 = parseDate("2000-05-11T11-13-11");
        final Date d3 = parseDate("2000-05-11T11-15-11");

        final ShipmentAuditItem item2 = createItem(null, shipment);
        item2.setTime(d2);
        dao.save(item2);

        final ShipmentAuditItem item1 = createItem(null, shipment);
        item1.setTime(d1);
        dao.save(item1);

        final ShipmentAuditItem item3 = createItem(null, shipment);
        item3.setTime(d3);
        dao.save(item3);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.TIME), null);

        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item3.getId(), items.get(2).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(false, ShipmentAuditConstants.TIME), null);

        assertEquals(item3.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
        assertEquals(item1.getId(), items.get(2).getId());
    }

    private ShipmentAuditItem createItem(final User user, final Shipment shipment) {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setAction(ShipmentAuditAction.Autocreated);
        item.setShipmentId(shipment.getId());
        item.setTime(parseDate("2000-05-11T11-11-11"));
        if (user != null) {
            item.setUserId(user.getId());
        }
        item.getAdditionalInfo().put("key1", "value1");
        item.getAdditionalInfo().put("key2", "value2");
        return dao.save(item);
    }
    @Test
    public void testPaging() {
        final User u1 = createUser("u1@smarttrace.com.au");
        final User u2 = createUser("u2@smarttrace.com.au");
        final User u3 = createUser("u3@smarttrace.com.au");

        final ShipmentAuditItem item2 = createItem(u2, shipment);
        final ShipmentAuditItem item1 = createItem(u1, shipment);
        final ShipmentAuditItem item3 = createItem(u3, shipment);

        List<ShipmentAuditItem> items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.USER_ID), new Page(1, 1));
        assertEquals(item1.getId(), items.get(0).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.USER_ID), new Page(2, 1));
        assertEquals(item2.getId(), items.get(0).getId());

        items = dao.findAll(shipment.getCompanyId(),
                null, new Sorting(ShipmentAuditConstants.USER_ID), new Page(3, 1));
        assertEquals(item3.getId(), items.get(0).getId());
    }
    /**
     * @param email user email.
     * @return
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(sharedCompany.getCompanyId());
        u.setFirstName("FirstName");
        u.setLastName("LastName");
        u.setEmail(email);
        return context.getBean(UserDao.class).save(u);
    }

    /**
     * @param str
     * @return
     */
    private Date parseDate(final String str) {
        try {
            return dateFormat.parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
