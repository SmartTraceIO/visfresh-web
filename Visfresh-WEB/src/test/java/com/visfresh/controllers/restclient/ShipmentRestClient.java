/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.UserResolver;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentRestClient extends RestClient {
    private ShipmentSerializer serializer;

    /**
     *
     */
    public ShipmentRestClient(final User user) {
        super();
        this.serializer = new ShipmentSerializer(user);
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
     * @param shipment
     * @param templateName
     * @param saveTemplate
     */
    public SaveShipmentResponse saveShipment(final ShipmentDto shipment, final String templateName,
            final boolean saveTemplate) throws RestServiceException, IOException {
        final SaveShipmentRequest req = new SaveShipmentRequest();
        req.setShipment(shipment);
        req.setTemplateName(templateName);
        req.setSaveAsNewTemplate(saveTemplate);

        return saveShipment(req);
    }
    /**
     * @param req save shipment request.
     * @return save shipment response.
     * @throws IOException
     * @throws RestServiceException
     */
    public SaveShipmentResponse saveShipment(final SaveShipmentRequest req)
            throws IOException, RestServiceException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveShipment"),
                serializer.toJson(req)).getAsJsonObject();
        final SaveShipmentResponse resp = serializer.parseSaveShipmentResponse(e);
        return resp;
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    public ShipmentDto getShipment(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getShipment"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipment(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteShipment(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", id.toString());
        sendGetRequest(getPathWithToken("deleteShipment"), params);
    }
    /**
     * @param id shipment ID.
     * @throws RestServiceException
     * @throws IOException
     */
    public void suppressAlerts(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", id.toString());
        sendGetRequest(getPathWithToken("suppressAlerts"), params);
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public JsonArray getShipments(final Integer pageIndex, final Integer pageSize)
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
    public JsonArray getShipmentsSorted(final String column, final boolean ascent)
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
    public JsonArray getShipments(final GetFilteredShipmentsRequest req)
            throws IOException, RestServiceException {
        return sendPostRequest(getPathWithToken("getShipments"),
                serializer.toJson(req)).getAsJsonArray();
    }
    /**
     * @param device
     * @throws RestServiceException
     * @throws IOException
     * @return ID of new autostarted shipment.
     */
    public Long autoStartShipment(final String device) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("device", device);

        final JsonElement el = sendGetRequest(getPathWithToken("createNewAutoSthipment"), params);
        return parseId(el.getAsJsonObject());
    }
    public void setUserResolver(final UserResolver r) {
        serializer.setUserResolver(r);
    }
}
