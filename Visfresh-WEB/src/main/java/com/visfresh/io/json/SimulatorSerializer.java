/**
 *
 */
package com.visfresh.io.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorSerializer extends AbstractJsonSerializer {
    /**
     * @param user user.
     */
    public SimulatorSerializer(final User user) {
        super(user.getTimeZone());
    }

    /**
     * @param el JSON source.
     * @return
     */
    public SimulatorDto parseSimulator(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final SimulatorDto dto = new SimulatorDto();
        return dto;
    }
    public JsonObject toJson(final SimulatorDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        return json;
    }

    /**
     * @param el
     * @return
     */
    public StartSimulatorRequest parseStartRequest(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final StartSimulatorRequest req = new StartSimulatorRequest();
        return req;
    }
}
