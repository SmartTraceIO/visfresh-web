/**
 *
 */
package au.smarttrace.ctrl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

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

import au.smarttrace.Company;
import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.ctrl.client.AuthClient;
import au.smarttrace.ctrl.client.ServiceException;
import au.smarttrace.ctrl.runner.ControllerTestRunner;
import au.smarttrace.ctrl.runner.ServiceUrlHolder;
import au.smarttrace.junit.categories.ControllerTest;
import au.smarttrace.security.AccessException;
import au.smarttrace.security.AccessService;
import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;
import au.smarttrace.user.UsersService;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(ControllerTest.class)
@RunWith(ControllerTestRunner.class)
public class AuthControllerTest {
    private AuthClient client;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private UsersService userService;
    @Autowired
    private AccessService accessService;
    @Autowired
    private CompaniesDao companiesDao;

    private Company company;
    /**
     * Default constructor.
     */
    public AuthControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        client = new AuthClient();
        client.setServiceUrl(context.getBean(ServiceUrlHolder.class).getServiceUrl());

        //create company
        company = new Company();
        company.setName("JUnit Company");
        companiesDao.createCompany(company);
    }

    @Test
    public void testLogin() throws ServiceException, IOException {
        final String login = "u1@junit.org";
        final String password = "p1";
        userService.createUser(createUser(login), password);

        //try unsuccessfully login
        try {
            client.login(login, "ppp");
            throw new AssertionFailedError("Exception should be throws according unsuccessfully login");
        } catch (final Exception e) {
            //OK
        }

        //try not existing user
        try {
            client.login("u3@junit.org", "p1");
            throw new AssertionFailedError("Exception should be throws according unexisting user");
        } catch (final Exception e) {
            //OK
        }

        //try successfull login
        final AuthInfo info = client.login(login, password);
        assertNotNull(info);

        final AuthInfo auth = accessService.getAuthForToken(info.getToken().getToken());
        assertNotNull(auth);
        assertNotNull(auth.getToken());
        assertNotNull(auth.getUser());
    }
    @Test
    public void testLogout() throws AccessException, ServiceException, IOException {
        final String login = "u1@junit.org";
        final String password = "p1";
        userService.createUser(createUser(login), password);

        final AuthInfo info = accessService.login(login, password);
        client.setAccessToken(info.getToken().getToken());

        client.logout();
        assertNull(accessService.getAuthForToken(info.getToken().getToken()));
    }
    @Test
    public void testRefreshToken() throws IOException, ServiceException, AccessException {
        final String login = "u1@junit.org";
        final String password = "p1";
        userService.createUser(createUser(login), password);

        //try unsuccessfully refresh token.
        try {
            client.refreshToken();
            throw new AssertionFailedError("Exception should be throws according unsuccessfully refresh token");
        } catch (final Exception e) {
            //OK
        }

        final AuthInfo info = accessService.login(login, password);
        client.setAccessToken(info.getToken().getToken());

        final AccessToken token = client.refreshToken();
        assertNotSame(info.getToken().getToken(), token.getToken());
    }
    @Test
    public void testGetAuthInfo() throws IOException, ServiceException, AccessException {
        final String login = "u1@junit.org";
        final String password = "p1";
        userService.createUser(createUser(login), password);

        //try unsuccessfully refresh token.
        try {
            client.getAuthInfo();
            throw new AssertionFailedError("Exception should be throws according unsuccessfully refresh token");
        } catch (final Exception e) {
            //OK
        }

        final AuthInfo info = accessService.login(login, password);
        client.setAccessToken(info.getToken().getToken());

        assertNotNull(client.getAuthInfo());
    }

    /**
     * @param email
     * @return new user not saved into DB.
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName("Java");
        u.setLastName("Developer");
        u.setCompany(company.getId());
        u.getRoles().add(Roles.SmartTraceAdmin);
        return u;
    }
    @After
    public void tearDown() {
        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update("delete from users", new HashMap<>());
        jdbc.update("delete from companies", new HashMap<>());
    }
}
