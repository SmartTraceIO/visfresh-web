/**
 *
 */
package com.visfresh.io.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Language;
import com.visfresh.entities.LocationProfile;
import com.visfresh.io.InterimStopDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopSerializer extends AbstractJsonSerializer {
    private final DateFormat isoFormat;

    /**
     * Default constructor.
     * @param lang language.
     * @param tz time zone.
     */
    public InterimStopSerializer(final Language lang, final TimeZone tz) {
        super(tz);
        this.isoFormat = createIsoFormat(lang, tz);
    }

    /**
     * @param l
     * @return
     */
    public JsonObject toJson(final LocationProfile l) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", l.getId());
        json.addProperty("lat", l.getLocation().getLatitude());
        json.addProperty("lon", l.getLocation().getLongitude());
        json.addProperty("radius", l.getRadius());
        return json;
    }
    public LocationProfile parseLocationProfile(final JsonObject json) {
        if (json == null) {
            return null;
        }

        final LocationProfile loc = new LocationProfile();
        loc.setId(json.get("id").getAsLong());
        loc.getLocation().setLatitude(json.get("lat").getAsDouble());
        loc.getLocation().setLongitude(json.get("lon").getAsDouble());
        loc.setRadius(json.get("radius").getAsInt());
        return loc;
    }
    /**
     * @param req
     * @return
     */
    public JsonObject toJson(final InterimStopDto req) {
        if (req == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        if (req.getId() != null) {
            json.addProperty("id", req.getId());
        }
        json.addProperty("shipmentId", req.getShipmentId());
        json.addProperty("locationId", req.getLocationId());
        json.addProperty("time", req.getTime());
        json.addProperty("stopDate", req.getDate() == null ? null : isoFormat.format(req.getDate()));
        return json;
    }

    public InterimStopDto parseInterimStopDto(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final InterimStopDto req = new InterimStopDto();

        req.setId(asLong(json.get("id")));
        req.setShipmentId(asLong(json.get("shipmentId")));
        req.setLocationId(asLong(json.get("locationId")));
        req.setTime(asInt(json.get("time")));
        if (json.has("stopDate")) {
            req.setDate(parseIsoDate(json.get("stopDate")));
        }

        return req;
    }
    /**
     * @param el
     * @return
     */
    private Date parseIsoDate(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        try {
            return isoFormat.parse(el.getAsString());
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
