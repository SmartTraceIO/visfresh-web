/**
 *
 */
package au.smarttrace.ctrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.ctrl.client.AuthClient;
import au.smarttrace.ctrl.client.ServiceException;
import au.smarttrace.ctrl.client.UserClient;
import au.smarttrace.ctrl.req.SaveUserRequest;
import au.smarttrace.ctrl.runner.ControllerTestRunner;
import au.smarttrace.ctrl.runner.ServiceUrlHolder;
import au.smarttrace.junit.categories.ControllerTest;
import au.smarttrace.user.UsersService;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(ControllerTest.class)
@RunWith(ControllerTestRunner.class)
public class UsersControllerTest {
    private AuthClient authClient = new AuthClient();
    private UserClient userClient = new UserClient();

    @Autowired
    private ApplicationContext context;
    @Autowired
    private UsersService userService;

    /**
     * Default constructor.
     */
    public UsersControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        final String serviceUrl = context.getBean(ServiceUrlHolder.class).getServiceUrl();
        authClient.setServiceUrl(serviceUrl);
        userClient.setServiceUrl(serviceUrl);
    }

    @Test
    public void createUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);

        userClient.createUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(1, actual.getRoles().size());
        assertTrue(actual.getRoles().contains(Roles.SmartTraceAdmin));
    }
    @Test
    public void createUserNotAdmin() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.BasicUser);
        login(u.getEmail(), "password");

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail("newemail@junit.org");
        req.setFirstName("New User");
        req.setPassword("12345");

        try {
            userClient.createUser(req);
            throw new AssertionFailedError("Access denied error should be thrown");
        } catch (final Exception e) {
            //Ok
        }

        //check can login
        assertNull(userService.findUserByEmailPassword(req.getEmail(), req.getPassword()));
    }
    @Test
    public void deleteUser() throws IOException, ServiceException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "otheruser@junit.org";
        final String password = "pass";

        createUser(email, password, Roles.SmartTraceAdmin);
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);

        userClient.deleteUser(actual.getId());
        assertNull(userService.findUserByEmailPassword(email, password));
    }
    @Test
    public void deleteUserNotAdmin() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.BasicUser);
        login(u.getEmail(), "password");

        final String email = "otheruser@junit.org";
        final String password = "pass";

        createUser(email, password, Roles.SmartTraceAdmin);
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);

        try {
            userClient.deleteUser(actual.getId());
            throw new AssertionFailedError("Bad credentials exception should be thrown");
        } catch (final Exception e) {
            //Ok
        }
        assertNotNull(userService.findUserByEmailPassword(email, password));
    }
    @Test
    public void updateUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";
        createUser(email, "oldpassword", Roles.Admin, Roles.SmartTraceAdmin);

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);

        userClient.updateUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(2, actual.getRoles().size());
    }
    @Test
    public void updateUserSelf() throws IOException, ServiceException {
        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";
        final User u = createUser(email, "oldpassword", Roles.SmartTraceAdmin);
        login(u.getEmail(), "oldpassword");

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);

        userClient.updateUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(1, actual.getRoles().size());
    }
    @Test
    public void updateUserNotChangePassword() throws IOException, ServiceException {
        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final User u = createUser(email, "oldpassword", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "oldpassword");

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);

        userClient.updateUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, "oldpassword");
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(2, actual.getRoles().size());
    }
    @Test
    public void updateUserNotAdminLeftUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";
        createUser(email, "oldpassword", Roles.Admin, Roles.SmartTraceAdmin);

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);

        try {
            userClient.updateUser(req);
            throw new AssertionFailedError("Bad credentials exception should be thrown");
        } catch (final Exception e) {
            //Ok
        }
    }

    /**
     * @param email
     * @param password
     * @throws IOException
     * @throws ServiceException
     */
    private void login(final String email, final String password) throws ServiceException, IOException {
        final String token = authClient.login(email, password).getToken().getToken();
        authClient.setAccessToken(token);
        userClient.setAccessToken(token);
    }
    /**
     * @param email email address.
     * @param password password.
     * @param roles user roles.
     * @return new user.
     */
    private User createUser(final String email, final String password, final String... roles) {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName("Java");
        u.setLastName("Developer");
        for (final String role : roles) {
            u.getRoles().add(role);
        }
        userService.createUser(u, password);
        return u;
    }
    @After
    public void tearDown() {
        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update("delete from users", new HashMap<>());
    }
}
