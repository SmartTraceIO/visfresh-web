/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.controllers.restclient.RestClient;
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
        final JsonObject array = client.sendGetRequest(
                client.getPathWithToken("getUserTime"), new HashMap<String, String>()).getAsJsonObject();
        assertNotNull(array);
    }
}
