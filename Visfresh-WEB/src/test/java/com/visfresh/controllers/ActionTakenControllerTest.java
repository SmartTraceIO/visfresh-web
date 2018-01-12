/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.ActionTakenRestClient;
import com.visfresh.dao.ActionTakenDao;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenControllerTest extends AbstractRestServiceTest {
    private ActionTakenDao dao;
    private ActionTakenRestClient client;
    private Shipment shipment;
    private User user;
    private TemperatureAlert temperatureAlert;
    private Alert lightOnAlert;
    /**
     * Default constructor.
     */
    public ActionTakenControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(ActionTakenDao.class);

        user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        String token;
        try {
            token = context.getBean(AuthService.class).login(user.getEmail(),"", "junit").getToken();
        } catch (final AuthenticationException e) {
            throw new RuntimeException(e);
        }

        client = new ActionTakenRestClient(user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);

        //create shipment
        shipment = createShipment(true);

        //create temperature alert
        final TemperatureRule tempRule = shipment.getAlertProfile().getAlertRules().get(0);

        final TemperatureAlert ta = new TemperatureAlert();
        ta.setType(tempRule.getType());
        ta.setCumulative(true);
        ta.setDate(new Date(System.currentTimeMillis() - 1000000l));
        ta.setDevice(shipment.getDevice());
        ta.setMinutes(79);
        ta.setRuleId(tempRule.getId());
        ta.setShipment(shipment);
        ta.setTemperature(99);
        context.getBean(AlertDao.class).save(ta);
        this.temperatureAlert = ta;

        //create usual light on alert

        final Alert la = new Alert(AlertType.LightOn);
        la.setDate(new Date(System.currentTimeMillis() - 100000l));
        la.setDevice(shipment.getDevice());
        la.setShipment(shipment);
        context.getBean(AlertDao.class).save(la);
        this.lightOnAlert = la;
    }

    //@RequestMapping(value = "/saveActionTaken", method = RequestMethod.POST)
    //public @ResponseBody String saveActionTaken(
    //        final @RequestBody String alert) {
    @Test
    public void testSaveActionTaken() throws RestServiceException, IOException {
        final ActionTaken p = createActionTaken(temperatureAlert, "Check the door opened");
        final Long id = client.saveActionTaken(p);
        assertNotNull(id);
    }
    @Test
    public void testGetActionTaken() throws IOException, RestServiceException {
        final ActionTaken ap = createActionTaken(temperatureAlert, "Check the door opened");
        assertNotNull(client.getActionTaken(ap.getId()));
    }
    @Test
    public void testVerifyActionTaken() throws IOException, RestServiceException {
        ActionTaken a1 = createActionTaken(temperatureAlert, "Action 1");
        ActionTaken a2 = createActionTaken(temperatureAlert, "Action 2");

        //test verify by comments
        client.verifyActionTaken(a1.getId(), "Comments for verify");

        a1 = dao.findOne(a1.getId());
        assertEquals("Comments for verify", a1.getVerifiedComments());
        assertEquals(user.getId(), a1.getVerifiedBy());
        assertNotNull(a1.getVerifiedTime());

        //test verify without comments
        client.verifyActionTaken(a2.getId(), null);

        a2 = dao.findOne(a2.getId());
        assertNull(a2.getVerifiedComments());
        assertEquals(user.getId(), a2.getVerifiedBy());
        assertNotNull(a2.getVerifiedTime());

        //test not verify if verified
        client.verifyActionTaken(a1.getId(), "Other comment");

        a1 = dao.findOne(a1.getId());
        assertEquals("Comments for verify", a1.getVerifiedComments());
    }
    @Test
    public void testDeleteActionTaken() throws RestServiceException, IOException {
        final ActionTaken p = createActionTaken(temperatureAlert, "Check the door opened");
        client.deleteActionTaken(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getActionTakens", method = RequestMethod.GET)
    //public @ResponseBody String getActionTakens() {
    @Test
    public void testGetActionTakens() throws RestServiceException, IOException {
        createActionTaken(temperatureAlert, "Check the door opened");
        createActionTaken(lightOnAlert, "Close the door");

        assertEquals(2, client.getActionTakens(shipment.getId()).size());
    }

    /**
     *
     */
    private ActionTaken createActionTaken(final Alert alert, final String action) {
        final ActionTaken at = new ActionTaken();
        at.setAction(new CorrectiveAction(action));
        at.setAlert(alert.getId());
        at.setComments("Any comments");
        at.setConfirmedBy(user.getId());
        at.setTime(new Date(alert.getDate().getTime() + 10000l));
        return dao.save(at);
    }
}
