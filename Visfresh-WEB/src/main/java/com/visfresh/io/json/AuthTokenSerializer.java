/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.services.AuthToken;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthTokenSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public AuthTokenSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param token
     * @return
     */
    public JsonObject toJson(final AuthToken token) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("token", token.getToken());
        obj.addProperty("expired", formatDate(token.getExpirationTime()));
        obj.addProperty("expiredTimestamp", DateTimeUtils.toTimestamp(token.getExpirationTime()));
        obj.addProperty("instance", token.getClientInstanceId());
        return obj;
    }
    /**
     * @param e JSON element.
     * @return AUTH token.
     */
    public AuthToken parseAuthToken(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final AuthToken token = new AuthToken(json.get("token").getAsString());
        token.setExpirationTime(parseDate(json.get("expired").getAsString()));
        token.setClientInstanceId(asString(json.get("instance")));
        return token;
    }
}
