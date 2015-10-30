/**
 *
 */
package com.visfresh.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("User")
@RequestMapping("/rest")
public class UserController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;

    /**
     * Default constructor.
     */
    public UserController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param username name of user for request info.
     * @return user info
     */
    @RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getUser(@PathVariable final String authToken,
            final @RequestParam String username) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanGetUserInfo(user, username);

            final User u = authService.getUser(username);
            return createSuccessResponse(u == null ? null : getSerializer().toJson(u));
        } catch (final Exception e) {
            log.error("Failed to get user info", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/createUser/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String createUser(@PathVariable final String authToken,
            final @RequestBody String req) {
        try {
            final User user = getLoggedInUser(authToken);
            final CreateUserRequest r = getSerializer().parseCreateUserRequest(getJSonObject(req));
            security.checkCanCreateUser(user, r);

            final User newUser = r.getUser();
            newUser.setCompany(r.getCompany());
            authService.createUser(newUser, r.getPassword());

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return profile for given user.
     */
    @RequestMapping(value = "/getProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getProfile(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkGetProfile(user);

            final UserProfile profile = restService.getProfile(user);
            return createSuccessResponse(getSerializer().toJson(profile));
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param profile to save.
     * @return profile for given user.
     */
    @RequestMapping(value = "/saveProfile/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String getProfile(@PathVariable final String authToken,
            @RequestBody final String profile) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkSaveProfile(user);

            final UserProfile p = getSerializer().parseUserProfile(getJSon(profile));
            restService.saveUserProfile(user, p);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
}
