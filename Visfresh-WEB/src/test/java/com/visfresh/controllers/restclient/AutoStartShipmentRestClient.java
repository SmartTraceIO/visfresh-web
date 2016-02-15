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
import com.visfresh.io.AutoStartShipmentDto;
import com.visfresh.io.json.AutoStartShipmentSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentRestClient extends RestClient {
    private AutoStartShipmentSerializer serializer;

    /**
     * @param tz time zone.
     */
    public AutoStartShipmentRestClient(final TimeZone tz) {
        super();
        this.serializer = new AutoStartShipmentSerializer(tz);
    }

    /**
     * @param id default shipment ID.
     * @return default shipment by given ID.
     * @throws IOException
     * @throws RestServiceException
     */
    public AutoStartShipmentDto getAutoStartShipment(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("autoStartShipmentId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getAutoStartShipment"),
                params);
        return serializer.parseAutoStartShipmentDto(response);
    }
    /**
     * @param id default shipment ID.
     * @throws IOException
     * @throws RestServiceException
     */
    public void deleteAutoStartShipment(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("autoStartShipmentId", id.toString());

        sendGetRequest(getPathWithToken("deleteAutoStartShipment"), params);
    }
    /**
     * @param dto default shipment.
     * @return ID of new saved default shipment.
     * @throws IOException
     * @throws RestServiceException
     */
    public Long saveAutoStartShipment(final AutoStartShipmentDto dto)
            throws IOException, RestServiceException {
        final JsonElement response = sendPostRequest(getPathWithToken("saveAutoStartShipment"),
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
    public List<AutoStartShipmentDto> getAutoStartShipments(
            final Integer pageIndex, final Integer pageSize)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        final JsonArray response = sendGetRequest(
                getPathWithToken("getAutoStartShipments"), params).getAsJsonArray();

        final List<AutoStartShipmentDto> result = new LinkedList<AutoStartShipmentDto>();
        for (final JsonElement e : response) {
            result.add(serializer.parseAutoStartShipmentDto(e));
        }
        return result;
    }
}
