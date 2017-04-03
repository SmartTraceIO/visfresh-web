/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.controllers.restclient.RestClient;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UtilitiesControllerTest extends AbstractRestServiceTest {
    private RestClient client = new RestClient();

    /**
     * Default constructor.
     */
    public UtilitiesControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }

    @Test
    public void testGetTimeZones() throws IOException, RestServiceException {
        final JsonArray array = client.sendGetRequest(
                client.getPathWithToken("getTimeZones"), new HashMap<String, String>()).getAsJsonArray();
        assertTrue(array.size() > 1);
    }
    @Test
    public void testGetLanguages() throws IOException, RestServiceException {
        final JsonArray array = client.sendGetRequest(
                client.getPathWithToken("getLanguages"), new HashMap<String, String>()).getAsJsonArray();
        assertTrue(array.size() > 0);
    }
    @Test
    public void testGetMeasurementUnits() throws IOException, RestServiceException {
        final JsonArray array = client.sendGetRequest(
                client.getPathWithToken("getMeasurementUnits"), new HashMap<String, String>()).getAsJsonArray();
        assertTrue(array.size() > 0);
    }
    @Test
    public void testGetRoles() throws IOException, RestServiceException {
        final JsonArray array = client.sendGetRequest(
                client.getPathWithToken("getRoles"), new HashMap<String, String>()).getAsJsonArray();
        assertTrue(array.size() > 0);
    }
    @Test
    public void testGetColors() throws IOException, RestServiceException {
        final JsonArray array = client.sendGetRequest(
                client.getPathWithToken("getColors"), new HashMap<String, String>()).getAsJsonArray();
        assertTrue(array.size() > 0);
    }
    @Test
    public void testGetUserTime() throws IOException, RestServiceException {
        final JsonObject json = client.sendGetRequest(
                client.getPathWithToken("getUserTime"), new HashMap<String, String>()).getAsJsonObject();
        assertNotNull(json);
    }
    @Test
    public void testClearShipmentCache() throws IOException, RestServiceException {
        final Shipment shipment = createShipment(true);
        final ShipmentDao dao = context.getBean(ShipmentDao.class);

        //load the shipment by DAO. It fills the cache
        dao.findOne(shipment.getId());
        assertTrue(dao.hasInCache(shipment.getCompany(), shipment.getId()));

        //check by incorrect shipment.
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipment", shipment.getId() + "111");

        JsonObject json = client.sendGetRequest(
                client.getPathWithToken("clearCache"), params).getAsJsonObject();
        assertFalse(json.get("clean").getAsBoolean());

        //check by correct shipment
        params.put("shipment", shipment.getId().toString());

        json = client.sendGetRequest(
                client.getPathWithToken("clearCache"), params).getAsJsonObject();
        assertTrue(json.get("clean").getAsBoolean());
        assertFalse(dao.hasInCache(shipment.getCompany(), shipment.getId()));
    }
    @Test
    public void testClearLocationCache() throws IOException, RestServiceException {
        final LocationProfile profile = createLocationProfile(true);
        final LocationProfileDao dao = context.getBean(LocationProfileDao.class);

        //load the shipment by DAO. It fills the cache
        dao.findOne(profile.getId());
        assertTrue(dao.hasInCache(profile.getCompany(), profile.getId()));

        //check by incorrect shipment.
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("location", profile.getId() + "111");

        JsonObject json = client.sendGetRequest(
                client.getPathWithToken("clearCache"), params).getAsJsonObject();
        assertFalse(json.get("clean").getAsBoolean());

        //check by correct shipment
        params.put("location", profile.getId().toString());

        json = client.sendGetRequest(
                client.getPathWithToken("clearCache"), params).getAsJsonObject();
        assertTrue(json.get("clean").getAsBoolean());
        assertFalse(dao.hasInCache(profile.getCompany(), profile.getId()));
    }
}
