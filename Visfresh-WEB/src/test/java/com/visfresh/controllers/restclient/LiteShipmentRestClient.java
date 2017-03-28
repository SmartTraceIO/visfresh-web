/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.controllers.lite.LiteShipmentSerializer;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.json.GetShipmentsRequestParser;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentRestClient extends RestClient {
    private static final String REST_SERVICE = "/lite";
    private LiteShipmentSerializer serializer;
    private GetShipmentsRequestParser reqParser;

    /**
     *
     */
    public LiteShipmentRestClient(final User user) {
        super();
        this.serializer = new LiteShipmentSerializer(
                user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
        this.reqParser = new GetShipmentsRequestParser(user.getTimeZone());
    }
    public JsonElement getSingleShipment(final Shipment shipment)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", shipment.getId().toString());

        return sendGetRequest(getPathWithToken("getSingleShipment"), params);
    }
    public JsonElement getSingleShipment(final String sn, final int trip)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (sn != null) {
            params.put("sn", sn);
            params.put("trip", Integer.toString(trip));
        }

        return sendGetRequest(getPathWithToken("getSingleShipment"), params);
    }
    /**
     * @param s
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public JsonElement getSingleShipmentLite(final Shipment s) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", s.getId().toString());

        return sendGetRequest(getPathWithToken("getSingleShipmentLite"), params);
    }

    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public List<LiteShipment> getShipments(final Integer pageIndex, final Integer pageSize)
            throws RestServiceException, IOException {
        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        req.setPageIndex(pageIndex);
        req.setPageSize(pageSize);
        return getShipments(req);
    }
    /**
     * @param column sorting column.
     * @param ascent sort order
     * @return array of shipments.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<LiteShipment> getShipmentsSorted(final String column, final boolean ascent)
            throws RestServiceException, IOException {
        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        req.setSortColumn(column);
        req.setSortOrder(ascent ? "asc": "desc");
        return getShipments(req);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param shippedFrom
     * @param shippedTo
     * @param goods
     * @param device
     * @param status
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<LiteShipment> getShipments(final GetFilteredShipmentsRequest req)
            throws IOException, RestServiceException {
        final JsonArray array = sendPostRequest(getPathWithToken("getShipments"),
                reqParser.toJson(req)).getAsJsonArray();

        final List<LiteShipment> shipments = new LinkedList<>();
        for (final JsonElement e : array) {
            shipments.add(this.serializer.parseLiteShipment(e));
        }

        return shipments;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.restclient.RestClient#getServiceUri()
     */
    @Override
    protected String getServiceUri() {
        return REST_SERVICE;
    }
}
