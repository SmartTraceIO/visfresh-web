/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
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
        final User user = client.getUser(this.user.getId());
        assertNotNull(user);
    }
    @Test
    public void testSaveUser() throws IOException, RestServiceException, AuthenticationException {
        //create company
        final Company c = new Company();
        c.setDescription("JUnit test company");
        c.setName("JUnit-C");
        companyDao.save(c);
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String scale = "scale";
        final String title = "Mrs";

        final User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setScale(scale);
        u.setTitle(title);
        u.setActive(false);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.ReportViewer);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.CompanyAdmin);

        final String password = "password";

        final Long id = client.saveUser(u, c, password);

        assertNotNull(id);
        User u2 = dao.findByEmail(u.getEmail());
        assertNotNull(u2);
        assertEquals(3, u2.getRoles().size());
        assertNotNull(u2.getCompany());
        assertEquals(firstName, u2.getFirstName());
        assertEquals(lastName, u2.getLastName());
        assertEquals(email, u2.getEmail());
        assertEquals(phone, u2.getPhone());
        assertEquals(position, u2.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(scale, u.getScale());
        assertEquals(title, u.getTitle());
        assertFalse(u.isActive());

        final AuthService auth = context.getBean(AuthService.class);

        //check password
        assertNotNull(auth.login(u.getEmail(), password));

        //check update user
        final String newPhone = "2930847093248";
        final String newPassword = "newpassword";

        u.setPhone(newPhone);
        u.setId(u2.getId());
        client.saveUser(u, getCompany(), newPassword);

        u2 = dao.findByEmail(u.getEmail());
        assertEquals(newPhone, u2.getPhone());

        //check password
        assertNotNull(auth.login(u.getEmail(), newPassword));
    }
    @Test
    public void testGetUsers() throws IOException, RestServiceException {
        final Company c = new Company();
        c.setName("Test");
        c.setDescription("Test company");
        context.getBean(CompanyDao.class).save(c);

        final User u1 = createUser("u1@google.com", "A2", "LastA2", c);
        final User u2 = createUser("u2@google.com", "A1", "LastA1", c);
        final String token = client.login("u1@google.com", "");
        client.setAuthToken(token);

        //test limit
        assertEquals(2, client.getUsers(1, 10000, null, null).size());
        assertEquals(1, client.getUsers(1, 1, null, null).size());
        assertEquals(1, client.getUsers(2, 1, null, null).size());

        assertEquals(u2.getId(), client.getUsers(1, 1,
                UserConstants.PROPERTY_FIRST_NAME, "asc").get(0).getId());
        assertEquals(u1.getId(), client.getUsers(1, 1,
                UserConstants.PROPERTY_FIRST_NAME, "desc").get(0).getId());
        //TODO other sortings.
    }
    @Test
    public void testListUsers() throws IOException, RestServiceException {
        final Company c = new Company();
        c.setName("Test");
        c.setDescription("Test company");
        context.getBean(CompanyDao.class).save(c);

        final User u1 = createUser("u1@google.com", "A2", "LastA2", c);
        final User u2 = createUser("u2@google.com", "A1", "LastA1", c);
        final String token = client.login("u1@google.com", "");
        client.setAuthToken(token);

        //test limit
        assertEquals(2, client.getUsers(1, 10000, null, null).size());
        assertEquals(1, client.getUsers(1, 1, null, null).size());
        assertEquals(1, client.getUsers(2, 1, null, null).size());

        assertEquals(u2.getId(), client.getUsers(1, 1,
                UserConstants.PROPERTY_FIRST_NAME, "asc").get(0).getId());
        assertEquals(u1.getId(), client.getUsers(1, 1,
                UserConstants.PROPERTY_FIRST_NAME, "desc").get(0).getId());
        //TODO other sortings.
    }
    @Test
    public void testUpdateUserDetails() throws IOException, RestServiceException {
        final TimeZone tz = TimeZone.getTimeZone("GMT+2");
        final String password = "abrakadabra";
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String position = "Manager";
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;
        final MeasurementUnits units = MeasurementUnits.English;
        final Language language = Language.English;
        final String scale = "scale";
        final String title = "Developer";

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setUser(user.getId());
        req.setTimeZone(tz);
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setEmail(email);
        req.setPhone(phone);
        req.setPosition(position);
        req.setTemperatureUnits(temperatureUnits);
        req.setPassword(password);
        req.setMeasurementUnits(units);
        req.setLanguage(language);
        req.setScale(scale);
        req.setTitle(title);

        client.updateUserDetails(req);

        final String tokent = client.login(req.getEmail(), password);
        client.setAuthToken(tokent);

        final User u = client.getUser(user.getId());

        assertNotNull(u);
        assertEquals(tz, u.getTimeZone());
        assertEquals(temperatureUnits, u.getTemperatureUnits());
        assertEquals(firstName, u.getFirstName());
        assertEquals(lastName, u.getLastName());
        assertEquals(email, u.getEmail());
        assertEquals(phone, u.getPhone());
        assertEquals(position, u.getPosition());
        assertEquals(units, u.getMeasurementUnits());
        assertEquals(language, u.getLanguage());
        assertEquals(scale, u.getScale());
        assertEquals(title, u.getTitle());
    }
    @Test
    public void testDeleteUser() throws IOException, RestServiceException {
        final User u = createUser("asuvorov@mail.ru", "Alexandr", "Suvorov", getCompany());
        client.deleteUser(u);

        assertNull(dao.findOne(u.getId()));
    }
    /**
     * @param email
     * @param firstName
     * @return user.
     */
    private User createUser(final String email, final String firstName,
            final String lastName, final Company company) {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setCompany(company);
        u.setAuthorizedDeviceGroup("AuthorizedDeviceGroup");
        u.setTitle("Mr");
        u.setScale("User Schale");
        u.getRoles().add(Role.CompanyAdmin);
        context.getBean(AuthService.class).saveUser(u, "");
        return u;
    }
}
