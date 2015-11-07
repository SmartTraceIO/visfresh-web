/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.User;
import com.visfresh.mock.MockAuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthServiceControllerTest extends AbstractRestServiceTest {

    private MockAuthService authService;

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
        authService = context.getBean(MockAuthService.class);
    }

    //@RequestMapping(value = "/login", method = RequestMethod.POST)
    //public @ResponseBody String login(@RequestBody final String loginRequest) {
    @Test
    public void testLogin() throws RestServiceException, IOException {
        final User user = new User();
        user.setLogin("aldsklksadf");
        final String password = "lkasdlfkj";

        authService.createUser(user, password);
        final String token = facade.login(user.getLogin(), password);
        assertNotNull(token);
    }
    //@RequestMapping(value = "/getToken", method = RequestMethod.GET)
    //public @ResponseBody String getAuthToken(final HttpSession session) {
    public void _testGetToken() throws IOException, RestServiceException {
        final String token = facade.getToken();
        assertNotNull(token);
    }
    //@RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String logout(@PathVariable final String authToken) {
    @Test
    public void testLogout() throws RestServiceException, IOException {
        facade.logout(facade.getAuthToken());
    }
    //@RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String refreshToken(@PathVariable final String authToken) {
    @Test
    public void testRefreshToken() throws IOException, RestServiceException {
        final String token = facade.refreshToken();
        facade.setAuthToken(token);
        assertNotNull(token);
    }
}
