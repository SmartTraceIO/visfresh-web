/**
 *
 */
package com.visfresh.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.AuthTokenSerializer;
import com.visfresh.services.AuthToken;
import com.visfresh.services.RestServiceException;

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
     * @return authorization token.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public JsonObject login(
            final @RequestParam String email,
            final @RequestParam String password,
            final @RequestParam(required = false) String instance)throws RestServiceException {
            final AuthToken token = authService.login(email, password, instance);
        log.debug("Login successes for " + email);
        final User user = authService.getUserForToken(token.getToken());
        return createSuccessResponse(getSerializer(user).toJson(token));
    }
    /**
     * @param authToken authentication token.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject logout() {
        authService.logout(getSession().getToken().getToken());
        return createSuccessResponse(null);
    }
    /**
     * @param authToken old authentication token.
     * @return refreshed authorization token.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject refreshToken()throws RestServiceException {
        final String authToken = getSession().getToken().getToken();
        final User user = getLoggedInUser();
        final AuthToken token = authService.refreshToken(authToken);
        return createSuccessResponse(getSerializer(user).toJson(token));
    }
    /**
     * @param email user email.
     * @param baseUrl base URL for final page.
     * @return JSON response.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/forgetRequest", method = RequestMethod.GET)
    public JsonObject startResetPassword(@RequestParam final String email, @RequestParam final String baseUrl)throws RestServiceException {
        authService.startResetPassword(email, baseUrl);
        return createSuccessResponse(null);
    }
    /**
     * @param email user email.
     * @param password user password.
     * @param token security token
     * @return
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public JsonObject resetPassword(
            @RequestParam final String email,
            @RequestParam final String password,
            @RequestParam final String token
            )throws RestServiceException {
        authService.resetPassword(email, password, token);
        return createSuccessResponse(null);
    }
    /**
     * @param user
     * @return
     */
    private AuthTokenSerializer getSerializer(final User user) {
        return new AuthTokenSerializer(user.getTimeZone());
    }
}
