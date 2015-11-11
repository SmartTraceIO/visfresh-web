/**
 *
 */
package com.visfresh.controllers;

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
import com.visfresh.constants.LocationConstants;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Location")
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
    public @ResponseBody String saveLocation(@PathVariable final String authToken,
            final @RequestBody String profile) {
        try {
            final User user = getLoggedInUser(authToken);
            final LocationProfile lp = getSerializer(user).parseLocationProfile(getJSonObject(profile));

            security.checkCanSaveLocation(user);
            checkCompanyAccess(user, lp);

            lp.setCompany(user.getCompany());
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
    public @ResponseBody String getLocation(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetLocations(user);

            final EntityJSonSerializer ser = getSerializer(user);

            final List<LocationProfile> locations = dao.findByCompany(user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder()),
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
     * @param authToken authentication token.
     * @param locationId location profile ID.
     * @return location profile.
     */
    @RequestMapping(value = "/getLocation/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getLocation(@PathVariable final String authToken,
            @RequestParam final Long locationId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetLocations(user);

            final LocationProfile p = dao.findOne(locationId);
            checkCompanyAccess(user, p);

            return createSuccessResponse(getSerializer(user).toJson(p));
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
    public @ResponseBody String deleteLocation(@PathVariable final String authToken,
            @RequestParam final Long locationId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveLocation(user);

            final LocationProfile p = dao.findOne(locationId);
            checkCompanyAccess(user, p);
            dao.delete(locationId);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_LOCATION_ID,
            PROPERTY_LOCATION_NAME,
            PROPERTY_COMPANY_NAME,
            PROPERTY_ADDRESS,
            PROPERTY_NOTES
        };
    }
}
