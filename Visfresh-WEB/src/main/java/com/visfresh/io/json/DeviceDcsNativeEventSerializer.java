/**
 *
 */
package com.visfresh.io.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.constants.TrackerEventConstants;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDcsNativeEventSerializer extends AbstractJsonSerializer {
    /**
     * @param tz
     */
    public DeviceDcsNativeEventSerializer() {
        super(SerializerUtils.UTС);
    }
    public DeviceDcsNativeEvent parseDeviceDcsNativeEvent(final JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final JsonObject obj = json.getAsJsonObject();

        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setBattery(asInt(obj.get(TrackerEventConstants.PROPERTY_BATTERY)));
        e.setTemperature(asDouble(obj.get(TrackerEventConstants.PROPERTY_TEMPERATURE)));
        try {
            e.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(asString(obj.get("time"))));
        } catch (final ParseException e1) {
            e1.printStackTrace();
        }
        e.setType(asString(obj.get("type")));
        e.getLocation().setLatitude(asDouble(obj.get(TrackerEventConstants.PROPERTY_LATITUDE)));
        e.getLocation().setLongitude(asDouble(obj.get(TrackerEventConstants.PROPERTY_LONGITUDE)));
        e.setImei(asString(obj.get("imei")));

        return e;
    }
    public JsonElement toJson(final DeviceDcsNativeEvent e) {
        if (e == null) {
            return JsonNull.INSTANCE;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(SerializerUtils.UTС);

        final JsonObject obj = new JsonObject();
        obj.addProperty(TrackerEventConstants.PROPERTY_BATTERY, e.getBattery());
        obj.addProperty(TrackerEventConstants.PROPERTY_TEMPERATURE, e.getTemperature());
        obj.addProperty("time", sdf.format(e.getTime()));
        obj.addProperty("type", e.getType());
        obj.addProperty(TrackerEventConstants.PROPERTY_LATITUDE, e.getLocation().getLatitude());
        obj.addProperty(TrackerEventConstants.PROPERTY_LONGITUDE, e.getLocation().getLongitude());
        obj.addProperty("imei", e.getImei());
        return obj;
    }
}
