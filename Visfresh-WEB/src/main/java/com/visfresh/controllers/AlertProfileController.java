/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Comparator;
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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("AlertProfile")
@RequestMapping("/rest")
public class AlertProfileController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AlertProfileController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;

    /**
     * Default constructor.
     */
    public AlertProfileController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param alert alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/saveAlertProfile/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveAlertProfile(@PathVariable final String authToken,
            final @RequestBody String alert) {
        try {
            final User user = getLoggedInUser(authToken);
            final AlertProfile p = getSerializer(user).parseAlertProfile(getJSonObject(alert));

            security.checkCanSaveAlertProfile(user);
            final Long id = restService.saveAlertProfile(user.getCompany(), p);
            return createIdResponse("alertProfileId", id);
        } catch (final Exception e) {
            log.error("Failed to save alert profile", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param alertProfileId alert profile ID.
     * @return alert profile.
     */
    @RequestMapping(value = "/getAlertProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getAlertProfile(@PathVariable final String authToken,
            @RequestParam final Long alertProfileId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);

            final AlertProfile alert = restService.getAlertProfile(user.getCompany(), alertProfileId);
            return createSuccessResponse(getSerializer(user).toJson(alert));
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param alertProfileId alert profile ID.
     * @return alert profile.
     */
    @RequestMapping(value = "/deleteAlertProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String deleteAlertProfile(@PathVariable final String authToken,
            @RequestParam final Long alertProfileId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveAlertProfile(user);

            restService.deleteAlertProfile(user.getCompany(), alertProfileId);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of alert profiles.
     */
    @RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getAlertProfiles(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);
            final EntityJSonSerializer ser = getSerializer(user);

            final List<AlertProfile> profiles = restService.getAlertProfiles(user.getCompany());
            sort(profiles, sc, so);

            final List<AlertProfile> alerts = getPage(profiles, pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final AlertProfile a : alerts) {
                array.add(ser.toJson(a));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param profiles
     * @param sc
     * @param so
     */
    private void sort(final List<AlertProfile> profiles, final String sc, final String so) {
        final boolean ascent = !"desc".equals(so);
        Collections.sort(profiles, new Comparator<AlertProfile>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final AlertProfile o1, final AlertProfile o2) {
                if ("alertProfileName".equalsIgnoreCase(sc)) {
                    return compareTo(o1.getName(), o2.getName(), ascent);
                }
                if ("alertProfileDescription".equalsIgnoreCase(sc)) {
                    return compareTo(o1.getDescription(), o2.getDescription(), ascent);
                }
                return compareTo(o1.getId(), o2.getId(), ascent);
            }
        });
    }
}
