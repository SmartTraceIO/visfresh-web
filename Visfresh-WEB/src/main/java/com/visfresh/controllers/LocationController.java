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
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.LocationConstants;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Role;
import com.visfresh.entities.ShortShipmentInfo;
import com.visfresh.entities.User;
import com.visfresh.io.json.LocationSerializer;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Location")
@RequestMapping("/rest")
public class LocationController extends AbstractController implements LocationConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LocationController.class);
    @Autowired
    private LocationProfileDao dao;

    /**
     * Default constructor.
     */
    public LocationController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param profile location profile.
     * @return ID of saved location profile.
     */
    @RequestMapping(value = "/saveLocation/{authToken}", method = RequestMethod.POST)
    public JsonObject saveLocation(@PathVariable final String authToken,
            final @RequestBody JsonObject profile) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final LocationProfile lp = createSerializer(user).parseLocationProfile(profile);
            lp.setCompany(user.getCompany());

            final LocationProfile old = dao.findOne(lp.getId());
            checkCompanyAccess(user, old);

            final long id = dao.save(lp).getId();
            return createIdResponse("locationId", id);
        } catch (final Exception e) {
            log.error("Failed to save location profile.", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of location profiles.
     */
    @RequestMapping(value = "/getLocations/{authToken}", method = RequestMethod.GET)
    public JsonObject getLocation(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final LocationSerializer ser = createSerializer(user);

            final List<LocationProfile> locations = dao.findByCompany(user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 1),
                    page,
                    null);

            final int total = dao.getEntityCount(user.getCompany(), null);
            final JsonArray array = new JsonArray();
            for (final LocationProfile location : locations) {
                array.add(ser.toJson(location));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private LocationSerializer createSerializer(final User user) {
        return new LocationSerializer(user.getTimeZone());
    }

    /**
     * @param authToken authentication token.
     * @param locationId location profile ID.
     * @return location profile.
     */
    @RequestMapping(value = "/getLocation/{authToken}", method = RequestMethod.GET)
    public JsonObject getLocation(@PathVariable final String authToken,
            @RequestParam final Long locationId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final LocationProfile p = dao.findOne(locationId);
            checkCompanyAccess(user, p);

            return createSuccessResponse(createSerializer(user).toJson(p));
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param locationId location profile ID.
     * @return location profile.
     */
    @RequestMapping(value = "/deleteLocation/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteLocation(@PathVariable final String authToken,
            @RequestParam final Long locationId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final LocationProfile p = dao.findOne(locationId);
            checkCompanyAccess(user, p);

            //check in use
            final List<ShortShipmentInfo> shipments = dao.getOwnerShipments(p);
            if (!shipments.isEmpty()) {
                return createErrorResponse(ErrorCodes.ENTITY_IN_USE, createLocationInUseMessage(shipments));
            } else {
                dao.delete(locationId);
            }

            return createSuccessResponse(null);
        } catch (final Throwable e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param list
     * @return
     */
    private String createLocationInUseMessage(final List<ShortShipmentInfo> list) {
        final List<Long> shipments = new LinkedList<>();
        final List<Long> templates = new LinkedList<>();
        for (final ShortShipmentInfo i : list) {
            if (i.isTemplate()) {
                templates.add(i.getId());
            } else {
                shipments.add(i.getId());
            }
        }

        final StringBuilder sb = new StringBuilder("Location can't be deleted because is referenced by ");
        if (!shipments.isEmpty()) {
            sb.append("shipments (");
            sb.append(StringUtils.combine(shipments, ", "));
            sb.append(')');
        }
        if (!templates.isEmpty()) {
            if (!shipments.isEmpty()) {
                sb.append(" and");
            }

            sb.append("templates (");
            sb.append(StringUtils.combine(templates, ", "));
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_LOCATION_NAME,
            PROPERTY_COMPANY_NAME,
            PROPERTY_ADDRESS,
            PROPERTY_RADIUS_METERS,
            PROPERTY_START_FLAG,
            PROPERTY_INTERIM_FLAG,
            PROPERTY_END_FLAG
        };
    }
}
