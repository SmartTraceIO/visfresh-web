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
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Location")
@RequestMapping("/rest")
public class LocationController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LocationController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;

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
            final LocationProfile lp = getSerializer().parseLocationProfile(getJSonObject(profile));

            security.checkCanSaveLocation(user);

            final Long id = restService.saveLocation(user.getCompany(), lp);
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
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetLocations(user);

            final EntityJSonSerializer ser = getSerializer();

            final List<LocationProfile> locations = getPage(restService.getLocation(user.getCompany()),
                    pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final LocationProfile location : locations) {
                array.add(ser.toJson(location));
            }

            return createSuccessResponse(array);
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

            final LocationProfile location = restService.getLocationProfile(user.getCompany(), locationId);
            return createSuccessResponse(getSerializer().toJson(location));
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

            restService.deleteLocation(user.getCompany(), locationId);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
}
