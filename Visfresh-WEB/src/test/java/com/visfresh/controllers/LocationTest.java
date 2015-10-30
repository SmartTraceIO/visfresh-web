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
public class LocationTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public LocationTest() {
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

        assertEquals(2, facade.getLocationProfiles(1, 10000).size());
        assertEquals(1, facade.getLocationProfiles(1, 1).size());
        assertEquals(1, facade.getLocationProfiles(2, 1).size());
        assertEquals(0, facade.getLocationProfiles(3, 10000).size());
    }
}
