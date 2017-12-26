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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Company;
import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.ctrl.client.AuthClient;
import au.smarttrace.ctrl.client.ServiceException;
import au.smarttrace.ctrl.client.UserClient;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.req.SaveUserRequest;
import au.smarttrace.ctrl.runner.ControllerTestRunner;
import au.smarttrace.ctrl.runner.ServiceUrlHolder;
import au.smarttrace.junit.categories.ControllerTest;
import au.smarttrace.user.GetUsersRequest;
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
    private UserClient client = new UserClient();

    @Autowired
    private ApplicationContext context;
    @Autowired
    private UsersService userService;
    private Company company;

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
        client.setServiceUrl(serviceUrl);

        company = new Company();
        company.setName("JUnit Company");
        context.getBean(CompaniesDao.class).createCompany(company);
    }

    @Test
    public void testCreateUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);
        req.setCompany(company.getId());
        req.getRoles().add(Roles.SmartTraceAdmin);

        client.createUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(1, actual.getRoles().size());
        assertTrue(actual.getRoles().contains(Roles.SmartTraceAdmin));
    }
    @Test
    public void testCreateUserNotAdmin() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.BasicUser);
        login(u.getEmail(), "password");

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail("newemail@junit.org");
        req.setFirstName("New User");
        req.setPassword("12345");

        try {
            client.createUser(req);
            throw new AssertionFailedError("Access denied error should be thrown");
        } catch (final Exception e) {
            //Ok
        }

        //check can login
        assertNull(userService.findUserByEmailPassword(req.getEmail(), req.getPassword()));
    }
    @Test
    public void testDeleteUser() throws IOException, ServiceException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "otheruser@junit.org";
        final String password = "pass";

        createUser(email, password, Roles.SmartTraceAdmin);
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);

        client.deleteUser(actual.getId());
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
            client.deleteUser(actual.getId());
            throw new AssertionFailedError("Bad credentials exception should be thrown");
        } catch (final Exception e) {
            //Ok
        }
        assertNotNull(userService.findUserByEmailPassword(email, password));
    }
    @Test
    public void testUpdateUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.Admin, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final String email = "newemail@junit.org";
        final String firstName = "New User";
        final String password = "12345";
        final User user = createUser(email, "oldpassword", Roles.Admin, Roles.SmartTraceAdmin);

        final SaveUserRequest req = new SaveUserRequest();
        req.setEmail(email);
        req.setFirstName(firstName);
        req.setPassword(password);
        req.setId(user.getId());
        req.setCompany(user.getCompany());
        req.getRoles().addAll(user.getRoles());

        client.updateUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, password);
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(2, actual.getRoles().size());
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
        req.setCompany(u.getCompany());
        req.setId(u.getId());
        req.getRoles().addAll(u.getRoles());

        client.updateUser(req);

        //check can login
        final User actual = userService.findUserByEmailPassword(email, "oldpassword");
        assertNotNull(actual);
        assertEquals(email, actual.getEmail());
        assertEquals(firstName, actual.getFirstName());
        assertEquals(2, actual.getRoles().size());
    }
    @Test
    public void updateUserNotAdminLeftUser() throws ServiceException, IOException {
        final User u = createUser("u1@junit.org", "password", Roles.BasicUser);
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
            client.updateUser(req);
            throw new AssertionFailedError("Bad credentials exception should be thrown");
        } catch (final Exception e) {
            //Ok
        }
    }
    @Test
    public void testSorting() throws IOException, ServiceException {
        //do test
        final User u1  = createUserByNames("FirstName-A", "LirstName-B", "b@junit.org", Roles.SmartTraceAdmin);
        login(u1.getEmail(), "password");
        final User u2  = createUserByNames("FirstName-B", "LirstName-A", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();

        //by first name
        req.getOrders().add(new Order("firstName", true));
        assertEquals(u1.getId(), client.getUsers(req).getItems().get(0).getId());

        //by last name
        req.getOrders().clear();
        req.getOrders().add(new Order("lastName", true));
        assertEquals(u2.getId(), client.getUsers(req).getItems().get(0).getId());

        //by email
        req.getOrders().clear();
        req.getOrders().add(new Order("email", true));
        assertEquals(u2.getId(), client.getUsers(req).getItems().get(0).getId());
    }
    @Test
    public void testSortingDesc() throws ServiceException, IOException {
        final User u = createUserByNames("FirstName-A", "LirstName-B", "b@junit.org", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");
        final User u2  = createUserByNames("FirstName-B", "LirstName-A", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();

        //by first name
        req.getOrders().add(new Order("firstName", false));
        assertEquals(u2.getId(), client.getUsers(req).getItems().get(0).getId());

        //by last name
        req.getOrders().clear();
        req.getOrders().add(new Order("lastName", true));
        assertEquals(u2.getId(), client.getUsers(req).getItems().get(0).getId());
    }
    @Test
    public void testMultiFieldSorting() throws ServiceException, IOException {
        final User u1  = createUserByNames("FirstName-A", "LirstName", "a1@junit.org", Roles.SmartTraceAdmin);
        login(u1.getEmail(), "password");
        final User u2  = createUserByNames("FirstName-B", "LirstName", "a2@junit.org");

        final User u3  = createUserByNames("FirstName-A", "LirstName", "b1@junit.org");
        final User u4  = createUserByNames("FirstName-B", "LirstName", "b2@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.getOrders().add(new Order("firstName", false));
        req.getOrders().add(new Order("email", true));

        final List<User> users = client.getUsers(req).getItems();

        assertEquals(u2.getId(), users.get(0).getId());
        assertEquals(u4.getId(), users.get(1).getId());
        assertEquals(u1.getId(), users.get(2).getId());
        assertEquals(u3.getId(), users.get(3).getId());
    }
    @Test
    public void testLimits() throws IOException, ServiceException {
        //do tests
        final User u = createUserByNames("FirstName-A", "LirstName", "a1@junit.org", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");
        createUserByNames("FirstName-B", "LirstName", "a2@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.setPageSize(1);

        assertEquals(1, client.getUsers(req).getItems().size());
        req.setPage(1);
        assertEquals(1, client.getUsers(req).getItems().size());
        req.setPage(2);
        assertEquals(0, client.getUsers(req).getItems().size());

        assertEquals(2, client.getUsers(req).getTotalCount());
    }
    @Test
    public void testFilterByCompanies() throws ServiceException, IOException {
        //do test
        final Long c1 = createSimpleCompany("C1").getId();
        final Long c2 = createSimpleCompany("C1").getId();
        final Long c3 = createSimpleCompany("C1").getId();

        final User u = createUser("u1@developer.com", c1, Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");
        createUser("u2@developer.com", c2);

        final GetUsersRequest req = new GetUsersRequest();

        assertEquals(2, client.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c3);
        assertEquals(0, client.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c1);
        assertEquals(1, client.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c2);
        assertEquals(2, client.getUsers(req).getItems().size());
    }
    @Test
    public void testFilterByName() throws ServiceException, IOException {
        final User u = createUserByNames("FirstName-A", "LastName", "a@junit.org", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final GetUsersRequest req = new GetUsersRequest();
        req.setNameFilter("A B First");
        assertEquals(1, client.getUsers(req).getItems().size());

        req.setNameFilter("C D Last");
        assertEquals(1, client.getUsers(req).getItems().size());

        req.setNameFilter("Third ED");
        assertEquals(0, client.getUsers(req).getItems().size());
    }
    @Test
    public void testFilterByEmail() throws IOException, ServiceException {
        //do tests
        final User u = createUserByNames("FirstName-A", "LirstName", "a@junit.org", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final GetUsersRequest req = new GetUsersRequest();
        req.setEmailFilter("a");
        assertEquals(1, client.getUsers(req).getItems().size());

        req.setEmailFilter("@junit.org");
        assertEquals(1, client.getUsers(req).getItems().size());

        req.setEmailFilter("b@junit.org");
        assertEquals(0, client.getUsers(req).getItems().size());
    }
    @Test
    public void testGetRoles() throws IOException, ServiceException {
        //do tests
        final User u = createUser("a@junit.org", "password", Roles.SmartTraceAdmin);
        login(u.getEmail(), "password");

        final List<?> roles = client.getRoles();
        assertTrue(roles.size() > 0);
    }
    /**
     * @param name
     * @return
     */
    private Company createSimpleCompany(final String name) {
        final Company c = new Company();
        c.setName(name);
        context.getBean(CompaniesDao.class).createCompany(c);
        return c;
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
        client.setAccessToken(token);
    }
    /**
     * @param email email address.
     * @param password password.
     * @param roles user roles.
     * @return new user.
     */
    private User createUser(final String email, final String password, final String... roles) {
        return createUser(email, password, company.getId(), roles);
    }
    /**
     * @param email email address.
     * @param password password.
     * @param companyId company ID.
     * @param roles roles.
     * @return new user.
     */
    protected User createUser(final String email, final String password, final Long companyId, final String... roles) {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName("Java");
        u.setLastName("Developer");
        u.setCompany(companyId);
        for (final String role : roles) {
            u.getRoles().add(role);
        }
        userService.createUser(u, password);
        return u;
    }
    /**
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    private User createUserByNames(final String firstName, final String lastName, final String email, final String... roles) {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setCompany(company.getId());
        for (final String role : roles) {
            u.getRoles().add(role);
        }
        userService.createUser(u, "password");
        return u;
    }
    /**
     * @param email email address.
     * @param companyId company ID.
     * @return new user.
     */
    private User createUser(final String email, final Long companyId, final String... roles) {
        return createUser(email, "password", companyId, roles);
    }

    @After
    public void tearDown() {
        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update("delete from users", new HashMap<>());
        jdbc.update("delete from companies", new HashMap<>());
    }
}
