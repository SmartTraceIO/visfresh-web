/**
 *
 */
package com.visfresh.controllers;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.UserConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.SaveUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.services.lists.ExpandedListUserItem;
import com.visfresh.services.lists.ShortListUserItem;
import com.visfresh.utils.HashGenerator;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("User")
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
     * @param userId ID of user for request info.
     * @return user info
     */
    @RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    public JsonObject getUser(@PathVariable final String authToken,
            final @RequestParam(required = false) Long userId) {
        try {
            final User user = getLoggedInUser(authToken);
            if (userId != null) {
                security.checkCanGetUserInfo(user, userId);
            }

            final User u = dao.findOne(userId == null ? user.getId() : userId);
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
    public JsonObject getUsers(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanListUsers(user);

            final int total = dao.getEntityCount(user.getCompany(), null);
            final UserSerializer ser = getUserSerializer(user);
            final JsonArray array = new JsonArray();

            final List<User> users = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultListShipmentsSortingOrder(), 2),
                    page,
                    null);

            for (final User u : users) {
                array.add(ser.toJson(new ExpandedListUserItem(u)));
            }
            return createListSuccessResponse(array, total);
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
    @RequestMapping(value = "/listUsers/{authToken}", method = RequestMethod.GET)
    public JsonObject listUsers(@PathVariable final String authToken,
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

            final List<ShortListUserItem> users = getUserListItems(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultListShipmentsSortingOrder(), 2),
                    null,
                    page);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final ShortListUserItem s : users) {
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
    private List<ShortListUserItem> getUserListItems(final Company company,
            final Sorting sorting, final Filter filter, final Page page) {
        final List<ShortListUserItem> result = new LinkedList<ShortListUserItem>();
        for (final User u : dao.findByCompany(company, sorting, page, filter)) {
            result.add(new ShortListUserItem(u));
        }
        return result;
    }

    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
            PROPERTY_EMAIL,
            PROPERTY_FIRST_NAME,
            PROPERTY_LAST_NAME,
            PROPERTY_POSITION,
            PROPERTY_PHONE,
            PROPERTY_ACTIVE,
            PROPERTY_COMPANY_ID,
            PROPERTY_COMPANY_NAME
        };
    }
    /**
     * @param authToken authentication token.
     * @param req save user request.
     * @return user ID.
     */
    @RequestMapping(value = "/saveUser/{authToken}", method = RequestMethod.POST)
    public JsonObject saveUser(@PathVariable final String authToken,
            final @RequestBody JsonObject req) {
        try {
            final User user = getLoggedInUser(authToken);
            final SaveUserRequest r = getUserSerializer(user).parseSaveUserRequest(req);
            security.checkCanManageUsers(user, r.getCompany());

            final User newUser = r.getUser();
            newUser.setCompany(r.getCompany() == null ? user.getCompany() : r.getCompany());
            security.checkCanAssignRoles(user, newUser.getRoles());
            authService.saveUser(newUser, r.getPassword());

            return createIdResponse("userId", newUser.getId());
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/deleteUser/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteUser(@PathVariable final String authToken,
            final @RequestParam Long userId) {
        try {
            final User user = getLoggedInUser(authToken);

            final User deletedUser = dao.findOne(userId);
            security.checkCanManageUsers(user, deletedUser.getCompany());

            dao.delete(userId);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/updateUserDetails/{authToken}", method = RequestMethod.POST)
    public JsonObject updateUserDetails(@PathVariable final String authToken,
            @RequestBody final JsonObject body) {
        try {
            final User user = getLoggedInUser(authToken);
            final UpdateUserDetailsRequest req = getUserSerializer(user).parseUpdateUserDetailsRequest(
                    body);

            security.checkUpdateUserDetails(user, req.getUser());

            final User u = dao.findOne(req.getUser());
            if (req.getFirstName() != null) {
                u.setFirstName(req.getFirstName());
            }
            if (req.getLastName() != null) {
                u.setLastName(req.getLastName());
            }
            if (req.getEmail() != null) {
                u.setEmail(req.getEmail());
            }
            if (req.getPhone() != null) {
                u.setPhone(req.getPhone());
            }
            if (req.getPosition() != null) {
                u.setPosition(req.getPosition());
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
            if (req.getMeasurementUnits() != null) {
                u.setMeasurementUnits(req.getMeasurementUnits());
            }
            if (req.getLanguage() != null) {
                u.setLanguage(req.getLanguage());
            }
            if (req.getScale() != null) {
                u.setScale(req.getScale());
            }
            if (req.getTitle() != null) {
                u.setTitle(req.getTitle());
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
