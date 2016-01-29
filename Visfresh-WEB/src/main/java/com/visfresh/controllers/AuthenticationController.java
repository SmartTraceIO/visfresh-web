/**
 *
 */
package com.visfresh.controllers;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.entities.User;
import com.visfresh.io.json.AuthTokenSerializer;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Authentication")
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
     * @param email login
     * @param password password.
     * @param session HTTP session.
     * @return authorization token.
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public JsonObject login(
            final @RequestParam String email,
            final @RequestParam String password,
            final HttpSession session) {
        try {
            final AuthToken oldToken = (AuthToken) session.getAttribute("authToken");
            if (oldToken != null) {
                log.debug("Cleaning old auth token for " + email);
                authService.logout(oldToken.getToken());
            }

            final AuthToken token = authService.login(email, password);
            log.debug("Login successes for " + email);

            //save auth token to session
            session.setAttribute("authToken", token);

            final User user = authService.getUserForToken(token.getToken());
            return createSuccessResponse(getSerializer(user).toJson(token));
        } catch (final Exception e) {
            log.error("Faile to log in " + email, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     */
    @RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    public JsonObject logout(@PathVariable final String authToken) {
        authService.logout(authToken);
        return createSuccessResponse(null);
    }
    /**
     * @param authToken old authentication token.
     * @return refreshed authorization token.
     */
    @RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    public JsonObject refreshToken(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            final AuthToken token = authService.refreshToken(authToken);
            return createSuccessResponse(getSerializer(user).toJson(token));
        } catch (final Exception e) {
            log.error("Failed to refresh token " + authToken, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param email user email.
     * @param baseUrl base URL for final page.
     * @return JSON response.
     */
    @RequestMapping(value = "/forgetRequest", method = RequestMethod.GET)
    public JsonObject startResetPassword(@RequestParam final String email, @RequestParam final String baseUrl) {
        try {
            authService.startResetPassword(email, baseUrl);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to start of reset password for " + email, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param email user email.
     * @param password user password.
     * @param token security token
     * @return
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public JsonObject resetPassword(
            @RequestParam final String email,
            @RequestParam final String password,
            @RequestParam final String token
            ) {
        try {
            authService.resetPassword(email, password, token);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to reset password for " + email, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private AuthTokenSerializer getSerializer(final User user) {
        return new AuthTokenSerializer(user.getTimeZone());
    }
}
