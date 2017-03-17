/**
 *
 */
package com.visfresh.controllers.lite;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Language;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentSerializer extends AbstractJsonSerializer {
    private final DateFormat isoFormat;
    private final DateFormat prettyFormat;
    private final TemperatureUnits units;

    /**
     * @param tz time zone.
     * @param lang language.
     * @param units temperature units.
     */
    public LiteShipmentSerializer(final TimeZone tz, final Language lang, final TemperatureUnits units) {
        super(tz);
        this.isoFormat = createIsoFormat(lang, tz);
        this.prettyFormat = createPrettyFormat(lang, tz);
        this.units = units;
    }
    /**
     * @param s lite shipment object.
     * @return JSON serialized lite shipment object.
     */
    public JsonObject toJson(final LiteShipment s) {
        if (s == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("status", s.getStatus().toString());

        json.addProperty(ShipmentConstants.DEVICE_SN, s.getDeviceSN());
        json.addProperty("tripCount", s.getTripCount());

        json.addProperty("shipmentId", s.getShipmentId());
        json.addProperty("shipmentDate", formatPretty(s.getShipmentDate()));
        json.addProperty("shipmentDateISO", formatIso(s.getShipmentDate()));

        json.addProperty("shippedFrom", s.getShippedFrom());
        json.addProperty("shippedTo", s.getShippedTo());
        json.addProperty("estArrivalDate", formatPretty(s.getEstArrivalDate()));
        json.addProperty("estArrivalDateISO", formatIso(s.getEstArrivalDate()));
        json.addProperty("actualArrivalDate", formatPretty(s.getActualArrivalDate()));
        json.addProperty("actualArrivalDateISO", formatIso(s.getActualArrivalDate()));
        json.addProperty("percentageComplete", s.getPercentageComplete());

        json.addProperty("lowerTemperatureLimit", convertToUnits(s.getLowerTemperatureLimit()));
        json.addProperty("upperTemperatureLimit", convertToUnits(s.getUpperTemperatureLimit()));

        json.add(ShipmentConstants.ALERT_SUMMARY, SerializerUtils.toJson(s.getAlertSummary()));

        json.addProperty(ShipmentConstants.SIBLING_COUNT, s.getSiblingCount());

        //key locations
        final JsonArray kls = new JsonArray();
        json.add("keyLocations", kls);

        for (final LiteKeyLocation kl : s.getKeyLocations()) {
            kls.add(toJson(kl));
        }

        return json;
    }
    public LiteShipment parseLiteShipment(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final LiteShipment s = new LiteShipment();
        s.setStatus(ShipmentStatus.valueOf(json.get("status").getAsString()));

        s.setDeviceSN(asString(json.get(ShipmentConstants.DEVICE_SN)));
        s.setTripCount(asInt(json.get("tripCount")));

        s.setShipmentId(asLong(json.get("shipmentId")));
        s.setShipmentDate(parseISO(json.get("shipmentDateISO").getAsString()));

        s.setShippedFrom(asString(json.get("shippedFrom")));
        s.setShippedTo(asString(json.get("shippedTo")));
        s.setEstArrivalDate(parseISO(json.get("estArrivalDateISO").getAsString()));
        s.setActualArrivalDate(parseISO(json.get("actualArrivalDateISO").getAsString()));
        s.setPercentageComplete(asInt(json.get("percentageComplete")));

        s.setLowerTemperatureLimit(convertFromUnits(asInt(json.get("lowerTemperatureLimit"))));
        s.setUpperTemperatureLimit(convertFromUnits(asInt(json.get("upperTemperatureLimit"))));
        s.setSiblingCount(asInt(json.get(ShipmentConstants.SIBLING_COUNT)));

        s.getAlertSummary().putAll(parseAlertSummary(json.get(ShipmentConstants.ALERT_SUMMARY)));

        //key locations
        for (final JsonElement el : json.get("keyLocations").getAsJsonArray()) {
            s.getKeyLocations().add(parseLiteKeyLocation(el));
        }

        return s;
    }

    /**
     * @param e JSON element.
     * @return map of number of alerts of different types.
     */
    private Map<AlertType, Integer> parseAlertSummary(final JsonElement e) {
        final HashMap<AlertType, Integer> map = new HashMap<>();
        if (e == null || e.isJsonNull()) {
            return map;
        }

        final JsonObject json = e.getAsJsonObject();
        for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
            map.put(AlertType.valueOf(entry.getKey()), asInt(entry.getValue()));
        }
        return map;
    }

    /**
     * @param kl
     * @return
     */
    public JsonObject toJson(final LiteKeyLocation kl) {
        if (kl == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("temperature", convertToUnits(kl.getTemperature()));
        json.addProperty("time", formatPretty(kl.getTime()));
        json.addProperty("timeISO", formatIso(kl.getTime()));
        return json;
    }
    public LiteKeyLocation parseLiteKeyLocation(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final LiteKeyLocation loc = new LiteKeyLocation();
        loc.setTemperature(convertFromUnits(asDouble(json.get("temperature"))));
        loc.setTime(parseISO(json.get("timeISO").getAsString()));
        return loc;
    }
    /**
     * @param t temperature.
     * @return converted temperature.
     */
    private double convertFromUnits(final double t) {
        double value = units == TemperatureUnits.Fahrenheit ? (t - 32) / 1.8 : t;
        //cut extra decimal signs.
        value = Math.round(value * 100) / 100.;
        return value;
    }
    /**
     * @param t temperature.
     * @return converted to user units.
     */
    protected double convertToUnits(final double t) {
        double value = units == TemperatureUnits.Fahrenheit ? t * 1.8 + 32 : t;
        //cut extra decimal signs.
        value = Math.round(value * 100) / 100.;
        return value;
    }

    /**
     * @param str date string.
     * @return date.
     */
    private Date parseISO(final String str) {
        try {
            return isoFormat.parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param date date to format.
     * @return formatted date.
     */
    private String formatIso(final Date date) {
        if (date != null) {
            return isoFormat.format(date);
        }
        return null;
    }
    /**
     * @param date date to format.
     * @return formatted date.
     */
    private String formatPretty(final Date date) {
        if (date != null) {
            return this.prettyFormat.format(date);
        }
        return null;
    }
}
