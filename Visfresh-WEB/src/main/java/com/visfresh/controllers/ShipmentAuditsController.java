/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.ShipmentAuditConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentAuditDao;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.ShipmentAuditsSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ShipmentAudits")
@RequestMapping("/rest")
public class ShipmentAuditsController extends AbstractController implements ShipmentAuditConstants {
    /**
     * REST service.
     */
    @Autowired
    private ShipmentAuditDao dao;

    /**
     * Default constructor.
     */
    public ShipmentAuditsController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of alert profiles.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getShipmentAudits", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin})
    public JsonElement getShipmentAudits(
            @RequestParam(value = "shipmentId", required = false) final Long shipmentId,
            @RequestParam(value = "userId", required = false) final Integer userId,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
        //check correct parameters
        if (userId == null && shipmentId == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "One shipmentId or userId should be specified");
        }

        final Filter filter = new Filter();
        if (userId != null) {
            filter.addFilter(USER_ID, userId);
        }
        if (shipmentId != null) {
            filter.addFilter(SHIPMENT_ID, shipmentId);
        }

        final List<ShipmentAuditItem> items = dao.findAll(
                user.getCompany(),
                filter,
                createSorting(sc, so, getDefaultSortOrder(), 2),
                page);
        final int total = dao.getEntityCount(user.getCompany(), filter);

        final ShipmentAuditsSerializer ser = createSerializer(user);

        final JsonArray array = new JsonArray();
        for (final ShipmentAuditItem item : items) {
            array.add(ser.toJson(item));
        }

        return createListSuccessResponse(array, total);
    }
    /**
     * @param user
     * @return
     */
    private ShipmentAuditsSerializer createSerializer(final User user) {
        return new ShipmentAuditsSerializer(user.getTimeZone());
    }

    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            ShipmentAuditConstants.TIME,
            ShipmentAuditConstants.SHIPMENT_ID,
            ShipmentAuditConstants.USER_ID,
            ShipmentAuditConstants.ID,
            ShipmentAuditConstants.ACTION
        };
    }
}
