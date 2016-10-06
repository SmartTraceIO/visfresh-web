/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.InterimStopDto;
import com.visfresh.io.json.InterimStopSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopRestClient extends RestClient {
    private InterimStopSerializer serializer;

    /**
     *
     */
    public InterimStopRestClient(final User user) {
        super();
        this.serializer = new InterimStopSerializer(user.getLanguage(), user.getTimeZone());
    }

    /**
     * @param req save shipment request.
     * @return save shipment response.
     * @throws IOException
     * @throws RestServiceException
     */
    public Long addInterimStop(final InterimStopDto req)
            throws IOException, RestServiceException {
        final JsonObject e = sendPostRequest(getPathWithToken("addInterimStop"),
                serializer.toJson(req)).getAsJsonObject();
        return parseId(e);
    }
    /**
     * @param req save shipment request.
     * @return save shipment response.
     * @throws IOException
     * @throws RestServiceException
     */
    public Long saveInterimStop(final InterimStopDto req)
            throws IOException, RestServiceException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveInterimStop"),
                serializer.toJson(req)).getAsJsonObject();
        return parseId(e);
    }

    /**
     * @param s
     * @param stop
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteInterimStop(final Shipment s, final InterimStop stop) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("shipment", s.getId().toString());
        params.put("id", stop.getId().toString());
        sendGetRequest(getPathWithToken("deleteInterimStop"), params);
    }

    /**
     * @param shipment
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<InterimStopDto> getInterimStops(final Shipment shipment) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("shipment", shipment.getId().toString());

        final List<InterimStopDto> stops = new LinkedList<>();
        final JsonElement res = sendGetRequest(getPathWithToken("getInterimStops"), params);
        for (final JsonElement e : res.getAsJsonArray()) {
            stops.add(serializer.parseInterimStopDto(e));
        }
        return stops;
    }
}
