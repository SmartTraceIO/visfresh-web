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

import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.controllers.restclient.AlertProfileRestClient;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileControllerTest extends AbstractRestServiceTest {
    private AlertProfileDao dao;
    private AlertProfileRestClient client;
    /**
     * Default constructor.
     */
    public AlertProfileControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(AlertProfileDao.class);
        client = new AlertProfileRestClient(SerializerUtils.UTС);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }

    //@RequestMapping(value = "/saveAlertProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveAlertProfile(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile(false);
        final Long id = client.saveAlertProfile(p);
        assertNotNull(id);
    }
    @Test
    public void testGetAlertProfile() throws IOException, RestServiceException {
        final AlertProfile ap = createAlertProfile(true);
        assertNotNull(client.getAlertProfile(ap.getId()));
    }
    @Test
    public void testDeleteAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile(true);
        client.deleteAlertProfile(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getAlertProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetAlertProfiles() throws RestServiceException, IOException {
        createAlertProfile(true);
        createAlertProfile(true);

        assertEquals(2, client.getAlertProfiles(null, null).size());
        assertEquals(1, client.getAlertProfiles(1, 1).size());
        assertEquals(1, client.getAlertProfiles(2, 1).size());
        assertEquals(0, client.getAlertProfiles(3, 1).size());
    }
    @Test
    public void testGetSortedAlertProfiles() throws RestServiceException, IOException {
        final AlertProfile p1 = createAlertProfile(false);
        p1.setName("b");
        p1.setDescription("c");
        saveAlertProfileDirectly(p1);

        final AlertProfile p2 = createAlertProfile(false);
        p2.setName("a");
        p2.setDescription("b");
        saveAlertProfileDirectly(p2);

        final AlertProfile p3 = createAlertProfile(false);
        p3.setName("c");
        p3.setDescription("a");
        saveAlertProfileDirectly(p3);

        final int maxIndex = 2;

        //test sort by ID
        AlertProfile first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID, "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID, "desc").get(maxIndex);
        assertEquals(p1.getId(), first.getId());

        //test sort by name
        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME, "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME, "desc").get(maxIndex);
        assertEquals(p2.getId(), first.getId());

        //test sort by description
        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION, "asc").get(0);
        assertEquals(p3.getId(), first.getId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION, "desc").get(maxIndex);
        assertEquals(p3.getId(), first.getId());
    }
}
