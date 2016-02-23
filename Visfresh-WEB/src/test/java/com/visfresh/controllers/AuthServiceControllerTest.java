/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.RestClient;
import com.visfresh.controllers.restclient.UserRestClient;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.mock.MockEmailService;
import com.visfresh.services.AuthService;
import com.visfresh.services.DefaultAuthService;
import com.visfresh.services.DefaultReferenceResolver;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthServiceControllerTest extends AbstractRestServiceTest {
    private AuthService authService;
    private RestClient client = new RestClient();

    /**
     * Default constructor.
     */
    public AuthServiceControllerTest() {
        super();
    }


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
        authService = context.getBean(AuthService.class);
    }

    //@RequestMapping(value = "/login", method = RequestMethod.POST)
    //public @ResponseBody String login(@RequestBody final String loginRequest) {
    @Test
    public void testLogin() throws RestServiceException, IOException {
        final User user = new User();
        user.setEmail("a-" + (++lastLong) + "@b.c");
        final String password = "lkasdlfkj";
        user.setCompany(getCompany());

        authService.saveUser(user, password, false);
        final String token = client.login(user.getEmail(), password);
        assertNotNull(token);
    }
    @Test
    public void testCaseInsensitiveLogin() throws RestServiceException, IOException {
        final User user = new User();
        user.setEmail("a-" + (++lastLong) + "@b.c");
        final String password = "lkasdlfkj";
        user.setCompany(getCompany());

        authService.saveUser(user, password, false);
        final String token = client.login(user.getEmail().toUpperCase(), password);
        assertNotNull(token);
    }
    @Test
    public void testSupportOfMultipleSessions() throws RestServiceException, IOException {
        final String password = "lkasdlfkj";

        final User u1 = new User();
        u1.setEmail("user1@visfresh.com");
        u1.setCompany(getCompany());

        final User u2 = new User();
        u2.setEmail("user2@visfresh.com");
        u2.setCompany(getCompany());

        authService.saveUser(u1, password, false);
        authService.saveUser(u2, password, false);

        //login both
        final String token1 = client.login(u1.getEmail(), password);
        final String token2 = client.login(u2.getEmail(), password);

        final UserRestClient userClient = new UserRestClient(TimeZone.getDefault());
        userClient.setServiceUrl(client.getServiceUrl());

        userClient.setAuthToken(token1);
        assertEquals(u1.getEmail(), userClient.getUser(null).getEmail());
        userClient.setAuthToken(token2);
        assertEquals(u2.getEmail(), userClient.getUser(null).getEmail());
    }
    //@RequestMapping(value = "/getToken", method = RequestMethod.GET)
    //public @ResponseBody String getAuthToken(final HttpSession session) {
    public void _testGetToken() throws IOException, RestServiceException {
        final String token = client.getToken();
        assertNotNull(token);
    }
    //@RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String logout(@PathVariable final String authToken) {
    @Test
    public void testLogout() throws RestServiceException, IOException {
        client.logout(client.getAuthToken());
    }
    //@RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String refreshToken(@PathVariable final String authToken) {
    @Test
    public void testRefreshToken() throws IOException, RestServiceException {
        final String token = client.refreshToken();
        assertNotNull(token);
    }
    //uncomment this test where will need to test the token expiration.
    //@Test
    public void testExpiredToken() throws RestServiceException, IOException, InterruptedException {
        final String email = "a-" + (++lastLong) + "@b.c";

        final User user = new User();
        user.setEmail(email);
        final String password = "lkasdlfkj";
        user.setCompany(getCompany());

        authService.saveUser(user, password, false);

        final String token = client.login(user.getEmail(), password);
        for (int i = 1; i < DefaultAuthService.USER_LOGIN_LIMIT; i++) {
            //make small pause
            Thread.sleep(10l);
            client.login(user.getEmail(), password);
        }

        final UserRestClient userRest = new UserRestClient(UTC);
        userRest.setCompanyResolver(context.getBean(DefaultReferenceResolver.class));
        userRest.setServiceUrl(client.getServiceUrl());

        //check first token is alive
        userRest.setAuthToken(token);
        assertNotNull(userRest.getUser(null));

        //check next login expires old token
        client.login(user.getEmail(), password);
        try {
            userRest.getUser(null);
            throw new AssertionFailedError("Security exception expected");
        } catch (final Exception e) {
            //correct
        }
    }
    @Test
    public void testResetPassword() throws IOException, RestServiceException {
        //create user
        final User user = new User();
        user.setEmail("a-" + (++lastLong) + "@b.c");
        final String password = "lkasdlfkj";
        user.setCompany(getCompany());

        authService.saveUser(user, password, false);

        //attempt to reset password without start reset
        final String newpassword = "wpeijpgw";
        try {
            client.resetPassword(user.getEmail(), newpassword, "abrakadabra");
            throw new AssertionFailedError("Authentication error should occurring");
        } catch (final Exception e) {
            // is ok
        }

        //start password reset
        final String baseUrl = "http://abra.cadabra?";
        client.forgetRequest(user.getEmail(), baseUrl);

        //get security token
        final EmailMessage msg = context.getBean(MockEmailService.class).getMessages().remove(0);
        final String securityToken = getSecurityTokenFromEmail(msg, baseUrl);

        assertEquals(1, msg.getEmails().length);
        assertEquals(user.getEmail(), msg.getEmails()[0]);
        assertNotNull(msg.getSubject());
        assertTrue(msg.getSubject().length() > 0);

        //attempt to reset password with incorrect token
        try {
            client.resetPassword(user.getEmail(), newpassword, securityToken + "abrakadabra");
            throw new AssertionFailedError("Authentication error should occurring");
        } catch (final Exception e) {
            // is ok
        }

        //attempt to reset password with correct token
        client.resetPassword(user.getEmail(), newpassword, securityToken);

        final String token = client.login(user.getEmail(), newpassword);
        assertNotNull(token);
    }
    /**
     * @param msg email message.
     * @param baseUrl
     * @return
     */
    private String getSecurityTokenFromEmail(final EmailMessage msg, final String baseUrl) {
        final String body = msg.getMessage();
        final int offset = body.indexOf(baseUrl) + baseUrl.length();

        final String paramString = body.substring(offset, body.indexOf(' ', offset));

        final Map<String, String> params = new HashMap<>();
        for (final String pairStr : paramString.split("&")) {
            final String[] pair = pairStr.split("=");
            params.put(pair[0].trim(), pair[1].trim());
        }

        return params.get("token");
    }
}
