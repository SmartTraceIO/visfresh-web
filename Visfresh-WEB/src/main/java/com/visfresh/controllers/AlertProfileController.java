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
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("AlertProfile")
@RequestMapping("/rest")
public class AlertProfileController extends AbstractController implements AlertProfileConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AlertProfileController.class);
    /**
     * REST service.
     */
    @Autowired
    private AlertProfileDao dao;

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
            checkCompanyAccess(user, p);

            p.setCompany(user.getCompany());
            final Long id = dao.save(p).getId();
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

            final AlertProfile alert = dao.findOne(alertProfileId);
            checkCompanyAccess(user, alert);

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

            final AlertProfile p = dao.findOne(alertProfileId);
            checkCompanyAccess(user, p);
            dao.delete(p);

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
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);
            final EntityJSonSerializer ser = getSerializer(user);

            final List<AlertProfile> alerts = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder()),
                    page,
                    null);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final AlertProfile a : alerts) {
                array.add(ser.toJson(a));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_ALERT_PROFILE_ID,
            PROPERTY_ALERT_PROFILE_NAME,
            PROPERTY_ALERT_PROFILE_DESCRIPTION
        };
    }
}
