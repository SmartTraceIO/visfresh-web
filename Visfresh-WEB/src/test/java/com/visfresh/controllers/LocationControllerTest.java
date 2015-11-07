/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.visfresh.entities.LocationProfile;
import com.visfresh.services.RestServiceException;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationControllerTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public LocationControllerTest() {
        super();
    }
    //@RequestMapping(value = "/saveLocationProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveLocationProfile(@PathVariable final String authToken,
    //        final @RequestBody String profile) {
    @Test
    public void testSaveLocationProfile() throws RestServiceException, IOException {
        final LocationProfile l = createLocationProfile(false);
        final Long id = facade.saveLocationProfile(l);
        assertNotNull(id);
    }
    @Test
    public void testGetLocationProfile() throws IOException, RestServiceException {
        final LocationProfile lp = createLocationProfile(true);
        assertNotNull(facade.getLocation(lp.getId()));
    }
    @Test
    public void testDeleteLocationProfile() throws IOException, RestServiceException {
        final LocationProfile lp = createLocationProfile(true);
        facade.deleteLocation(lp.getId());
        assertNull(facade.getLocation(lp.getId()));
    }
    //@RequestMapping(value = "/getLocationProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getLocationProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetLocationProfiles() throws RestServiceException, IOException {
        createLocationProfile(true);
        createLocationProfile(true);

        assertEquals(2, facade.getLocations(null, null).size());
        assertEquals(1, facade.getLocations(1, 1).size());
        assertEquals(1, facade.getLocations(2, 1).size());
        assertEquals(0, facade.getLocations(3, 10000).size());
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

        //test sort by ID
        LocationProfile first = facade.getLocations(1, 10000, "locationId", "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = facade.getLocations(1, 10000, "locationId", "desc").get(0);
        assertEquals(p5.getId(), first.getId());

        //location name
        first = facade.getLocations(1, 10000, "locationName", "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = facade.getLocations(1, 10000, "locationName", "desc").get(0);
        assertEquals(p1.getId(), first.getId());

        //test sort by address
        first = facade.getLocations(1, 10000, "address", "asc").get(0);
        assertEquals(p3.getId(), first.getId());

        first = facade.getLocations(1, 10000, "address", "desc").get(0);
        assertEquals(p1.getId(), first.getId());

        //test sort by description
        first = facade.getLocations(1, 10000, "companyDescription", "asc").get(0);
        assertEquals(p4.getId(), first.getId());

        first = facade.getLocations(1, 10000, "companyDescription", "desc").get(0);
        assertEquals(p1.getId(), first.getId());

        //test sort by description
        first = facade.getLocations(1, 10000, "notes", "asc").get(0);
        assertEquals(p5.getId(), first.getId());

        first = facade.getLocations(1, 10000, "notes", "desc").get(0);
        assertEquals(p1.getId(), first.getId());
    }
}
