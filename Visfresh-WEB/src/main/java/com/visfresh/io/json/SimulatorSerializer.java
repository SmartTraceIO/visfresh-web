/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

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
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    private static final String USER = "user";
    private static final String TARGET_DEVICE = "targetDevice";
    private static final String SOURCE_DEVICE = "sourceDevice";
    private static final String VELOSITY = "velosity";

    /**
     * @param user user.
     */
    public SimulatorSerializer(final User user) {
        this(user.getTimeZone());
    }
    /**
     * @param tz the user's time zone.
     */
    public SimulatorSerializer(final TimeZone tz) {
        super(tz);
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
        dto.setSourceDevice(asString(json.get(SOURCE_DEVICE)));
        dto.setTargetDevice(asString(json.get(TARGET_DEVICE)));
        dto.setUser(asString(json.get(USER)));

        return dto;
    }
    public JsonObject toJson(final SimulatorDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(SOURCE_DEVICE, dto.getSourceDevice());
        json.addProperty(TARGET_DEVICE, dto.getTargetDevice());
        json.addProperty(USER, dto.getUser());
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
        req.setEndDate(asString(json.get(END_DATE)));
        req.setStartDate(asString(json.get(START_DATE)));
        req.setUser(asString(json.get(USER)));

        final Integer velosity = asInteger(json.get(VELOSITY));
        if (velosity != null) {
            req.setVelosity(velosity);
        }

        return req;
    }
    public JsonObject toJson(final StartSimulatorRequest req) {
        if (req == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(USER, req.getUser());
        json.addProperty(START_DATE, req.getStartDate());
        json.addProperty(END_DATE, req.getEndDate());
        json.addProperty(VELOSITY, req.getVelosity());

        return json;
    }
}
