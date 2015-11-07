/**
 *
 */
package com.visfresh.controllers;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Authentication")
@RequestMapping("/rest")
public class AuthenticationController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    /**
     * Default constructor.
     */
    public AuthenticationController() {
        super();
    }
    //authentication
    /**
     * @param login login
     * @param password password.
     * @return authorization token.
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public @ResponseBody String login(final @RequestParam String login, final @RequestParam String password) {
        try {
            final AuthToken token = authService.login(login, password);
            final User user = authService.getUser(login);
            return createSuccessResponse(getSerializer(user).toJson(token));
        } catch (final Exception e) {
            log.error("Faile to log in " + login, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param session this method can be used if the user was authorized out of the controller
     * and wants to obtain an security token for REST service.
     * @return authorization token.
     */
    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public @ResponseBody String getAuthToken(final HttpSession session) {
        try {
            final AuthToken token = authService.attachToExistingSession(session);
            final User user = authService.getUserForToken(token.getToken());
            return createSuccessResponse(getSerializer(user).toJson(token));
        } catch (final Exception e) {
            log.error("Failed to get auth token. Possible not user has logged in", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     */
    @RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String logout(@PathVariable final String authToken) {
        authService.logout(authToken);
        return createSuccessResponse(null);
    }
    /**
     * @param authToken old authentication token.
     * @return refreshed authorization token.
     */
    @RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String refreshToken(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            final AuthToken token = authService.refreshToken(user);
            return createSuccessResponse(getSerializer(user).toJson(token));
        } catch (final Exception e) {
            log.error("Failed to refresh token " + authToken, e);
            return createErrorResponse(e);
        }
    }
}
