/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.LocationConstants;
import com.visfresh.controllers.restclient.LocationRestClient;
import com.visfresh.entities.LocationProfile;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationControllerTest extends AbstractRestServiceTest {
    private LocationRestClient client;

    /**
     * Default constructor.
     */
    public LocationControllerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        client = new LocationRestClient(SerializerUtils.UTС);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }

    //@RequestMapping(value = "/saveLocationProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveLocationProfile(@PathVariable final String authToken,
    //        final @RequestBody String profile) {
    @Test
    public void testSaveLocationProfile() throws RestServiceException, IOException {
        final LocationProfile l = createLocationProfile(false);
        final Long id = client.saveLocationProfile(l);
        assertNotNull(id);
    }
    @Test
    public void testGetLocationProfile() throws IOException, RestServiceException {
        final LocationProfile lp = createLocationProfile(true);
        assertNotNull(client.getLocation(lp.getId()));
    }
    @Test
    public void testDeleteLocationProfile() throws IOException, RestServiceException {
        final LocationProfile lp = createLocationProfile(true);
        client.deleteLocation(lp.getId());
        assertNull(client.getLocation(lp.getId()));
    }
    //@RequestMapping(value = "/getLocationProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getLocationProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetLocationProfiles() throws RestServiceException, IOException {
        createLocationProfile(true);
        createLocationProfile(true);

        assertEquals(2, client.getLocations(null, null).size());
        assertEquals(1, client.getLocations(1, 1).size());
        assertEquals(1, client.getLocations(2, 1).size());
        assertEquals(0, client.getLocations(3, 10000).size());
    }
    @Test
    public void testGetSortedLocationProfiles() throws RestServiceException, IOException {
        final LocationProfile p1 = createLocationProfile(false);
        p1.setName("f");
        p1.setAddress("f");
        p1.setCompanyName("f");
        p1.setNotes("f");
        saveLocationDirectly(p1);

        final LocationProfile p2 = createLocationProfile(false);
        p2.setName("a");
        p2.setAddress("b");
        p2.setCompanyName("c");
        p2.setNotes("d");
        saveLocationDirectly(p2);

        final LocationProfile p3 = createLocationProfile(false);
        p3.setName("d");
        p3.setAddress("a");
        p3.setCompanyName("b");
        p3.setNotes("c");
        saveLocationDirectly(p3);

        final LocationProfile p4 = createLocationProfile(false);
        p4.setName("c");
        p4.setAddress("d");
        p4.setCompanyName("a");
        p4.setNotes("b");
        saveLocationDirectly(p4);

        final LocationProfile p5 = createLocationProfile(false);
        p5.setName("b");
        p5.setAddress("c");
        p5.setCompanyName("d");
        p5.setNotes("a");
        saveLocationDirectly(p5);

        final int lastIndex = 4;
        //test sort by ID
        LocationProfile loc = client.getLocations(1, 10000,
                LocationConstants.PROPERTY_LOCATION_ID, "asc").get(0);
        assertEquals(p1.getId(), loc.getId());

        loc = client.getLocations(1, 10000,
                LocationConstants.PROPERTY_LOCATION_ID, "desc").get(lastIndex);
        assertEquals(p1.getId(), loc.getId());

        //location name
        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_LOCATION_NAME, "asc").get(0);
        assertEquals(p2.getId(), loc.getId());

        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_LOCATION_NAME, "desc").get(lastIndex);
        assertEquals(p2.getId(), loc.getId());

        //test sort by address
        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_ADDRESS, "asc").get(0);
        assertEquals(p3.getId(), loc.getId());

        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_ADDRESS, "desc").get(lastIndex);
        assertEquals(p3.getId(), loc.getId());

        //test sort by description
        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_COMPANY_NAME, "asc").get(0);
        assertEquals(p4.getId(), loc.getId());

        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_COMPANY_NAME, "desc").get(lastIndex);
        assertEquals(p4.getId(), loc.getId());

        //test sort by description
        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_NOTES, "asc").get(0);
        assertEquals(p5.getId(), loc.getId());

        loc = client.getLocations(1, 10000, LocationConstants.PROPERTY_NOTES, "desc").get(lastIndex);
        assertEquals(p5.getId(), loc.getId());
    }
}
