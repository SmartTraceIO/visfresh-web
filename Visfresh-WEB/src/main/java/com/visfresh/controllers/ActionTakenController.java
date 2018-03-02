/**
 *
 */
package com.visfresh.controllers;

import java.util.Date;

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
import com.visfresh.dao.ActionTakenDao;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.ActionTakenSerializer;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ActionTaken")
@RequestMapping("/rest")
public class ActionTakenController extends AbstractController {
    /**
     * REST service.
     */
    @Autowired
    private ActionTakenDao dao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private RuleBundle ruleBundle;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public ActionTakenController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param alert action taken.
     * @return ID of saved action taken.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/saveActionTaken", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveActionTaken(final @RequestBody JsonObject actionTakenJson) throws RestServiceException {
        final User user = getLoggedInUser();

        final ActionTaken p = createSerializer(user).parseActionTaken(actionTakenJson);

        final Alert alert = alertDao.findOne(p.getAlert());
        if (alert == null) {
            throw new IllegalArgumentException("Alert with given ID "
                    + p.getAlert() + " is not exist");
        }

        checkCompanyAccess(user, alert.getShipment());

        //check created on time
        if (p.getCreatedOn() == null) {
            if (p.getId() == null) {
                p.setCreatedOn(new Date());
            } else {
                throw new IllegalArgumentException("createdOn date could not be NULL");
            }
        }

        final Long id = dao.save(p).getId();
        return createIdResponse("actionTakenId", id);
    }
    /**
     * @param authToken authentication token.
     * @param alert action taken.
     * @return ID of saved action taken.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/verifyActionTaken", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject verifyActionTaken(final @RequestBody JsonObject req) throws RestServiceException {
        final User user = getLoggedInUser();

        final Long id = req.get("id").getAsLong();
        String comments = null;
        final JsonElement json = req.get("comments");
        if (json != null && !json.isJsonNull()) {
            comments = json.getAsString();
        }

        final ActionTaken p = dao.findOne(id);
        final Alert alert = alertDao.findOne(p.getAlert());

        checkCompanyAccess(user, alert.getShipment());

        if (p.getVerifiedBy() == null) {//ignore if already verified
            p.setVerifiedComments(comments);
            p.setVerifiedBy(user.getId());
            p.setVerifiedTime(new Date());

            dao.save(p).getId();
        }

        return createSuccessResponse(null);
    }
    /**
     * @param authToken authentication token.
     * @param id action taken ID.
     * @return action taken.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getActionTaken", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getActionTaken(@RequestParam final Long id) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();

        final ActionTakenView actionTaken = dao.findOne(id, user.getCompanyId());
        return createSuccessResponse(createSerializer(user).toJson(actionTaken));
    }
    /**
     * @param authToken authentication token.
     * @param id action taken ID.
     * @return action taken.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/deleteActionTaken", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteActionTaken(@RequestParam final Long id) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();

        final ActionTakenView p = dao.findOne(id, user.getCompanyId());
        if (p != null) {
            dao.delete(p);
        }

        return createSuccessResponse(null);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of action takens.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getActionTakens", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement getActionTakens(@RequestParam(required = true, value = "shipment") final Long shipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();

        final Shipment shipment = shipmentDao.findOne(shipmentId);

        final ActionTakenSerializer ser = createSerializer(user);

        final JsonArray array = new JsonArray();
        for (final ActionTakenView a : dao.findByShipment(shipment)) {
            array.add(ser.toJson(a));
        }

        return createListSuccessResponse(array, array.size());
    }
    /**
     * @param user
     * @return
     */
    private ActionTakenSerializer createSerializer(final User user) {
        return new ActionTakenSerializer(user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits(),
                ruleBundle);
    }
}
