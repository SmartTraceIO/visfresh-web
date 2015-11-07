/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileControllerTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public AlertProfileControllerTest() {
        super();
    }
    //@RequestMapping(value = "/saveAlertProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveAlertProfile(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile(false);
        final Long id = facade.saveAlertProfile(p);
        assertNotNull(id);
    }
    @Test
    public void testGetAlertProfile() throws IOException, RestServiceException {
        final AlertProfile ap = createAlertProfile(true);
        assertNotNull(facade.getAlertProfile(ap.getId()));
    }
    @Test
    public void testDeleteAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile(true);
        facade.deleteAlertProfile(p);
        assertNull(getRestService().getAlertProfile(getCompany(), p.getId()));
    }
    //@RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getAlertProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetAlertProfiles() throws RestServiceException, IOException {
        createAlertProfile(true);
        createAlertProfile(true);

        assertEquals(2, facade.getAlertProfiles(null, null).size());
        assertEquals(1, facade.getAlertProfiles(1, 1).size());
        assertEquals(1, facade.getAlertProfiles(2, 1).size());
        assertEquals(0, facade.getAlertProfiles(3, 1).size());
    }
    @Test
    public void testGetSortedAlertProfiles() throws RestServiceException, IOException {
        final AlertProfile p1 = createAlertProfile(false);
        p1.setName("b");
        p1.setDescription("c");
        getRestService().saveAlertProfile(getCompany(), p1);

        final AlertProfile p2 = createAlertProfile(false);
        p2.setName("a");
        p2.setDescription("b");
        getRestService().saveAlertProfile(getCompany(), p2);

        final AlertProfile p3 = createAlertProfile(false);
        p3.setName("c");
        p3.setDescription("a");
        getRestService().saveAlertProfile(getCompany(), p3);

        //test sort by ID
        AlertProfile first = facade.getAlertProfiles(1, 10000, "alertProfileId", "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = facade.getAlertProfiles(1, 10000, "alertProfileId", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by name
        first = facade.getAlertProfiles(1, 10000, "alertProfileName", "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = facade.getAlertProfiles(1, 10000, "alertProfileName", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by description
        first = facade.getAlertProfiles(1, 10000, "alertProfileDescription", "asc").get(0);
        assertEquals(p3.getId(), first.getId());

        first = facade.getAlertProfiles(1, 10000, "alertProfileDescription", "desc").get(0);
        assertEquals(p1.getId(), first.getId());
    }
}
