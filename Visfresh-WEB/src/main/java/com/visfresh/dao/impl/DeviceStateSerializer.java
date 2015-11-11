/**
 *
 */
package com.visfresh.dao.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.io.AbstractJsonSerializer;
import com.visfresh.rules.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceStateSerializer extends AbstractJsonSerializer {
    /**
     * Default constructor.
     */
    public DeviceStateSerializer() {
        super(TimeZone.getTimeZone("UTC"));
    }

    /* (non-Javadoc)
     * @see com.visfresh.io.AbstractJsonSerializer#createDateFormat()
     */
    @Override
    protected SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    /**
     * @param state
     * @return
     */
    public DeviceState parseState(final String state) {
        return parseState(parseJson(state).getAsJsonObject());
    }
    /**
     * @param json
     * @return
     */
    private DeviceState parseState(final JsonObject json) {
        final DeviceState s = new DeviceState();
        s.getDates().putAll(jsonToDates(json.get("dates").getAsJsonObject()));
        return s;
    }
    /**
     * @param json
     * @return
     */
    private Map<String, Date> jsonToDates(final JsonObject json) {
        final Map<String, Date> map = new HashMap<String, Date>();
        for (final Map.Entry<String, JsonElement> e : json.entrySet()) {
            map.put(e.getKey(), parseDate(e.getValue().getAsString()));
        }
        return map ;
    }

    /**
     * @param state state.
     * @return string.
     */
    public String toString(final DeviceState state) {
        return toJson(state).toString();
    }
    /**
     * @param state
     * @return
     */
    private JsonObject toJson(final DeviceState state) {
        if (state == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.add("dates", datesToJson(state.getDates()));
        return json;
    }

    /**
     * @param dates
     * @return
     */
    private JsonObject datesToJson(final Map<String, Date> dates) {
        final JsonObject obj = new JsonObject();
        for (final Map.Entry<String, Date> e : dates.entrySet()) {
            obj.addProperty(e.getKey(), formatDate(e.getValue()));
        }
        return obj;
    }
}
