/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.CorrectiveActionsConstants;
import com.visfresh.dao.CorrectiveActionListDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.CorrectiveActionListSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("CorrectiveAction")
@RequestMapping("/rest")
public class CorrectiveActionController extends AbstractController implements CorrectiveActionsConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(CorrectiveActionController.class);
    /**
     * REST service.
     */
    @Autowired
    private CorrectiveActionListDao dao;

    /**
     * Default constructor.
     */
    public CorrectiveActionController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param list critical action list.
     * @return ID of saved critical action list.
     */
    @RequestMapping(value = "/saveCorrectiveActionList", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveCorrectiveActionList(
            final @RequestBody JsonObject list) {
        try {
            final User user = getLoggedInUser();
            final CorrectiveActionList p = createSerializer(user.getCompany()).parseCorrectiveActionList(list);
            final CorrectiveActionList old = dao.findOne(p.getId());

            checkCompanyAccess(user, old);

            final Long id = dao.save(p).getId();
            return createIdResponse("id", id);
        } catch (final Exception e) {
            log.error("Failed to save critical action list", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id critical action list ID.
     * @return critical action list.
     */
    @RequestMapping(value = "/getCorrectiveActionList", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getCorrectiveActionList(
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
            final CorrectiveActionList list = dao.findOne(id);
            checkCompanyAccess(user, list);

            return createSuccessResponse(createSerializer(list.getCompany()).toJson(list));
        } catch (final Exception e) {
            log.error("Failed to get critical action lists", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id critical action list ID.
     * @return critical action list.
     */
    @RequestMapping(value = "/deleteCorrectiveActionList", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteCorrectiveActionList(
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
            final CorrectiveActionList p = dao.findOne(id);
            checkCompanyAccess(user, p);
            dao.delete(p);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get critical action lists", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of critical action lists.
     */
    @RequestMapping(value = "/getCorrectiveActionLists", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement getCorrectiveActionLists(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser();
            final CorrectiveActionListSerializer ser = createSerializer(user.getCompany());

            final List<CorrectiveActionList> lists = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 2),
                    page,
                    null);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final CorrectiveActionList a : lists) {
                array.add(ser.toJson(a));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get critical action lists", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return
     */
    private CorrectiveActionListSerializer createSerializer(final Company company) {
        return new CorrectiveActionListSerializer(company);
    }

    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
                LIST_ID,
                LIST_NAME,
                DESCRIPTION
        };
    }
}
