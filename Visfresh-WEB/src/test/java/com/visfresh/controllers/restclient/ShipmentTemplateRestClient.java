/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.json.ShipmentTemplateSerializer;
import com.visfresh.lists.ListShipmentTemplateItem;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateRestClient extends RestClient {
    private ShipmentTemplateSerializer serializer;

    /**
     *
     */
    public ShipmentTemplateRestClient(final TimeZone tz) {
        super();
        serializer = new ShipmentTemplateSerializer(tz);
    }

    public Long saveShipmentTemplate(final ShipmentTemplate tpl)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveShipmentTemplate"),
                serializer.toJson(tpl)).getAsJsonObject();
        return parseId(e);
    }

    public List<ListShipmentTemplateItem> getShipmentTemplates(final Integer pageIndex, final Integer pageSize, String sortColumn, String sortOrder) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getShipmentTemplates"),
                params).getAsJsonArray();

        final List<ListShipmentTemplateItem> profiles = new ArrayList<ListShipmentTemplateItem>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseListShipmentTemplateItem(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    /**
     * @param id
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public ShipmentTemplate getShipmentTemplate(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentTemplateId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getShipmentTemplate"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipmentTemplate(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteShipmentTemplate(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentTemplateId", id.toString());
        sendGetRequest(getPathWithToken("deleteShipmentTemplate"), params);
    }
    /**
     * @param r reference resolver.
     */
    public void setReferenceResolver(final ReferenceResolver r) {
        serializer.setReferenceResolver(r);
    }
}
