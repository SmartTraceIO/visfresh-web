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
import com.visfresh.entities.LocationProfile;
import com.visfresh.io.json.LocationSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationRestClient extends RestClient {
    /**
     * Serializer.
     */
    private LocationSerializer serializer;
    /**
     * @param tz time zone.
     */
    public LocationRestClient(final TimeZone tz) {
        super();
        this.serializer = new LocationSerializer(tz);
    }

    public Long saveLocationProfile(final LocationProfile profile)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveLocation"),
                serializer.toJson(profile)).getAsJsonObject();
        return parseId(e);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getLocationProfile(java.lang.Long)
     */
    public LocationProfile getLocation(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("locationId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getLocation"), params);
        return response == null || response.isJsonNull() ? null : serializer.parseLocationProfile(
                response.getAsJsonObject());
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
    public List<LocationProfile> getLocations(final Integer pageIndex, final Integer pageSize,
            final String sortColumn, final String sortOrder) throws IOException, RestServiceException {
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

        final JsonArray response = sendGetRequest(getPathWithToken("getLocations"),
                params).getAsJsonArray();

        final List<LocationProfile> profiles = new ArrayList<LocationProfile>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseLocationProfile(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of location profiles.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<LocationProfile> getLocations(final Integer pageIndex, final Integer pageSize)
            throws RestServiceException, IOException {
        return getLocations(pageIndex, pageSize, null, null);
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteLocation(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("locationId", id.toString());

        sendGetRequest(getPathWithToken("deleteLocation"), params);
    }
}
