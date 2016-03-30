/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.io.NoteDto;
import com.visfresh.io.json.NoteSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteRestClient extends RestClient {
    private final NoteSerializer serializer;

    /**
     * @param tz time zone.
     */
    public NoteRestClient(final TimeZone tz) {
        super();
        serializer = new NoteSerializer(tz);
    }

    /**
     * @param note note.
     * @return note number.
     * @throws IOException
     * @throws RestServiceException
     */
    public int saveNote(final NoteDto note) throws IOException, RestServiceException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveNote"),
                serializer.toJson(note)).getAsJsonObject();
        return e.get("noteNum").getAsInt();
    }
    public List<NoteDto> getNotes(final Long shipmentId) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", shipmentId.toString());
        return getNotes(params);
    }
    public List<NoteDto> getNotes(final String sn, final int trip) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("sn", sn);
        params.put("trip", Integer.toString(trip));
        return getNotes(params);
    }
    /**
     * @param params
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private List<NoteDto> getNotes(final Map<String, String> params)
            throws IOException, RestServiceException {
        final JsonArray array = sendGetRequest(getPathWithToken("getNotes"), params).getAsJsonArray();

        final List<NoteDto> result = new LinkedList<NoteDto>();
        for (final JsonElement el : array) {
            result.add(serializer.parseNoteDto(el));
        }

        return result;
    }
}
