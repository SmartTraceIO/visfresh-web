/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.RestClient;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
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

        authService.saveUser(user, password);
        final String token = client.login(user.getEmail(), password);
        assertNotNull(token);
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
}
