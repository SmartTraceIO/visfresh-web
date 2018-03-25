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
import com.visfresh.impl.services.DeviceDcsNativeEvent;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDcsNativeEventSerializer extends AbstractJsonSerializer {
    private static final String CREATED_ON = "createdOn";
    private static final String IMEI = "imei";
    private static final String TYPE = "type";
    private static final String TIME = "time";

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

        //event date
        final SimpleDateFormat sdf = createUtcDateFormat();
        try {
            e.setDate(sdf.parse(asString(obj.get(TIME))));
        } catch (final ParseException e1) {
            e1.printStackTrace();
        }

        //created on date
        final String createdOn = asString(obj.get(CREATED_ON));
        if (createdOn != null) {
            try {
                e.setCreatedOn(sdf.parse(createdOn));
            } catch (final ParseException e1) {
                e1.printStackTrace();
            }
        }

        e.setType(asString(obj.get(TYPE)));

        final JsonElement lat = obj.get(TrackerEventConstants.PROPERTY_LATITUDE);
        final JsonElement lon = obj.get(TrackerEventConstants.PROPERTY_LONGITUDE);
        if (!isNull(lat, lon)) {
            e.setLocation(asDouble(lat), asDouble(lon));
        }

        e.setImei(asString(obj.get(IMEI)));
        e.setBeacon(asString(obj.get(TrackerEventConstants.PROPERTY_BEACON)));

        return e;
    }
    public JsonElement toJson(final DeviceDcsNativeEvent e) {
        if (e == null) {
            return JsonNull.INSTANCE;
        }
        final SimpleDateFormat sdf = createUtcDateFormat();

        final JsonObject obj = new JsonObject();
        obj.addProperty(TrackerEventConstants.PROPERTY_BATTERY, e.getBattery());
        obj.addProperty(TrackerEventConstants.PROPERTY_TEMPERATURE, e.getTemperature());
        obj.addProperty(TIME, sdf.format(e.getDate()));
        obj.addProperty(CREATED_ON, sdf.format(e.getCreatedOn()));
        obj.addProperty(TYPE, e.getType());
        if (e.getLocation() != null) {
            obj.addProperty(TrackerEventConstants.PROPERTY_LATITUDE, e.getLocation().getLatitude());
            obj.addProperty(TrackerEventConstants.PROPERTY_LONGITUDE, e.getLocation().getLongitude());
        } else {
            obj.add(TrackerEventConstants.PROPERTY_LATITUDE, JsonNull.INSTANCE);
            obj.add(TrackerEventConstants.PROPERTY_LONGITUDE, JsonNull.INSTANCE);
        }
        obj.addProperty(IMEI, e.getImei());
        obj.addProperty(TrackerEventConstants.PROPERTY_BEACON, e.getBeacon());
        return obj;
    }
    /**
     * @return
     */
    private SimpleDateFormat createUtcDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    }
}
