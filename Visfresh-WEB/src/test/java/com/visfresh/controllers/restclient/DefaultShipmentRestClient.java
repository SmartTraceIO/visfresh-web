/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.io.DefaultShipmentDto;
import com.visfresh.io.json.DefaultShipmentSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentRestClient extends RestClient {
    private DefaultShipmentSerializer serializer;

    /**
     * @param tz time zone.
     */
    public DefaultShipmentRestClient(final TimeZone tz) {
        super();
        this.serializer = new DefaultShipmentSerializer(tz);
    }

    /**
     * @param id default shipment ID.
     * @return default shipment by given ID.
     * @throws IOException
     * @throws RestServiceException
     */
    public DefaultShipmentDto getDefaultShipment(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("defaultShipmentId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getDefaultShipment"),
                params);
        return serializer.parseDefaultShipmentDto(response);
    }
    /**
     * @param id default shipment ID.
     * @throws IOException
     * @throws RestServiceException
     */
    public void deleteDefaultShipment(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("defaultShipmentId", id.toString());

        sendGetRequest(getPathWithToken("deleteDefaultShipment"), params);
    }
    /**
     * @param dto default shipment.
     * @return ID of new saved default shipment.
     * @throws IOException
     * @throws RestServiceException
     */
    public Long saveDefaultShipment(final DefaultShipmentDto dto)
            throws IOException, RestServiceException {
        final JsonElement response = sendPostRequest(getPathWithToken("saveDefaultShipment"),
                serializer.toJson(dto));
        return parseId(response.getAsJsonObject());
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of default shipments.
     * @throws IOException
     * @throws RestServiceException
     */
    public List<DefaultShipmentDto> getDefaultShipments(
            final Integer pageIndex, final Integer pageSize)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        final JsonArray response = sendGetRequest(
                getPathWithToken("getDefaultShipments"), params).getAsJsonArray();

        final List<DefaultShipmentDto> result = new LinkedList<DefaultShipmentDto>();
        for (final JsonElement e : response) {
            result.add(serializer.parseDefaultShipmentDto(e));
        }
        return result;
    }
}
