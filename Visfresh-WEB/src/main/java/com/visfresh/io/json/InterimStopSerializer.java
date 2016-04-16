/**
 *
 */
package com.visfresh.io.json;

import com.google.gson.JsonObject;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopSerializer {
    /**
     * Default constructor.
     */
    public InterimStopSerializer() {
        super();
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
    public LocationProfile parseLocation(final JsonObject json) {
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
}
