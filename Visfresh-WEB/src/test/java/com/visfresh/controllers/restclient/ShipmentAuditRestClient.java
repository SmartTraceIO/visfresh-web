/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.User;
import com.visfresh.io.json.ShipmentAuditsSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditRestClient extends RestClient {
    private ShipmentAuditsSerializer serializer;

    /**
     *
     */
    public ShipmentAuditRestClient(final User user) {
        super();
        this.serializer = new ShipmentAuditsSerializer(user.getTimeZone());
    }
    /**
     * @param user user
     * @param shipment shipment.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sortColumn sort column.
     * @param sortOrder sort order.
     * @return list of shipment audit items.
     * @throws IOException
     * @throws RestServiceException
     */
    public List<ShipmentAuditItem> getAudits(final User user, final Shipment shipment,
            final Integer pageIndex, final Integer pageSize,
            final String sortColumn, final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();

        if (user != null) {
            params.put("userId", user.getId().toString());
        }
        if (shipment != null) {
            params.put("shipmentId", shipment.getId().toString());
        }
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

        final JsonArray response = sendGetRequest(getPathWithToken("getShipmentAudits"),
                params).getAsJsonArray();

        final List<ShipmentAuditItem> profiles = new ArrayList<>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseShipmentAuditItem(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
}
