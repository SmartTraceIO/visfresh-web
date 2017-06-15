/**
 *
 */
package com.visfresh.controllers;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.io.json.AlertProfileSerializer;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.lists.ListAlertProfileItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("AlertProfile")
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
    @Autowired
    private RuleBundle ruleBundle;

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
    public JsonObject saveAlertProfile(@PathVariable final String authToken,
            final @RequestBody JsonObject alert) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final AlertProfile p = createSerializer(user, user.getCompany()).parseAlertProfile(alert);

            final AlertProfile old = dao.findOne(p.getId());
            checkCompany(old, user.getCompany());

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
    public JsonObject getAlertProfile(@PathVariable final String authToken,
            @RequestParam final Long alertProfileId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final AlertProfile alert = dao.findOne(alertProfileId);
            checkCompanyAccess(user, alert);

            return createSuccessResponse(createSerializer(user, alert.getCompany()).toJson(alert));
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
    public JsonObject deleteAlertProfile(@PathVariable final String authToken,
            @RequestParam final Long alertProfileId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

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
    public JsonElement getAlertProfiles(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final AlertProfileSerializer ser = createSerializer(user, user.getCompany());

            final List<AlertProfile> alerts = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 2),
                    page,
                    null);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final AlertProfile a : alerts) {
                //convert profile to profile list item.
                final ListAlertProfileItem item = new ListAlertProfileItem();
                item.setAlertProfileId(a.getId());
                item.setAlertProfileName(a.getName());
                item.setAlertProfileDescription(a.getDescription());
                for (final TemperatureRule rule : a.getAlertRules()) {
                    item.getAlertRuleList().add(
                            ruleBundle.buildDescription(rule, user.getTemperatureUnits()));
                }

                array.add(ser.toJson(item));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private AlertProfileSerializer createSerializer(final User user, final Company company) {
        return new AlertProfileSerializer(company, user.getTimeZone(), user.getTemperatureUnits());
    }

    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            ALERT_PROFILE_ID,
            ALERT_PROFILE_NAME,
            ALERT_PROFILE_DESCRIPTION
        };
    }
}
