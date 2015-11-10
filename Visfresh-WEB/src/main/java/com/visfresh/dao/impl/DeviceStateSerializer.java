/**
 *
 */
package com.visfresh.dao.impl;

import java.text.SimpleDateFormat;
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
        return parseState(parseJson(state));
    }
    /**
     * @param parseJson
     * @return
     */
    private DeviceState parseState(final JsonElement parseJson) {
        final DeviceState s = new DeviceState();
        return s;
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
        return json;
    }
}
