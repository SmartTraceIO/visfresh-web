/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.ShipmentAuditConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.controllers.restclient.ShipmentAuditRestClient;
import com.visfresh.dao.ShipmentAuditDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditsControllerTest extends AbstractRestServiceTest {
    private ShipmentAuditDao dao;
    private User user;
    private ShipmentAuditRestClient client;

    /**
     * Default constructor.
     */
    public ShipmentAuditsControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(ShipmentAuditDao.class);

        final String token = login();
        this.user = context.getBean(AuthService.class).getUserForToken(token);
        client = new ShipmentAuditRestClient(user);

        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
    }

    @Test
    public void testGetAuditsBySorting() throws IOException, RestServiceException {
        final Shipment s1 = createShipment(true);
        createShipment(true);

        createItem(null, s1);
        createItem(null, s1);

        final List<ShipmentAuditItem> audits = client.getAudits(
                null, s1, 1, 100, ShipmentAuditConstants.SHIPMENT_ID, "asc");
        assertEquals(2, audits.size());
    }
    @Test
    public void testGetAuditsByUser() throws IOException, RestServiceException {
        final User u1 = createUser1();
        createItem(u1, createShipment(true));
        createItem(u1, createShipment(true));
        createItem(createUser2(), createShipment(true));

        final List<ShipmentAuditItem> audits = client.getAudits(
                u1, null, 1, 100, ShipmentAuditConstants.USER_ID, "asc");
        assertEquals(2, audits.size());
    }
    @Test
    public void testGetAuditsByShipment() throws IOException, RestServiceException {
        final Shipment s1 = createShipment(true);
        createShipment(true);

        createItem(null, s1);
        createItem(null, s1);

        final List<ShipmentAuditItem> audits = client.getAudits(
                null, s1, 1, 100, ShipmentAuditConstants.SHIPMENT_ID, "asc");
        assertEquals(2, audits.size());
    }

    private ShipmentAuditItem createItem(final User user, final Shipment shipment) {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setAction(ShipmentAuditAction.Autocreated);
        item.setShipmentId(shipment.getId());
        item.setTime(new Date(System.currentTimeMillis() - 1000000000l));
        if (user != null) {
            item.setUserId(user.getId());
        }
        item.getAdditionalInfo().put("key1", "value1");
        item.getAdditionalInfo().put("key2", "value2");
        return dao.save(item);
    }
}
