/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.controllers.restclient.RestIoListener;
import com.visfresh.controllers.restclient.ShipmentTemplateRestClient;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.ShipmentTemplateDto;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateControllerTest extends AbstractRestServiceTest {
    private ShipmentTemplateDao dao;
    private ShipmentTemplateRestClient client = new ShipmentTemplateRestClient(UTC);

    private JsonObject currentJsonResponse;
    //response catcher
    final RestIoListener l = new RestIoListener() {
        @Override
        public void sendingRequest(final String url, final String body, final String methodName) {
        }
        @Override
        public void receivedResponse(final String responseBody) {
            currentJsonResponse = new JsonParser().parse(responseBody).getAsJsonObject();
        }
    };

    /**
     * Default constructor.
     */
    public ShipmentTemplateControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(ShipmentTemplateDao.class);

        final String token = login();

        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
        client.addRestIoListener(l);

        currentJsonResponse = null;
    }

    @Test
    public void testSaveShipmentTemplate() throws RestServiceException, IOException {
        final ShipmentTemplateDto t = new ShipmentTemplateDto(createShipmentTemplate(true));
        t.setId(null);
        final Long id = client.saveShipmentTemplate(t);
        assertNotNull(id);
    }
    @Test
    public void testGetShipmentTemplates() throws RestServiceException, IOException {
        createShipmentTemplate(true);
        createShipmentTemplate(true);

        assertEquals(2, client.getShipmentTemplates(1, 10000, null, null).size());
        assertEquals(1, client.getShipmentTemplates(1, 1, null, null).size());
        assertEquals(1, client.getShipmentTemplates(2, 1, null, null).size());
        assertEquals(0, client.getShipmentTemplates(3, 10000, null, null).size());
    }
    /**
     * The bug found where the count of item returned from server is not equals of real
     * number of items
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testItemCount() throws RestServiceException, IOException {
        createShipmentTemplate(true);
        createShipmentTemplate(true);
        createShipment(true);

        assertEquals(2, client.getShipmentTemplates(1, 10000, null, null).size());
        assertEquals(2, currentJsonResponse.get("totalCount").getAsInt());
    }
    @Test
    public void testGetShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        assertNotNull(client.getShipmentTemplate(sp.getId()));
    }
    @Test
    public void testDeleteShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        client.deleteShipmentTemplate(sp.getId());
        assertNull(dao.findOne(sp.getId()));
    }
    @Test
    public void testSorting() throws RestServiceException, IOException {
        //crate locations
        final LocationProfile lowLocation = createLocationProfile(false);
        lowLocation.setName("z-JUnit Location");
        saveLocationDirectly(lowLocation);

        final LocationProfile topLocation = createLocationProfile(false);
        topLocation.setName("a-JUnit Location");
        saveLocationDirectly(topLocation);

        //create alert profiles
        final AlertProfile lowAlert = createAlertProfile(false);
        lowAlert.setName("z-JUnit Alert");
        saveAlertProfileDirectly(lowAlert);

        final AlertProfile topAlert = createAlertProfile(false);
        lowAlert.setName("a-JUnit Alert");
        saveAlertProfileDirectly(topAlert);

        //create shipment templates
        final ShipmentTemplate sp1 = createShipmentTemplate(false);
        sp1.setName("a-JUnit template");
        sp1.setShipmentDescription("z-JUnit template description");
        sp1.setShippedFrom(topLocation);
        sp1.setShippedTo(lowLocation);
        sp1.setAlertProfile(topAlert);

        final ShipmentTemplate sp2 = createShipmentTemplate(false);
        sp2.setName("z-JUnit template");
        sp2.setShipmentDescription("a-JUnit template description");
        sp2.setShippedFrom(topLocation);
        sp2.setShippedTo(topLocation);
        sp2.setAlertProfile(lowAlert);

        final ShipmentTemplate sp3 = createShipmentTemplate(false);
        sp3.setName("z-JUnit template");
        sp3.setShipmentDescription("z-JUnit template description");
        sp3.setShippedFrom(lowLocation);
        sp3.setShippedTo(topLocation);
        sp3.setAlertProfile(topAlert);

        saveShipmentTemplateDirectly(sp1);
        saveShipmentTemplateDirectly(sp2);
        saveShipmentTemplateDirectly(sp3);

        final int endIndex = 2;

        //name
        assertEquals(sp1.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME, "asc").get(0).getId());
        assertEquals(sp1.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME, "desc").get(endIndex).getId());

        //description
        assertEquals(sp2.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPMENT_DESCRIPTION, "asc").get(0).getId());
        assertEquals(sp2.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPMENT_DESCRIPTION, "desc").get(endIndex).getId());

        //location from
        assertEquals(sp3.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPPED_FROM, "asc").get(0).getId());
        assertEquals(sp3.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPPED_FROM, "desc").get(endIndex).getId());

        //location to
        assertEquals(sp1.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPPED_TO, "asc").get(0).getId());
        assertEquals(sp1.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.SHIPPED_TO, "desc").get(endIndex).getId());

        //alert profile
        assertEquals(sp2.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.ALERT_PROFILE_ID, "asc").get(0).getId());
        assertEquals(sp2.getId(), client.getShipmentTemplates(1, 1000,
                ShipmentTemplateConstants.ALERT_PROFILE_ID, "desc").get(endIndex).getId());
    }
    @Test
    public void testSaveEmpty() throws RestServiceException, IOException {
        final ShipmentTemplateDto t = new ShipmentTemplateDto();
        final Long id = client.saveShipmentTemplate(t);
        assertNotNull(id);

        final ShipmentTemplate tpl = dao.findOne(id);
        assertNull(tpl.getShutdownDeviceAfterMinutes());
        assertNull(tpl.getArrivalNotificationWithinKm());
    }
    @Test
    public void testAutoStartTemplatesInvisible() throws RestServiceException, IOException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        sp.setAutostart(true);
        context.getBean(ShipmentTemplateDao.class).save(sp);

        assertNotNull(client.getShipmentTemplate(sp.getId()));
        //but not occurrence in list
        assertEquals(0, client.getShipmentTemplates(null, null, null, null).size());
    }
    @After
    public void tearDown() {
        client.removeRestIoListener(l);
    }
}
