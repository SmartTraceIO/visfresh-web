/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.io.json.AlertProfileSerializer;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("AlertProfile")
@RequestMapping("/rest")
public class AlertProfileController extends AbstractController implements AlertProfileConstants {
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
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/saveAlertProfile", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveAlertProfile(
        final @RequestBody JsonObject alert) throws RestServiceException {
        final User user = getLoggedInUser();
        final AlertProfile p = createSerializer(user, user.getCompany()).parseAlertProfile(alert);

        final AlertProfile old = dao.findOne(p.getId());
        checkCompany(old, user.getCompany());

        final Long id = dao.save(p).getId();
        return createIdResponse("alertProfileId", id);
    }
    /**
     * @param authToken authentication token.
     * @param alertProfileId alert profile ID.
     * @return alert profile.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getAlertProfile", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getAlertProfile(@RequestParam final Long alertProfileId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final AlertProfile alert = dao.findOne(alertProfileId);
        checkCompanyAccess(user, alert);
        return createSuccessResponse(createSerializer(user, alert.getCompany()).toJson(alert));
    }
    /**
     * @param authToken authentication token.
     * @param alertProfileId alert profile ID.
     * @return alert profile.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/deleteAlertProfile", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteAlertProfile(@RequestParam final Long alertProfileId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final AlertProfile p = dao.findOne(alertProfileId);
        checkCompanyAccess(user, p);
        dao.delete(p);

        return createSuccessResponse(null);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of alert profiles.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getAlertProfiles", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement getAlertProfiles(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
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
