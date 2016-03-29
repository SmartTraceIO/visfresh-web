/**
 *
 */
package com.visfresh.io.json;

import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Location;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceStateSerializer {
    private static final String PROPERTIES = "properties";

    /**
     * Default constructor.
     */
    public DeviceStateSerializer() {
        super();
    }
    /**
     * @param state
     * @return
     */
    public DeviceState parseState(final String state) {
        return parseState(SerializerUtils.parseJson(state).getAsJsonObject());
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

        final JsonObject props = new JsonObject();
        json.add(PROPERTIES, props);
        for (final String key : state.getKeys()) {
            final String value = state.getProperty(key);
            if (value != null) {
                props.addProperty(key, value);
            }
        }

        //last location
        if (state.getLastLocation() != null) {
            json.addProperty("lat", state.getLastLocation().getLatitude());
            json.addProperty("lon", state.getLastLocation().getLongitude());
        }
        return json;
    }
    /**
     * @param json
     * @return
     */
    private DeviceState parseState(final JsonObject json) {
        final DeviceState s = new DeviceState();

        final JsonElement props = json.get(PROPERTIES);
        if (props != null) {
            final JsonObject obj = props.getAsJsonObject();
            final Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
            for (final Entry<String, JsonElement> e : entrySet) {
                s.setProperty(e.getKey(), e.getValue().getAsString());
            }
        }

        //last location
        if (json.has("lat")) {
            final Location loc = new Location();
            loc.setLatitude(json.get("lat").getAsDouble());
            loc.setLongitude(json.get("lon").getAsDouble());
            s.setLastLocation(loc);
        }
        return s;
    }
}
