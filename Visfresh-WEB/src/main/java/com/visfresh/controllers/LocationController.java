/**
 *
 */
package com.visfresh.controllers;

import java.util.LinkedList;
import java.util.List;

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
import com.visfresh.constants.LocationConstants;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShortShipmentInfo;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.LocationSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Location")
@RequestMapping("/rest")
public class LocationController extends AbstractController implements LocationConstants {
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
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/saveLocation", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveLocation(final @RequestBody JsonObject profile) throws RestServiceException {
        final User user = getLoggedInUser();
        final LocationProfile lp = createSerializer(user).parseLocationProfile(profile);
        lp.setCompany(user.getCompanyId());

        final LocationProfile old = dao.findOne(lp.getId());
        checkCompanyAccess(user, old);

        final long id = dao.save(lp).getId();
        return createIdResponse("locationId", id);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of location profiles.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getLocations", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getLocation(
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
        final LocationSerializer ser = createSerializer(user);

        final List<LocationProfile> locations = dao.findByCompany(user.getCompanyId(),
                createSorting(sc, so, getDefaultSortOrder(), 1),
                page,
                null);

        final int total = dao.getEntityCount(user.getCompanyId(), null);
        final JsonArray array = new JsonArray();
        for (final LocationProfile location : locations) {
            array.add(ser.toJson(location));
        }

        return createListSuccessResponse(array, total);
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
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getLocation", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getLocation(@RequestParam final Long locationId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final LocationProfile p = dao.findOne(locationId);
        checkCompanyAccess(user, p);

        return createSuccessResponse(createSerializer(user).toJson(p));
    }
    /**
     * @param authToken authentication token.
     * @param locationId location profile ID.
     * @return location profile.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/deleteLocation", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteLocation(@RequestParam final Long locationId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final LocationProfile p = dao.findOne(locationId);
        checkCompanyAccess(user, p);

        //check in use
        final List<ShortShipmentInfo> shipments = dao.getOwnerShipments(p);
        if (!shipments.isEmpty()) {
            throw new RestServiceException(ErrorCodes.ENTITY_IN_USE, createLocationInUseMessage(shipments));
        } else {
            dao.delete(locationId);
        }

        return createSuccessResponse(null);
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
