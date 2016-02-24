/**
 *
 */
package com.visfresh.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.UserConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.dao.impl.NotificationDaoImpl;
import com.visfresh.dao.impl.NotificationScheduleDaoImpl;
import com.visfresh.entities.Company;
import com.visfresh.entities.ReferenceInfo;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.SaveUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.lists.ExpandedListUserItem;
import com.visfresh.lists.ShortListUserItem;
import com.visfresh.utils.HashGenerator;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

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
            PROPERTY_EXTERNAL_COMPANY,
            PROPERTY_POSITION,
            PROPERTY_PHONE,
            PROPERTY_ACTIVE,
            PROPERTY_INTERNAL_COMPANY_ID
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
            security.checkCanManageUsers(user, r.getUser().getCompany());

            User newUser = r.getUser();
            if (newUser.getId() != null) {
                newUser = dao.findOne(newUser.getId());
                checkCompanyAccess(user, newUser);

                newUser = merteUsers(newUser, r.getUser());
            } else {
                if (newUser.getRoles() == null) {
                    newUser.setRoles(new HashSet<Role>());
                }
            }

            if (newUser.getCompany() == null) {
                newUser.setCompany(user.getCompany());
            }
            security.checkCanAssignRoles(user, newUser.getRoles());
            authService.saveUser(newUser, r.getPassword(), Boolean.TRUE.equals(r.getResetOnLogin()));

            return createIdResponse("userId", newUser.getId());
        } catch (final Exception e) {
            log.error("Failed to save user", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param oldUser old user.
     * @param newUser new user.
     * @return
     */
    private User merteUsers(final User oldUser, final User newUser) {
        if (newUser.getCompany() != null) {
            oldUser.setCompany(newUser.getCompany());
        }
        if (newUser.getDeviceGroup() != null) {
            oldUser.setDeviceGroup(newUser.getDeviceGroup());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getExternalCompany() != null) {
            oldUser.setExternalCompany(newUser.getExternalCompany());
        }
        if (newUser.getFirstName() != null) {
            oldUser.setFirstName(newUser.getFirstName());
        }
        if (newUser.getLanguage() != null) {
            oldUser.setLanguage(newUser.getLanguage());
        }
        if (newUser.getLastName() != null) {
            oldUser.setLastName(newUser.getLastName());
        }
        if (newUser.getMeasurementUnits() != null) {
            oldUser.setMeasurementUnits(newUser.getMeasurementUnits());
        }
        if (newUser.getPhone() != null) {
            oldUser.setPhone(newUser.getPhone());
        }
        if (newUser.getPosition() != null) {
            oldUser.setPosition(newUser.getPosition());
        }
        if (newUser.getRoles() != null) {
            oldUser.setRoles(newUser.getRoles());
        }
        if (newUser.getTemperatureUnits() != null) {
            oldUser.setTemperatureUnits(newUser.getTemperatureUnits());
        }
        if (newUser.getTimeZone() != null) {
            oldUser.setTimeZone(newUser.getTimeZone());
        }
        if (newUser.getTitle() != null) {
            oldUser.setTitle(newUser.getTitle());
        }
        if (newUser.getActive() != null) {
            oldUser.setActive(newUser.getActive());
        }
        if (newUser.getExternal() != null) {
            oldUser.setExternal(newUser.getExternal());
        }
        if (newUser.getCompany() != null) {
            oldUser.setCompany(newUser.getCompany());
        }
        return oldUser;
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
            log.error("Failed to delete user " + userId, e);
            //possible is referenced
            final List<ReferenceInfo> refs = dao.getDbReferences(userId);
            if (!refs.isEmpty()) {
                return createErrorResponse(ErrorCodes.ENTITY_IN_USE, createEntityInUseMessage(refs));
            }
            return createErrorResponse(e);
        }
    }
    /**
     * @param refs list of references.
     * @return
     */
    private String createEntityInUseMessage(final List<ReferenceInfo> refs) {
        final String notifications = NotificationDaoImpl.TABLE;
        final String personalSchedules = NotificationScheduleDaoImpl.PERSONAL_SCHEDULE_TABLE;

        //create references map
        final Map<String, List<Long>> refMap = new HashMap<>();
        refMap.put(notifications, new LinkedList<Long>());
        refMap.put(personalSchedules, new LinkedList<Long>());

        //group references by tables
        for (final ReferenceInfo ref : refs) {
            refMap.get(ref.getType()).add((Long) ref.getId());
        }

        final StringBuilder sb = new StringBuilder("User can't be deleted because is referenced by ");
        if (!refMap.get(notifications).isEmpty()) {
            sb.append("several notifications ");
        }
        if (!refMap.get(personalSchedules).isEmpty()) {
            if (!refMap.get(notifications).isEmpty()) {
                sb.append("and ");
            }

            sb.append("personal schedules (");
            sb.append(StringUtils.combine(refMap.get(personalSchedules), ", "));
            sb.append(')');
        }
        return sb.toString();
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
            if (req.getTitle() != null) {
                u.setTitle(req.getTitle());
            }

            dao.save(u);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to update user details", e);
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
