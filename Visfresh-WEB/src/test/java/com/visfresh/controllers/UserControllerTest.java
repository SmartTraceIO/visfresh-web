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

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserControllerTest extends AbstractRestServiceTest {
    private User user;
    private UserDao dao;
    private CompanyDao companyDao;

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
        user = dao.findAll().get(0);
    }
    //@RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getUser(@PathVariable final String authToken,
    //        final @RequestParam String username) {
    @Test
    public void testGetUser() throws IOException, RestServiceException {
        final User user = facade.getUser("anylogin");
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

        p = facade.getProfile();
        assertEquals(1, p.getShipments().size());

        dao.saveProfile(user, null);
        assertNull(facade.getProfile());

        facade.saveProfile(p);
        assertNotNull(facade.getProfile());
        assertEquals(1, p.getShipments().size());
    }
    @Test
    public void testCreateUser() throws IOException, RestServiceException {
        //create company
        final Company c = new Company();
        c.setDescription("JUnit test company");
        c.setId(7777l);
        c.setName("JUnit-C");
        companyDao.save(c);

        final User u = new User();
        u.setLogin("test-1");
        u.setFullName("Junit Automat");
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.CompanyAdmin);

        final String password = "password";

        facade.createUser(u, c, password);

        final User u2 = dao.findOne(u.getLogin());
        assertNotNull(u2);
        assertEquals(u.getFullName(), u2.getFullName());
        assertEquals(u.getId(), u2.getId());
        assertEquals(u.getLogin(), u2.getLogin());
        assertEquals(2, u2.getRoles().size());
        assertNotNull(u2.getCompany());
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

        facade.updateUserDetails(req);

        final String tokent = facade.login(user.getLogin(), password);
        facade.setAuthToken(tokent);

        final User u = facade.getUser(user.getLogin());

        assertNotNull(u);
        assertEquals(fullName, u.getFullName());
        assertEquals(tz, u.getTimeZone());
        assertEquals(temperatureUnits, u.getTemperatureUnits());
    }
}
