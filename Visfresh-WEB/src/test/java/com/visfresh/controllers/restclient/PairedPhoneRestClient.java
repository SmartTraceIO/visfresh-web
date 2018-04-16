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
import com.google.gson.JsonObject;
import com.visfresh.entities.PairedPhone;
import com.visfresh.io.json.PairedPhoneSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PairedPhoneRestClient extends RestClient {
    private PairedPhoneSerializer serializer;

    /**
     * @param tz time zone.
     *
     */
    public PairedPhoneRestClient(final TimeZone tz) {
        this.serializer = new PairedPhoneSerializer(tz);
    }

    /**
     * @param id paired phone ID.
     * @return paired phone.
     * @throws RestServiceException
     * @throws IOException
     */
    public PairedPhone getPairedPhone(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonObject json = sendGetRequest(getPathWithToken("getPairedPhone"),
                params).getAsJsonObject();
        return serializer.parsePairedPhone(json);
    }
    /**
     * @param sortColumn
     * @param sortOrder
     * @param pageIndex
     * @param pageSize
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    public List<PairedPhone> getPairedPhones(final String sortColumn, final boolean sortOrder,
            final Integer pageIndex, final Integer pageSize) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sortColumn != null) {
            params.put("sc", sortColumn);
            params.put("so", sortOrder ? "asc" : "desc");
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getPairedPhones"),
                params).getAsJsonArray();

        final List<PairedPhone> devices = new ArrayList<>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(serializer.parsePairedPhone(response.get(i).getAsJsonObject()));
        }
        return devices;
    }

    /**
     * @param phone
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<String> getPairedBeacons(final String phone) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("phone", phone);

        final JsonArray response = sendGetRequest(getPathWithToken("getPairedBeacons"),
                params).getAsJsonArray();

        final List<String> devices = new ArrayList<>(response.size());
        for (final JsonElement e : response) {
            devices.add(e.getAsString());
        }
        return devices;
    }
    /**
     * @param p paired phone.
     * @return paired phone ID.
     * @throws RestServiceException
     * @throws IOException
     */
    public Long savePairedPhone(final PairedPhone p) throws IOException, RestServiceException {
        final JsonObject json = sendPostRequest(getPathWithToken("savePairedPhone"),
                serializer.toJson(p)).getAsJsonObject();
        return json.get("id").getAsLong();
    }

    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deletePairedPhone(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        sendGetRequest(getPathWithToken("deletePairedPhone"), params);
    }
    /**
     * @param phone
     * @param beacon
     * @throws RestServiceException
     * @throws IOException
     */
    public void deletePairedPhone(final String phone, final String beacon) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("phone", phone);
        params.put("beacon", beacon);

        sendGetRequest(getPathWithToken("deletePairedPhone"), params);
    }
    /**
     * @param phone
     * @param beacon
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public PairedPhone getPairedPhone(final String phone, final String beacon) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("phone", phone);
        params.put("beacon", beacon);

        final JsonObject json = sendGetRequest(getPathWithToken("getPairedPhone"),
                params).getAsJsonObject();
        return serializer.parsePairedPhone(json);
    }
}
