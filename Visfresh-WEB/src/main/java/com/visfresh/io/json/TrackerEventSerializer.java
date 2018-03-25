/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.TrackerEventConstants;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public TrackerEventSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param e tracker event.
     * @return JSON object.
     */
    public JsonObject toJson(final TrackerEvent e) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(TrackerEventConstants.PROPERTY_BATTERY, e.getBattery());
        obj.addProperty(TrackerEventConstants.PROPERTY_ID, e.getId());
        obj.addProperty(TrackerEventConstants.PROPERTY_BEACON, e.getBeaconId());
        obj.addProperty(TrackerEventConstants.PROPERTY_TEMPERATURE, e.getTemperature());
        obj.addProperty(TrackerEventConstants.PROPERTY_TIME, formatDate(e.getTime()));
        obj.addProperty(TrackerEventConstants.PROPERTY_CREATEDON, formatDate(e.getCreatedOn()));
        obj.addProperty(TrackerEventConstants.PROPERTY_TYPE, e.getType().toString());
        obj.addProperty(TrackerEventConstants.PROPERTY_LATITUDE, e.getLatitude());
        obj.addProperty(TrackerEventConstants.PROPERTY_LONGITUDE, e.getLongitude());
        return obj;
    }
    public TrackerEvent parseTrackerEvent(final JsonObject json) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(asInt(json.get(TrackerEventConstants.PROPERTY_BATTERY)));
        e.setId(asLong(json.get(TrackerEventConstants.PROPERTY_ID)));
        e.setBeaconId(asString(json.get(TrackerEventConstants.PROPERTY_BEACON)));
        e.setTemperature(asDouble(json.get(TrackerEventConstants.PROPERTY_TEMPERATURE)));
        e.setTime(asDate(json.get(TrackerEventConstants.PROPERTY_TIME)));
        e.setCreatedOn(asDate(json.get(TrackerEventConstants.PROPERTY_CREATEDON)));
        e.setType(TrackerEventType.valueOf(asString(json.get(TrackerEventConstants.PROPERTY_TYPE))));

        final JsonElement lat = json.get(TrackerEventConstants.PROPERTY_LATITUDE);
        final JsonElement lon = json.get(TrackerEventConstants.PROPERTY_LONGITUDE);

        if (lat != null && lon != null) {
            e.setLatitude(asDouble(lat));
            e.setLongitude(asDouble(lon));
        }

        return e;
    }
}
