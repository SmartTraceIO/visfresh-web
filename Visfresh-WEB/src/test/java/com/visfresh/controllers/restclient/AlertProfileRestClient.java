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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.json.AlertProfileSerializer;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileRestClient extends RestClient {
    /**
     * Alert profile serializer.
     */
    private AlertProfileSerializer serializer;

    /**
     * @param tz time zone.
     */
    public AlertProfileRestClient(final TimeZone tz, final TemperatureUnits units) {
        super();
        serializer = new AlertProfileSerializer(tz, units);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<ListAlertProfileItem> getAlertProfiles(final Integer pageIndex, final Integer pageSize,
            final String sortColumn,
            final String sortOrder) throws IOException, RestServiceException {
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

        final JsonArray response = sendGetRequest(getPathWithToken("getAlertProfiles"),
                params).getAsJsonArray();

        final List<ListAlertProfileItem> profiles = new ArrayList<ListAlertProfileItem>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseListAlertProfileItem(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    public AlertProfile getAlertProfile(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("alertProfileId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getAlertProfile"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseAlertProfile(
                response.getAsJsonObject());
    }

    public Long saveAlertProfile(final AlertProfile alert)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveAlertProfile"),
                serializer.toJson(alert)).getAsJsonObject();
        return parseId(e);
    }
    /**
     * @param p
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteAlertProfile(final AlertProfile p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("alertProfileId", p.getId().toString());
        sendGetRequest(getPathWithToken("deleteAlertProfile"), params);
    }

    public List<ListAlertProfileItem> getAlertProfiles(final Integer pageIndex, final Integer pageSize) throws RestServiceException, IOException {
        return getAlertProfiles(pageIndex, pageSize, null, null);
    }
}
