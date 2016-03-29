/**
 *
 */
package com.visfresh.io.json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.rules.state.RulesState;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSessionSerializer extends AbstractJsonSerializer {
    /**
     *
     */
    private static final String ALERTS_SUPPRESSED = "alertsSuppressed";
    /**
     *
     */
    private static final String SHIPMENT_PROPERTIES = "shipmentProperties";
    private static final String TEMPERATURE_ALERTS = "temperatureAlerts";

    /**
     * Default constructor.
     */
    public ShipmentSessionSerializer() {
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
    public ShipmentSession parseState(final String state) {
        return parseState(SerializerUtils.parseJson(state).getAsJsonObject());
    }

    /**
     * @param state state.
     * @return string.
     */
    public String toString(final ShipmentSession state) {
        return toJson(state).toString();
    }
    /**
     * @param state
     * @return
     */
    private JsonObject toJson(final ShipmentSession state) {
        if (state == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.add(TEMPERATURE_ALERTS, toJson(state.getTemperatureAlerts()));
        json.addProperty(ALERTS_SUPPRESSED, state.isAlertsSuppressed());

        final JsonObject props = new JsonObject();
        json.add(SHIPMENT_PROPERTIES, props);
        for (final String key : state.getShipmentKeys()) {
            final String value = state.getShipmentProperty(key);
            if (value != null) {
                props.addProperty(key, value);
            }
        }
        return json;
    }
    /**
     * @param json
     * @return
     */
    private ShipmentSession parseState(final JsonObject json) {
        final ShipmentSession s = new ShipmentSession();
        parseRulesState(s.getTemperatureAlerts(), json.get(TEMPERATURE_ALERTS));

        final JsonElement props = json.get(SHIPMENT_PROPERTIES);
        if (props != null) {
            final JsonObject obj = props.getAsJsonObject();
            final Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
            for (final Entry<String, JsonElement> e : entrySet) {
                s.setShipmentProperty(e.getKey(), e.getValue().getAsString());
            }
        }
        final JsonElement alertsSuppressed = json.get(ALERTS_SUPPRESSED);
        if (alertsSuppressed != null) {
            s.setAlertsSuppressed(Boolean.TRUE.equals(alertsSuppressed.getAsBoolean()));
        }

        return s;
    }
    /**
     * @param e
     * @return
     */
    private RulesState parseRulesState(final RulesState s, final JsonElement e) {
        final JsonObject json = e.getAsJsonObject();
        s.getDates().putAll(jsonToDates(json.get("dates").getAsJsonObject()));
        s.getProperties().putAll(SerializerUtils.parseStringMap(json.get("properties")));
        return s;
    }
    /**
     * @param s
     * @return
     */
    private JsonObject toJson(final RulesState s) {
        final JsonObject obj = new JsonObject();
        obj.add("dates", datesToJson(s.getDates()));
        obj.add("properties", SerializerUtils.toJson(s.getProperties()));
        return obj;
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
