/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
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
    public JsonElement getSingleShipment(final Shipment shipment, final Date from, final Date to)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("fromDate", serializer.formatDate(from));
        params.put("toDate", serializer.formatDate(to));
        params.put("shipment", shipment.getId().toString());

        return sendGetRequest(getPathWithToken("getSingleShipment"), params);
    }

    /**
     * @param shipment
     * @param templateName
     * @param saveTemplate
     */
    public SaveShipmentResponse saveShipment(final Shipment shipment, final String templateName,
            final boolean saveTemplate) throws RestServiceException, IOException {
        final SaveShipmentRequest req = new SaveShipmentRequest();
        req.setShipment(shipment);
        req.setTemplateName(templateName);
        req.setSaveAsNewTemplate(saveTemplate);

        final JsonObject e = sendPostRequest(getPathWithToken("saveShipment"),
                serializer.toJson(req)).getAsJsonObject();
        final SaveShipmentResponse resp = serializer.parseSaveShipmentResponse(e);
        return resp;
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    public Shipment getShipment(final Long id) throws IOException, RestServiceException {
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
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public JsonArray getShipments(final Integer pageIndex, final Integer pageSize)
            throws RestServiceException, IOException {
        return getShipments(pageIndex, pageSize, null, null, null, null, null);
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
    public JsonArray getShipments(final Integer pageIndex, final Integer pageSize, final Long shippedFrom,
            final Long shippedTo, final String goods, final String device, final ShipmentStatus status)
                    throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }

        if (shippedFrom != null) {
            params.put("shippedFrom", shippedFrom.toString());
        }
        if (shippedTo != null) {
            params.put("shippedTo", shippedTo.toString());
        }
        if (goods != null) {
            params.put("goods", goods.toString());
        }
        if (device != null) {
            params.put("device", device.toString());
        }
        if (status != null) {
            params.put("status", status.toString());
        }

        return sendGetRequest(getPathWithToken("getShipments"),
                params).getAsJsonArray();
    }

    /**
     * @param r reference resolver.
     */
    public void setReferenceResolver(final ReferenceResolver r) {
        serializer.setReferenceResolver(r);
    }
}
