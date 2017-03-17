/**
 *
 */
package com.visfresh.controllers.lite;

import static com.visfresh.constants.BaseShipmentConstants.SHIPPED_FROM;
import static com.visfresh.constants.BaseShipmentConstants.SHIPPED_TO;
import static com.visfresh.constants.ShipmentConstants.SHIPPED_FROM_LOCATION_NAME;
import static com.visfresh.constants.ShipmentConstants.SHIPPED_TO_LOCATION_NAME;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.controllers.AbstractController;
import com.visfresh.controllers.ShipmentController;
import com.visfresh.controllers.lite.dao.LiteShipmentDao;
import com.visfresh.controllers.lite.dao.LiteShipmentResult;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.json.GetShipmentsRequestParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("LiteShipment")
@RequestMapping("/lite")
public class LiteShipmentController extends AbstractController {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LiteShipmentController.class);

    @Autowired
    private LiteShipmentDao dao;

    /**
     * Default constructor.
     */
    public LiteShipmentController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.POST)
    public JsonObject getShipments(@PathVariable final String authToken,
            @RequestBody final JsonObject request) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final LiteShipmentSerializer ser = getSerializer(user);
            final GetFilteredShipmentsRequest req = new GetShipmentsRequestParser(
                    user.getTimeZone()).parseGetFilteredShipmentsRequest(request);

            final Integer pageIndex = req.getPageIndex();
            final Integer pageSize = req.getPageSize();
            final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

            final Filter filter = ShipmentController.createFilter(req);
            final LiteShipmentResult result = dao.getShipments(
                    user.getCompany(),
                    createSortingShipments(
                            req.getSortColumn(),
                            req.getSortOrder(),
                            ShipmentController.getDefaultListShipmentsSortingOrder(), 2),
                    filter,
                    page, user);
            final List<LiteShipment> shipments = result.getResult();
            final int total = result.getTotalCount();

            final JsonArray array = new JsonArray();
            for (final LiteShipment s : shipments) {
                array.add(ser.toJson(s));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get shipments", e);
            return createErrorResponse(e);
        }
    }

    private Sorting createSortingShipments(final String sc, final String so,
            final String[] defaultSortOrder, final int maxNumOfSortColumns) {
        String sortColumn;
        if (SHIPPED_FROM.equals(sc)) {
            sortColumn = SHIPPED_FROM_LOCATION_NAME;
        } else if (SHIPPED_TO.equals(sc)) {
            sortColumn = SHIPPED_TO_LOCATION_NAME;
        } else {
            sortColumn = sc;
        }
        return super.createSorting(sortColumn, so, defaultSortOrder, maxNumOfSortColumns);
    }
    /**
     * @param user the user.
     * @return shipment serializer for given user.
     */
    private LiteShipmentSerializer getSerializer(final User user) {
        return new LiteShipmentSerializer(user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
    }
}
