/**
 *
 */
package com.visfresh.controllers;

import java.util.LinkedList;
import java.util.List;

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

import com.google.gson.JsonArray;
import com.visfresh.constants.UserConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.services.lists.ListUserItem;
import com.visfresh.utils.HashGenerator;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("User")
@RequestMapping("/rest")
public class UserController extends AbstractController implements UserConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    /**
     * User DAO.
     */
    @Autowired
    private UserDao dao;
    @Autowired
    private ShipmentResolver shipmentResolver;
    @Autowired
    private CompanyResolver companyResolver;

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
            return createSuccessResponse(u == null ? null : getUserSerializer(user).toJson(u));
        } catch (final Exception e) {
            log.error("Failed to get user info", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sc sort column
     * @param so sort order.
     * @return
     */
    @RequestMapping(value = "/getUsers/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getUsers(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanListUsers(user);

            final UserSerializer ser = getUserSerializer(user);

            final List<ListUserItem> shipments = getUserListItems(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultListShipmentsSortingOrder()),
                    null,
                    page);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final ListUserItem s : shipments) {
                array.add(ser.toJson(s));
            }
            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get user info", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param company
     * @param sorting
     * @param filter
     * @param page
     * @return
     */
    private List<ListUserItem> getUserListItems(final Company company,
            final Sorting sorting, final Filter filter, final Page page) {
        final List<ListUserItem> result = new LinkedList<ListUserItem>();
        for (final User u : dao.findByCompany(company, sorting, page, filter)) {
            result.add(new ListUserItem(u));
        }
        return result;
    }

    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            PROPERTY_LOGIN,
            PROPERTY_FULL_NAME
        };
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
            final CreateUserRequest r = getUserSerializer(user).parseCreateUserRequest(getJSonObject(req));
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

            final UserProfile profile = dao.getProfile(user);
            return createSuccessResponse(getUserSerializer(user).toJson(profile));
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

            final UserProfile p = getUserSerializer(user).parseUserProfile(getJSon(profile));
            dao.saveProfile(user, p);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/updateUserDetails/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String updateUserDetails(@PathVariable final String authToken,
            @RequestBody final String body) {
        try {
            final User user = getLoggedInUser(authToken);
            final UpdateUserDetailsRequest req = getUserSerializer(user).parseUpdateUserDetailsRequest(getJSon(body));

            security.checkUpdateUserDetails(user, req.getUser());

            final User u = dao.findOne(req.getUser());
            if (req.getFullName() != null) {
                u.setFullName(req.getFullName());
            }
            if (req.getPassword() != null) {
                u.setPassword(HashGenerator.createMd5Hash(req.getPassword()));
            }
            if (req.getTemperatureUnits() != null) {
                u.setTemperatureUnits(req.getTemperatureUnits());
            }
            if (req.getTimeZone() != null) {
                u.setTimeZone(req.getTimeZone());
            }
            dao.save(u);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user user.
     * @return serializer.
     */
    protected UserSerializer getUserSerializer(final User user) {
        final UserSerializer ser = new UserSerializer(
                user == null ? SerializerUtils.UTС : user.getTimeZone());
        ser.setCompanyResolver(companyResolver);
        ser.setShipmentResolver(shipmentResolver);
        return ser;
    }
}
