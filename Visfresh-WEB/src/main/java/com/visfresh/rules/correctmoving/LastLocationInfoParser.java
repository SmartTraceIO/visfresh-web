/**
 *
 */
package com.visfresh.rules.correctmoving;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LastLocationInfoParser {
    /**
     * Default constructor.
     */
    public LastLocationInfoParser() {
        super();
    }

    /**
     * @param info
     *            last location info.
     * @return JSON object.
     */
    public JsonObject toJSon(final LastLocationInfo info) {
        if (info == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        // last location
        json.addProperty("lat", info.getLastLocation().getLatitude());
        json.addProperty("lon", info.getLastLocation().getLongitude());
        json.addProperty("lastReadTime",
                createDateFormat().format(info.getLastReadTime()));

        return json;
    }

    public LastLocationInfo parseLastLocationInfo(final JsonObject json) {
        final LastLocationInfo info = new LastLocationInfo();

        // last location
        final Location loc = new Location();
        loc.setLatitude(json.get("lat").getAsDouble());
        loc.setLongitude(json.get("lon").getAsDouble());
        info.setLastLocation(loc);

        // set last reading.
        try {
            info.setLastReadTime(createDateFormat().parse(
                    json.get("lastReadTime").getAsString()));
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * @return
     */
    private DateFormat createDateFormat() {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Language.English, TimeZone.getTimeZone("UTC"));
    }
}
