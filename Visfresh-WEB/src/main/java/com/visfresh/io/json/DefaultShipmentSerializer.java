/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.DefaultShipmentConstants;
import com.visfresh.io.DefaultShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentSerializer extends AbstractJsonSerializer
        implements DefaultShipmentConstants {
    /**
     * @param tz time zone.
     */
    public DefaultShipmentSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param e JSON element to parse.
     * @return default shipment DTO.
     */
    public DefaultShipmentDto parseDefaultShipmentDto(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final DefaultShipmentDto dto = new DefaultShipmentDto();
        dto.setId(asLong(json.get(ID)));
        dto.setTemplate(json.get(TEMPLATE).getAsLong());

        //start locations.
        JsonArray array = json.get(START_LOCATIONS).getAsJsonArray();
        for (final JsonElement el : array) {
            dto.getStartLocations().add(el.getAsLong());
        }

        //end locations.
        array = json.get(END_LOCATIONS).getAsJsonArray();
        for (final JsonElement el : array) {
            dto.getEndLocations().add(el.getAsLong());
        }

        return dto;
    }

    /**
     * @param ds
     * @return
     */
    public JsonObject toJson(final DefaultShipmentDto ds) {
        if (ds == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(TEMPLATE, ds.getTemplate());
        json.addProperty(ID, ds.getId());

        //start locations
        JsonArray array = new JsonArray();
        json.add(START_LOCATIONS, array);

        for (final Long id : ds.getStartLocations()) {
            array.add(new JsonPrimitive(id));
        }

        //end locations
        array = new JsonArray();
        json.add(END_LOCATIONS, array);

        for (final Long id : ds.getEndLocations()) {
            array.add(new JsonPrimitive(id));
        }

        return json;
    }
}
