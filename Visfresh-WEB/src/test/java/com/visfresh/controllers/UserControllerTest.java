/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.UserConstants;
import com.visfresh.controllers.restclient.UserRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthToken;
import com.visfresh.services.RestServiceException;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserControllerTest extends AbstractRestServiceTest {
    private User user;
    private UserDao dao;
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
        user = dao.findAll(null, null, null).get(0);

        final UserRestClient c = createClient();
        c.setAuthToken(login());
        this.client = c;
    }
    /**
     * Tests getUser method.
     * @throws IOException
     * @throws RestServiceException
     */
    @Test
    public void testGetUser() throws IOException, RestServiceException {
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String externalCompany = "External JUnit company";
        final boolean external = true;
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String title = "Mr";

        final User u = new User();
        u.setCompany(getCompanyId());
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setTitle(title);
        u.setActive(false);
        u.setExternal(external);
        u.setExternalCompany(externalCompany);
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.NormalUser);
        u.getRoles().add(Role.Admin);

        final String password = "password";

        final AuthService auth = context.getBean(AuthService.class);
        auth.saveUser(u, password, true);

        final User u2 = client.getUser(u.getId());
        assertNotNull(u2);
        assertEquals(2, u2.getRoles().size());
        assertEquals(firstName, u2.getFirstName());
        assertEquals(lastName, u2.getLastName());
        assertEquals(email, u2.getEmail());
        assertEquals(phone, u2.getPhone());
        assertEquals(externalCompany, u2.getExternalCompany());
        assertEquals(position, u2.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(title, u.getTitle());
        assertFalse(u.getActive());
        assertEquals(external, u.getExternal());
    }
    @Test
    public void testGetUserWithoutUserId() throws IOException, RestServiceException {
        //test without user ID.
        assertNotNull(client.getUser(null));
    }
    @Test
    public void testSaveUser() throws IOException, RestServiceException {
        //create company
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String externalCompany = "External JUnit company";
        final boolean external = true;
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String title = "Mrs";

        final User u = new User();
        u.setCompany(getCompanyId());
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setTitle(title);
        u.setActive(true);
        u.setExternal(external);
        u.setExternalCompany(externalCompany);
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.NormalUser);
        u.getRoles().add(Role.BasicUser);

        final String password = "password";

        final Long id = client.saveUser(u, getCompany(), password, true);

        assertNotNull(id);
        User u2 = dao.findByEmail(u.getEmail());
        assertNotNull(u2);
        assertEquals(2, u2.getRoles().size());
        assertNotNull(u2.getCompanyId());
        assertEquals(firstName, u2.getFirstName());
        assertEquals(lastName, u2.getLastName());
        assertEquals(email, u2.getEmail());
        assertEquals(phone, u2.getPhone());
        assertEquals(externalCompany, u2.getExternalCompany());
        assertEquals(position, u2.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(title, u.getTitle());
        assertTrue(u.getActive());
        assertEquals(external, u.getExternal());

        final AuthService auth = context.getBean(AuthService.class);

        //check password
        assertNotNull(auth.login(u.getEmail(), password, "junit"));

        //check update user
        final String newPhone = "2930847093248";
        final String newPassword = "newpassword";

        u.setPhone(newPhone);
        u.setId(id);
        client.saveUser(u, getCompany(), newPassword, true);

        u2 = dao.findByEmail(u.getEmail());
        assertEquals(newPhone, u2.getPhone());

        //check password
        assertNotNull(auth.login(u.getEmail(), newPassword, "junit"));
    }
    @Test
    public void testSaveWithoutCompany() throws IOException, RestServiceException {
        user.getRoles().clear();
        user.getRoles().add(Role.Admin);
        dao.save(user);

        final AuthService auth = context.getBean(AuthService.class);
        final AuthToken authToken = auth.login(user.getEmail(), "", "junit");
        client.setAuthToken(authToken.getToken());

        //create company
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String externalCompany = "External JUnit company";
        final boolean external = true;
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String title = "Mrs";

        final User u = new User();
        u.setCompany(null);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setTitle(title);
        u.setActive(true);
        u.setExternal(external);
        u.setExternalCompany(externalCompany);
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.NormalUser);
        u.getRoles().add(Role.BasicUser);

        final String password = "password";

        final Long id = client.saveUser(u, null, password, true);

        assertNotNull(id);
        User u2 = dao.findByEmail(u.getEmail());
        assertNotNull(u2);
        assertEquals(2, u2.getRoles().size());
        assertNotNull(u2.getCompanyId());
        assertEquals(firstName, u2.getFirstName());
        assertEquals(lastName, u2.getLastName());
        assertEquals(email, u2.getEmail());
        assertEquals(phone, u2.getPhone());
        assertEquals(externalCompany, u2.getExternalCompany());
        assertEquals(position, u2.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(title, u.getTitle());
        assertTrue(u.getActive());
        assertEquals(external, u.getExternal());

        //check update user
        final String newPhone = "2930847093248";
        final String newPassword = "newpassword";

        u.setPhone(newPhone);
        u.setId(u2.getId());
        u.setCompany(getCompanyId());
        client.saveUser(u, getCompany(), newPassword, true);

        u2 = dao.findByEmail(u.getEmail());
        assertEquals(newPhone, u2.getPhone());

        //check password
        assertNotNull(auth.login(u.getEmail(), newPassword, "junit"));
    }
    @Test
    public void testNotLoginInactiveUser() throws RestServiceException {
        final User u = createUser("abra@cada.bra", "James", "Bond", getCompany());

        //check login
        final AuthService auth = context.getBean(AuthService.class);
        assertNotNull(auth.login(u.getEmail(), "", "junit"));

        //set user to inactive
        u.setActive(false);
        dao.save(u);

        try {
            assertNotNull(auth.login(u.getEmail(), "", "junit"));
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final RestServiceException e) {
            // ok
        }
    }
    @Test
    public void testLogout() throws IOException, RestServiceException {
        final User u = createUser("abra@cada.bra", "James", "Bond", getCompany());

        //check login
        final AuthService auth = context.getBean(AuthService.class);

        assertNotNull(auth.login(u.getEmail(), "", "junit"));
        assertNotNull(client.getUser(null));

        auth.logout(client.getAuthToken());
        try {
            client.getUser(null);
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final Exception e) {
            // ok
        }
    }
    @Test
    public void testForceLogout() throws IOException, RestServiceException {
        final User u = createUser("abra@cada.bra", "James", "Bond", getCompany());

        //check login
        final AuthService auth = context.getBean(AuthService.class);

        final UserRestClient c1 = createClient();
        final UserRestClient c2 = createClient();

        c1.setAuthToken(auth.login(u.getEmail(), "", "junit1").getToken());
        c2.setAuthToken(auth.login(u.getEmail(), "", "junit2").getToken());

        assertNotNull(c1.getUser(null));
        assertNotNull(c2.getUser(null));

        auth.forceLogout(u);
        try {
            c1.getUser(null);
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final Exception e) {
            // ok
        }
        try {
            c2.getUser(null);
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final Exception e) {
            // ok
        }
    }
    @Test
    public void testSaveWithNullValues() throws IOException, RestServiceException {
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String externalCompany = "External JUnit company";
        final boolean external = true;
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String title = "Mrs";

        final User u = new User();
        u.setCompany(getCompanyId());
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setTitle(title);
        u.setActive(false);
        u.setExternal(external);
        u.setExternalCompany(externalCompany);
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.NormalUser);

        final String password = "password";
        final Long id = client.saveUser(u, getCompany(), password, false);

        final User tmp = new User();
        tmp.setId(id);
        client.saveUser(tmp, getCompany(), null, false);
        //save user by null values for check correct handling
        //null values.

        final User u2 = dao.findByEmail(u.getEmail());
        assertNotNull(u2);
        assertEquals(1, u2.getRoles().size());
        assertNotNull(u2.getCompanyId());
        assertEquals(firstName, u2.getFirstName());
        assertEquals(lastName, u2.getLastName());
        assertEquals(email, u2.getEmail());
        assertEquals(phone, u2.getPhone());
        assertEquals(externalCompany, u2.getExternalCompany());
        assertEquals(position, u2.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(title, u.getTitle());
        assertFalse(u.getActive());
        assertEquals(external, u.getExternal());
    }

    @Test
    public void testGetUsers() throws IOException, RestServiceException {
        final Company c = new Company();
        c.setName("Internal JUnit Company");
        c.setDescription("Test company");
        context.getBean(CompanyDao.class).save(c);

        final User u1 = createUser("u1@google.com", "A2", "LastA2", c);
        final User u2 = createUser("u2@google.com", "A1", "LastA1", c);
        u2.setExternal(true);
        u2.setExternalCompany("External JUnit Company");
        u2.setPosition("Driver");
        context.getBean(UserDao.class).save(u2);

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
        c.setName("JUnit Internal Company");
        c.setDescription("Test company");
        context.getBean(CompanyDao.class).save(c);

        final User u1 = createUser("u1@google.com", "A2", "LastA2", c);
        final User u2 = createUser("u2@google.com", "A1", "LastA1", c);
        u2.setExternal(true);
        u2.setPosition("Docker");
        u2.setExternalCompany("JUnit External Company");
        context.getBean(UserDao.class).save(u2);

        final String token = client.login("u1@google.com", "");
        client.setAuthToken(token);

        //test limit
        assertEquals(2, client.listUsers(1, 10000, null, null).size());
        assertEquals(1, client.listUsers(1, 1, null, null).size());
        assertEquals(1, client.listUsers(2, 1, null, null).size());

        assertEquals(u2.getId(), client.listUsers(1, 1,
                UserConstants.PROPERTY_FIRST_NAME, "asc").get(0).getId());
        assertEquals(u1.getId(), client.listUsers(1, 1,
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
        req.setPosition(position);
        req.setEmail(email);
        req.setPhone(phone);
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
        assertEquals(title, u.getTitle());
    }
    @Test
    public void testDeleteUser() throws IOException, RestServiceException {
        final User u = createUser("asuvorov@mail.ru", "Alexandr", "Suvorov", getCompany());
        client.deleteUser(u);

        assertNull(dao.findOne(u.getId()));
    }
    @Test
    public void testDeleteUserWithLiveRefs() throws IOException, RestServiceException {
        final User u = createUser("asuvorov@mail.ru", "Alexandr", "Suvorov", getCompany());

        //create references to user
        createNotificationSchedule(u, true);

        //create notification
        final Shipment s = createShipment(true);

        Alert a = new Alert();
        a.setDevice(s.getDevice());
        a.setShipment(s);
        a.setType(AlertType.LightOff);
        a = getContext().getBean(AlertDao.class).save(a);

        final Notification n = new Notification();
        n.setType(NotificationType.Alert);
        n.setUser(u);
        n.setIssue(a);
        getContext().getBean(NotificationDao.class).save(n);

        try {
            client.deleteUser(u);
        } catch (final RestServiceException e) {
            assertEquals(ErrorCodes.ENTITY_IN_USE, e.getErrorCode());
        }

        assertNotNull(dao.findOne(u.getId()));
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
        u.setCompany(company.getCompanyId());
        u.setPosition("Manager");
        u.setExternalCompany("External JUnit company");
        u.setDeviceGroup("AuthorizedDeviceGroup");
        u.setTitle("Mr");
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.Admin);
        context.getBean(AuthService.class).saveUser(u, "", false);
        return u;
    }
    /**
     * @return
     */
    protected UserRestClient createClient() {
        final UserRestClient c = new UserRestClient(UTC);
        c.setServiceUrl(getServiceUrl());
        c.setShipmentResolver(context.getBean(ShipmentResolver.class));
        return c;
    }
}
