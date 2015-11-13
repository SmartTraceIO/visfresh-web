/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.UserConstants;
import com.visfresh.controllers.restclient.UserRestClient;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserControllerTest extends AbstractRestServiceTest {
    private User user;
    private UserDao dao;
    private CompanyDao companyDao;
    private UserRestClient client;

    /**
     * Default constructor.
     */
    public UserControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(UserDao.class);
        companyDao = context.getBean(CompanyDao.class);
        user = dao.findAll(null, null, null).get(0);

        client = new UserRestClient(UTC);
        client.setAuthToken(login());
        client.setServiceUrl(getServiceUrl());
        client.setCompanyResolver(context.getBean(CompanyResolver.class));
        client.setShipmentResolver(context.getBean(ShipmentResolver.class));
    }
    //@RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getUser(@PathVariable final String authToken,
    //        final @RequestParam String username) {
    @Test
    public void testGetUser() throws IOException, RestServiceException {
        final User user = client.getUser("anylogin");
        assertNotNull(user);
    }
    @Test
    public void testUserProfile() throws IOException, RestServiceException {
        //add shipment to service
        final Shipment sp = createShipment(true);
        //add profile to service

        UserProfile p = new UserProfile();
        p.getShipments().add(sp);

        dao.saveProfile(user, p);

        p = client.getProfile();
        assertEquals(1, p.getShipments().size());

        dao.saveProfile(user, null);
        assertNull(client.getProfile());

        client.saveProfile(p);
        assertNotNull(client.getProfile());
        assertEquals(1, p.getShipments().size());
    }
    @Test
    public void testCreateUser() throws IOException, RestServiceException {
        //create company
        final Company c = new Company();
        c.setDescription("JUnit test company");
        c.setName("JUnit-C");
        companyDao.save(c);

        final User u = new User();
        u.setLogin("test-1");
        u.setFullName("Junit Automat");
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.CompanyAdmin);

        final String password = "password";

        client.createUser(u, c, password);

        final User u2 = dao.findOne(u.getLogin());
        assertNotNull(u2);
        assertEquals(u.getFullName(), u2.getFullName());
        assertEquals(u.getId(), u2.getId());
        assertEquals(u.getLogin(), u2.getLogin());
        assertEquals(2, u2.getRoles().size());
        assertNotNull(u2.getCompany());
    }
    @Test
    public void testGetUsers() throws IOException, RestServiceException {
        final Company c = new Company();
        c.setName("Test");
        c.setDescription("Test company");
        context.getBean(CompanyDao.class).save(c);

        createUser("u1", "A2", c);
        createUser("u2", "A1", c);
        final String token = client.login("u1", "");
        client.setAuthToken(token);

        //test limit
        assertEquals(2, client.getUsers(1, 10000, null, null).size());
        assertEquals(1, client.getUsers(1, 1, null, null).size());
        assertEquals(1, client.getUsers(2, 1, null, null).size());

        //test sort
        assertEquals("u1", client.getUsers(1, 1, UserConstants.PROPERTY_LOGIN, "asc").get(0).getLogin());
        assertEquals("u2", client.getUsers(1, 1, UserConstants.PROPERTY_LOGIN, "desc").get(0).getLogin());

        assertEquals("u2", client.getUsers(1, 1, UserConstants.PROPERTY_FULL_NAME, "asc").get(0).getLogin());
        assertEquals("u1", client.getUsers(1, 1, UserConstants.PROPERTY_FULL_NAME, "desc").get(0).getLogin());
    }
    @Test
    public void testUpdateUserDetails() throws IOException, RestServiceException {
        final TimeZone tz = TimeZone.getTimeZone("GMT+2");
        final String fullName = "Full User Name";
        final String password = "abrakadabra";
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setUser(user.getLogin());
        req.setTimeZone(tz);
        req.setFullName(fullName);
        req.setTemperatureUnits(temperatureUnits);
        req.setPassword(password);

        client.updateUserDetails(req);

        final String tokent = client.login(user.getLogin(), password);
        client.setAuthToken(tokent);

        final User u = client.getUser(user.getLogin());

        assertNotNull(u);
        assertEquals(fullName, u.getFullName());
        assertEquals(tz, u.getTimeZone());
        assertEquals(temperatureUnits, u.getTemperatureUnits());
    }
    /**
     * @param login
     * @param fullName
     * @return user.
     */
    private User createUser(final String login, final String fullName, final Company company) {
        final User u = new User();
        u.setLogin(login);
        u.setFullName(fullName);
        u.setCompany(company);
        u.getRoles().add(Role.CompanyAdmin);
        context.getBean(AuthService.class).createUser(u, "");
        return u;
    }
}
