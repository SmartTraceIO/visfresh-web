/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.CriticalActionList;
import com.visfresh.io.json.CriticalActionListSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CriticalActionListRestClient extends RestClient {
    private CriticalActionListSerializer serializer;

    /**
     * Default constructor.
     */
    public CriticalActionListRestClient() {
        super();
        serializer = new CriticalActionListSerializer(null);
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
    public List<CriticalActionList> getCriticalActionLists(final Integer pageIndex, final Integer pageSize,
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

        final JsonArray response = sendGetRequest(getPathWithToken("getCriticalActionLists"),
                params).getAsJsonArray();

        final List<CriticalActionList> profiles = new ArrayList<CriticalActionList>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseCriticalActionList(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    public CriticalActionList getCriticalActionList(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getCriticalActionList"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseCriticalActionList(
                response.getAsJsonObject());
    }

    public Long saveCriticalActionList(final CriticalActionList list)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveCriticalActionList"),
                serializer.toJson(list)).getAsJsonObject();
        return parseId(e);
    }
    /**
     * @param p
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteCriticalActionList(final CriticalActionList p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", p.getId().toString());
        sendGetRequest(getPathWithToken("deleteCriticalActionList"), params);
    }

    public List<CriticalActionList> getCriticalActionLists(final Integer pageIndex, final Integer pageSize) throws RestServiceException, IOException {
        return getCriticalActionLists(pageIndex, pageSize, null, null);
    }

}
