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
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.json.ActionTakenSerializer;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenRestClient extends RestClient {
    /**
     * Alert profile serializer.
     */
    private ActionTakenSerializer serializer;

    /**
     * @param tz time zone.
     */
    public ActionTakenRestClient(final TimeZone tz, final TemperatureUnits units) {
        super();
        serializer = new ActionTakenSerializer(tz, units, new RuleBundle());
    }
    public List<ActionTaken> getActionTakens(final Long shipmentId) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipment", shipmentId.toString());

        final JsonArray response = sendGetRequest(getPathWithToken("getActionTakens"),
                params).getAsJsonArray();

        final List<ActionTaken> profiles = new ArrayList<>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseActionTaken(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getActionTaken(java.lang.Long)
     */
    public ActionTaken getActionTaken(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getActionTaken"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseActionTaken(
                response.getAsJsonObject());
    }

    public Long saveActionTaken(final ActionTaken at)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveActionTaken"),
                serializer.createJsonWithBaseParams(at)).getAsJsonObject();
        return parseId(e);
    }
    /**
     * @param p
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteActionTaken(final ActionTaken p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", p.getId().toString());
        sendGetRequest(getPathWithToken("deleteActionTaken"), params);
    }
    /**
     * @param id action ID.
     * @param comments verified comments.
     * @throws RestServiceException
     * @throws IOException
     */
    public void verifyActionTaken(final Long id, final String comments)
            throws IOException, RestServiceException {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("comments", comments);
        sendPostRequest(getPathWithToken("verifyActionTaken"), json);
    }
}
