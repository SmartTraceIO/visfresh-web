/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.controllers.restclient.AlertProfileRestClient;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CorrectiveActionListDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

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

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        String token;
        try {
            token = context.getBean(AuthService.class).login(user.getEmail(),"", "junit").getToken();
        } catch (final RestServiceException e) {
            throw new RuntimeException(e);
        }

        client = new AlertProfileRestClient(user.getTimeZone(), user.getTemperatureUnits());
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
    }

    //@RequestMapping(value = "/saveAlertProfile", method = RequestMethod.POST)
    //public @ResponseBody String saveAlertProfile(
    //        final @RequestBody String alert) {
    @Test
    public void testSaveAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile(false);
        final Long id = client.saveAlertProfile(p);
        assertNotNull(id);
    }
    @Test
    public void testChangeToCumulative() throws RestServiceException, IOException {
        final AlertProfile ap = new AlertProfile();
        ap.setName("AnyAlert");
        ap.setDescription("Any description");

        final int normalTemperature = 3;
        final TemperatureRule criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 15);
        criticalHot.setTimeOutMinutes(0);
        criticalHot.setCumulativeFlag(false);
        ap.getAlertRules().add(criticalHot);

        final Long id = client.saveAlertProfile(ap);

        //change rule to cumulative
        AlertProfile p = dao.findOne(id);
        p.getAlertRules().get(0).setCumulativeFlag(true);
        client.saveAlertProfile(p);

        //check cumulative is really set
        p = dao.findOne(id);
        assertTrue(p.getAlertRules().get(0).isCumulativeFlag());
    }
    @Test
    public void testSaveInFahrenheit() throws RestServiceException, IOException {
        final String authToken = client.getAuthToken();

        final User user = context.getBean(AuthService.class).getUserForToken(authToken);
        user.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        context.getBean(UserDao.class).save(user);

        //rebuild client
        client = new AlertProfileRestClient(user.getTimeZone(), user.getTemperatureUnits());
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(authToken);

        final double lowerTemperatureLimit = -11.11;
        final double upperTemperatureLimit = 12.12;
        final int temp = 18;

        final AlertProfile ap = new AlertProfile();
        ap.setName("AnyAlert");
        ap.setDescription("Any description");
        ap.setLowerTemperatureLimit(lowerTemperatureLimit);
        ap.setUpperTemperatureLimit(upperTemperatureLimit);

        final TemperatureRule criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(temp);
        ap.getAlertRules().add(criticalHot);

        final Long id = client.saveAlertProfile(ap);

        //change rule to cumulative
        final AlertProfile p = dao.findOne(id);
        assertEquals(temp, p.getAlertRules().get(0).getTemperature(), 0.00001);
        assertEquals(lowerTemperatureLimit, p.getLowerTemperatureLimit(), 0.00001);
        assertEquals(upperTemperatureLimit, p.getUpperTemperatureLimit(), 0.00001);
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
    //@RequestMapping(value = "/getAlertProfiles", method = RequestMethod.GET)
    //public @ResponseBody String getAlertProfiles() {
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
        ListAlertProfileItem first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_ID, "asc").get(0);
        assertEquals((long) p1.getId(), first.getAlertProfileId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_ID, "desc").get(maxIndex);
        assertEquals((long) p1.getId(), first.getAlertProfileId());

        //test sort by name
        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_NAME, "asc").get(0);
        assertEquals((long) p2.getId(), first.getAlertProfileId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_NAME, "desc").get(maxIndex);
        assertEquals((long) p2.getId(), first.getAlertProfileId());

        //test sort by description
        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_DESCRIPTION, "asc").get(0);
        assertEquals((long) p3.getId(), first.getAlertProfileId());

        first = client.getAlertProfiles(1, 10000,
                AlertProfileConstants.ALERT_PROFILE_DESCRIPTION, "desc").get(maxIndex);
        assertEquals((long) p3.getId(), first.getAlertProfileId());
    }
    @Test
    public void testGetCorrectiveActions() throws IOException, RestServiceException {
        final AlertProfile ap = createAlertProfile(true);

        final CorrectiveActionList lo = createCorrectiveActions();
        final CorrectiveActionList bl = createCorrectiveActions();

        ap.setLightOnCorrectiveActions(lo);
        ap.setBatteryLowCorrectiveActions(bl);
        dao.save(ap);

        final AlertProfile actual = client.getAlertProfile(ap.getId());
        final CorrectiveActionList actualLo = actual.getLightOnCorrectiveActions();
        final CorrectiveActionList actualBl = actual.getBatteryLowCorrectiveActions();

        assertEquals(lo.getId(), actualLo.getId());
        assertEquals(bl.getId(), actualBl.getId());

        //check LightOn action
        assertEquals(lo.getName(), actualLo.getName());
        assertEquals(lo.getActions().size(), actualLo.getActions().size());

        //check BatteryLow action
        assertEquals(bl.getName(), actualBl.getName());
        assertEquals(bl.getActions().size(), actualBl.getActions().size());
    }
    @Test
    public void testSaveCorrectiveActions() throws RestServiceException, IOException {
        final AlertProfile ap = createAlertProfile(true);

        ap.setLightOnCorrectiveActions(createCorrectiveActions());
        ap.setBatteryLowCorrectiveActions(createCorrectiveActions());
        client.saveAlertProfile(ap);

        final AlertProfile actual = dao.findOne(ap.getId());
        assertEquals(actual.getLightOnCorrectiveActions().getId(), ap.getLightOnCorrectiveActions().getId());
        assertEquals(actual.getBatteryLowCorrectiveActions().getId(), ap.getBatteryLowCorrectiveActions().getId());
    }
    @Test
    public void testGetTemperatureRuleCorrectiveActions() throws IOException, RestServiceException {
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(getCompanyId());
        ap.setName("AnyAlert");
        ap.setDescription("Any description");

        final TemperatureRule criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(17);
        ap.getAlertRules().add(criticalHot);

        final CorrectiveActionList actions = createCorrectiveActions();
        criticalHot.setCorrectiveActions(actions);

        dao.save(ap);

        final CorrectiveActionList actual = client.getAlertProfile(ap.getId()).getAlertRules().get(0).getCorrectiveActions();

        assertEquals(actions.getId(), actual.getId());
        assertEquals(actions.getName(), actual.getName());
        assertEquals(actions.getActions().size(), actual.getActions().size());
    }
    @Test
    public void testSaveTemperatureRuleCorrectiveActions() throws RestServiceException, IOException {
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(getCompanyId());
        ap.setName("AnyAlert");
        ap.setDescription("Any description");

        final TemperatureRule criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(17);
        ap.getAlertRules().add(criticalHot);

        dao.save(ap);

        final CorrectiveActionList actions = createCorrectiveActions();
        criticalHot.setCorrectiveActions(actions);
        client.saveAlertProfile(ap);

        final CorrectiveActionList actual = dao.findOne(ap.getId()).getAlertRules().get(0).getCorrectiveActions();

        assertEquals(actions.getId(), actual.getId());
        assertEquals(actions.getName(), actual.getName());
        assertEquals(actions.getDescription(), actual.getDescription());
        assertEquals(actions.getActions().size(), actual.getActions().size());
    }

    /**
     * @return corrective action list.
     */
    private CorrectiveActionList createCorrectiveActions() {
        final CorrectiveActionList list = new CorrectiveActionList();
        list.setCompany(getCompanyId());
        list.setName("JUnit actions");
        list.setDescription("JUnit action list description");
        list.getActions().add(new CorrectiveAction("First action", true));
        list.getActions().add(new CorrectiveAction("Second action", true));
        return context.getBean(CorrectiveActionListDao.class).save(list);
    }
}
