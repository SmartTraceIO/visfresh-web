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
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.io.json.CorrectiveActionListSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionListRestClient extends RestClient {
    private CorrectiveActionListSerializer serializer;

    /**
     * Default constructor.
     */
    public CorrectiveActionListRestClient() {
        super();
        serializer = new CorrectiveActionListSerializer(null);
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
    public List<CorrectiveActionList> getCorrectiveActionLists(final Integer pageIndex, final Integer pageSize,
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

        final JsonArray response = sendGetRequest(getPathWithToken("getCorrectiveActionLists"),
                params).getAsJsonArray();

        final List<CorrectiveActionList> profiles = new ArrayList<CorrectiveActionList>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseCorrectiveActionList(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    public CorrectiveActionList getCorrectiveActionList(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getCorrectiveActionList"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseCorrectiveActionList(
                response.getAsJsonObject());
    }

    public Long saveCorrectiveActionList(final CorrectiveActionList list)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveCorrectiveActionList"),
                serializer.toJson(list)).getAsJsonObject();
        return parseId(e);
    }
    /**
     * @param p
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteCorrectiveActionList(final CorrectiveActionList p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", p.getId().toString());
        sendGetRequest(getPathWithToken("deleteCorrectiveActionList"), params);
    }

    public List<CorrectiveActionList> getCorrectiveActionLists(final Integer pageIndex, final Integer pageSize) throws RestServiceException, IOException {
        return getCorrectiveActionLists(pageIndex, pageSize, null, null);
    }

}
