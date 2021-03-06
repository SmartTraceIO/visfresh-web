/**
 *
 */
package com.visfresh.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.UserConstants;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.dao.impl.NotificationDaoImpl;
import com.visfresh.dao.impl.NotificationScheduleDaoImpl;
import com.visfresh.entities.Company;
import com.visfresh.entities.ReferenceInfo;
import com.visfresh.entities.Role;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.SaveUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.lists.ExpandedListUserItem;
import com.visfresh.lists.ShortListUserItem;
import com.visfresh.services.RestServiceException;
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
    private CompanyDao companyDao;

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
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getUser(final @RequestParam(required = false) Long userId) throws RestServiceException {
        final User user = getLoggedInUser();
        if (userId != null && !user.getId().equals(userId) && !Role.BasicUser.hasRole(user)) {
                throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                        user.getEmail() + " is only permitted for role " + Role.BasicUser);
        }

        final User u = dao.findOne(userId == null ? user.getId() : userId);
        checkCompanyAccess(user, u);

        final Company company = companyDao.findOne(u.getCompanyId());
        return createSuccessResponse(u == null ? null : getUserSerializer(user).toJson(u, company));
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sc sort column
     * @param so sort order.
     * @return
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getUsers(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        final User user = getLoggedInUser();
        final int total = dao.getEntityCount(user.getCompanyId(), null);
        final UserSerializer ser = getUserSerializer(user);
        final JsonArray array = new JsonArray();

        final List<User> users = dao.findByCompany(
                user.getCompanyId(),
                createSorting(sc, so, getDefaultListShipmentsSortingOrder(), 2),
                page,
                null);

        final Company company = companyDao.findOne(user.getCompanyId());
        for (final User u : users) {
            array.add(ser.toJson(new ExpandedListUserItem(u, company)));
        }
        return createListSuccessResponse(array, total);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sc sort column
     * @param so sort order.
     * @return
     * @throws RestServiceException
     */
    @RequestMapping(value = "/listUsers", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject listUsers(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        final User user = getLoggedInUser();
        final UserSerializer ser = getUserSerializer(user);

        final List<ShortListUserItem> users = getUserListItems(
                companyDao.findOne(user.getCompanyId()),
                createSorting(sc, so, getDefaultListShipmentsSortingOrder(), 2),
                null,
                page);
        final int total = dao.getEntityCount(user.getCompanyId(), null);

        final JsonArray array = new JsonArray();
        for (final ShortListUserItem s : users) {
            array.add(ser.toJson(s));
        }
        return createListSuccessResponse(array, total);
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
        for (final User u : dao.findByCompany(company.getCompanyId(), sorting, page, filter)) {
            result.add(new ShortListUserItem(u, company));
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
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin})
    public JsonObject saveUser(final @RequestBody JsonObject req) throws RestServiceException {
        final User user = getLoggedInUser();
        final SaveUserRequest r = getUserSerializer(user).parseSaveUserRequest(req);

        User newUser = r.getUser();
        if (newUser.getId() != null) {
            newUser = dao.findOne(newUser.getId());
            checkCompanyAccess(user, newUser);

            newUser = mergeUsers(newUser, r.getUser());
        } else if (newUser.getRoles() == null) {
            newUser.setRoles(new HashSet<Role>());
        }

        if (newUser.getCompanyId() == null) {
            newUser.setCompany(user.getCompanyId());
        } else {
            checkCompanyAccess(user, newUser.getCompanyId());
        }

        checkCanAssignRoles(user, newUser.getRoles());
        authService.saveUser(newUser, r.getPassword(), Boolean.TRUE.equals(r.getResetOnLogin()));

        return createIdResponse("userId", newUser.getId());
    }
    /**
     * @param user the user
     * @param roles
     * @throws RestServiceException
     */
    private void checkCanAssignRoles(final User user, final Set<Role> roles) throws RestServiceException {
        for (final Role role : roles) {
            if (!role.hasRole(user)) {
                throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                        user.getEmail() + " can't assign role " + role);
            }
        }
    }

    /**
     * @param oldUser old user.
     * @param newUser new user.
     * @return
     */
    private User mergeUsers(final User oldUser, final User newUser) {
        if (newUser.getCompanyId() != null) {
            oldUser.setCompany(newUser.getCompanyId());
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
        if (newUser.getCompanyId() != null) {
            oldUser.setCompany(newUser.getCompanyId());
        }
        return oldUser;
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin})
    public JsonObject deleteUser(final @RequestParam Long userId) throws Exception {
        try {
            final User user = getLoggedInUser();
            final User deletedUser = dao.findOne(userId);
            checkCompanyAccess(user, deletedUser);

            dao.delete(userId);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete user " + userId, e);
            //possible is referenced
            final List<ReferenceInfo> refs = dao.getDbReferences(userId);
            if (!refs.isEmpty()) {
                throw new RestServiceException(ErrorCodes.ENTITY_IN_USE, createEntityInUseMessage(refs));
            }
            throw e;
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

    @RequestMapping(value = "/updateUserDetails", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject updateUserDetails(@RequestBody final JsonObject body) throws RestServiceException {
        final User user = getLoggedInUser();
        final UpdateUserDetailsRequest req = getUserSerializer(user).parseUpdateUserDetailsRequest(
                body);

        final User u = dao.findOne(req.getUser());
        if (!Role.Admin.hasRole(user) && !u.getId().equals(user.getId())) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    user.getEmail() + " is not permitted to change user details for "
                    + u.getEmail());
        }

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

        if (!u.isActive()) {
            authService.forceLogout(u);
        }
        return createSuccessResponse(null);
    }
    /**
     * @param user user.
     * @return serializer.
     */
    protected UserSerializer getUserSerializer(final User user) {
        final UserSerializer ser = new UserSerializer(
                user == null ? SerializerUtils.UT?? : user.getTimeZone());
        ser.setShipmentResolver(shipmentResolver);
        return ser;
    }
}
