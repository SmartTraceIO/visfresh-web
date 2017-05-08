/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;

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
public class ShipmentAuditDaoTest extends BaseCrudTest<ShipmentAuditDao, ShipmentAuditItem, Long> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

    private Shipment shipment;
    private User user;

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
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);

        this.user = createUser("aa@bbb.c");
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

    /**
     * @param email user email.
     * @return
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(sharedCompany);
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
