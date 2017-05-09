/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.audit.CurrentSessionHolder;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentAuditServiceTest extends DefaultShipmentAuditService {
    private Company company;

    private List<ShipmentAuditItem> items = new LinkedList<>();
    private Shipment shipment1;
    private Shipment shipment2;

    private long lastId = 1l;
    private ThreadLocal<RestSession> currentSession;

    /**
     * Default constructor.
     */
    public DefaultShipmentAuditServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        //get thread local from session holder.
        new CurrentSessionHolder() {
            {
                DefaultShipmentAuditServiceTest.this.currentSession = CurrentSessionHolder.currentSession;
            }
        };

        //create company
        company = new Company(7777l);
        company.setName("JUnit");

        //create shipments
        shipment1 = createShipment(1l);
        shipment2 = createShipment(2l);
    }
    /**
     * @param id
     * @return
     */
    private Shipment createShipment(final long id) {
        final Device d = new Device();
        d.setImei(Long.toString(92384709111111111l + id));
        d.setName("Test Device");
        d.setCompany(company);
        d.setDescription("Test device");
        d.setTripCount(5);

        final Shipment s = new Shipment();
        s.setId(id);
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);

        return s;
    }

    @After
    public void tearDown() {
        //clear current session
        currentSession.set(null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultShipmentAuditService#save(com.visfresh.entities.ShipmentAuditItem)
     */
    @Override
    protected void save(final ShipmentAuditItem item) {
        items.add(item);
    }

    @Test
    public void testUsualAction() {
        final User user = createUser("a@b.c");
        createCurrentSession(user);

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ManyallyCreated, null);
        assertEquals(1, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ManyallyCreated, null);
        assertEquals(2, this.items.size());

    }
    @Test
    public void testWithoutUser() {
        handleShipmentAction(shipment1, null, ShipmentAuditAction.LoadedForEdit, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.ManyallyCreated, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.ManyallyCreatedFromAutostart, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.SuppressedAlerts, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.Updated, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.Viewed, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.ViewedLite, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, null, ShipmentAuditAction.Autocreated, null);
        assertEquals(1, this.items.size());
    }
    @Test
    public void testSingleShipmentAction() {
        final User user = createUser("a@b.c");
        createCurrentSession(user);

        handleShipmentAction(shipment1, user, ShipmentAuditAction.Viewed, null);
        assertEquals(1, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.Viewed, null);
        assertEquals(1, this.items.size());

        handleShipmentAction(shipment2, user, ShipmentAuditAction.Viewed, null);
        assertEquals(2, this.items.size());
    }
    @Test
    public void testSingleShipmentLiteAction() {
        final User user = createUser("a@b.c");
        createCurrentSession(user);

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ViewedLite, null);
        assertEquals(1, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ViewedLite, null);
        assertEquals(1, this.items.size());

        handleShipmentAction(shipment2, user, ShipmentAuditAction.ViewedLite, null);
        assertEquals(2, this.items.size());
    }
    @Test
    public void testSingleShipmentActionWithoutSession() {
        final User user = createUser("a@b.c");

        handleShipmentAction(shipment1, user, ShipmentAuditAction.LoadedForEdit, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ManyallyCreated, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ManyallyCreatedFromAutostart, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.SuppressedAlerts, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.Updated, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.Viewed, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.ViewedLite, null);
        assertEquals(0, this.items.size());

        handleShipmentAction(shipment1, user, ShipmentAuditAction.Autocreated, null);
        assertEquals(1, this.items.size());
    }

    /**
     * @param email user's email.
     * @return user instance.
     */
    private User createUser(final String email) {
        final User user = new User();
        user.setId(lastId++);
        user.setEmail(email);
        user.setActive(true);
        return user;
    }
    private RestSession createCurrentSession(final User u) {
        final RestSession s = new RestSession();
        s.setId(lastId ++);
        s.setUser(u);

        s.setToken(new AuthToken("111000222-" + s.getId()));

        currentSession.set(s);
        return s;
    }
}
