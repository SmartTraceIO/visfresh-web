/**
 *
 */
package com.visfresh.controllers.lite;

import static com.visfresh.constants.BaseShipmentConstants.SHIPPED_FROM;
import static com.visfresh.constants.BaseShipmentConstants.SHIPPED_TO;
import static com.visfresh.constants.ShipmentConstants.SHIPPED_FROM_LOCATION_NAME;
import static com.visfresh.constants.ShipmentConstants.SHIPPED_TO_LOCATION_NAME;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
import com.google.gson.JsonObject;
import com.visfresh.controllers.AbstractController;
import com.visfresh.controllers.ShipmentController;
import com.visfresh.dao.Filter;
import com.visfresh.dao.LiteShipmentDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Language;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.json.GetShipmentsRequestParser;
import com.visfresh.utils.DateTimeUtils;

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
    @RequestMapping(value = "/getShipments", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipments(@RequestBody final JsonObject request) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
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
                    page);
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

    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipmentsNearby", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipmentsNearby(
            @RequestParam(value = "lat") final String latStr,
            @RequestParam(value = "lon") final String lonStr,
            @RequestParam final int radius,
            @RequestParam(required = false, value = "from") final String fromStr) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
            //parse request parameters.
            final double lat = Double.parseDouble(latStr);
            final double lon = Double.parseDouble(lonStr);
            final Date startDate = fromStr == null
                    ? new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000l)
                    : createDateFormat(user.getLanguage(), user.getTimeZone()).parse(fromStr);

            //request shipments from DB
            final List<LiteShipment> shipments = dao.getShipmentsNearby(
                    user.getCompany(), lat, lon, radius, startDate);

            final LiteShipmentSerializer ser = getSerializer(user);
            final JsonArray array = new JsonArray();
            for (final LiteShipment s : shipments) {
                array.add(ser.toJson(s));
            }

            return createListSuccessResponse(array, shipments.size());
        } catch (final Exception e) {
            log.error("Failed to get shipments near (" + latStr + ", " + lonStr + "), radius: " + radius, e);
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
    private DateFormat createDateFormat(final Language lang, final TimeZone tz) {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd'T'HH-mm-ss", lang, tz);
    }
}
