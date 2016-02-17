/**
 *
 */
package com.visfresh.utils;

import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class SerializerUtils {
    public static final TimeZone UTС = TimeZone.getTimeZone("UTC");
    /**
     * Default constructor.
     */
    public SerializerUtils() {
        super();
    }
    /**
     * @param errorCode error code.
     * @param msg error message.
     * @return error object.
     */
    public static JsonObject createErrorStatus(final int errorCode, final String msg) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("code", errorCode);
        obj.addProperty("message", msg);
        return obj;
    }
    /**
     * @param text JSON text.
     * @return JSON element.
     */
    public static JsonElement parseJson(final String text) {
        if (text == null) {
            return null;
        }

        final Reader in = new StringReader(text);
        return new JsonParser().parse(in);
    }
    /**
     * @param idFieldName ID field name
     * @param id entity ID.
     * @return JSON object.
     */
    public static JsonObject idToJson(final String idFieldName, final Long id) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(idFieldName, id);
        return obj;
    }
    /**
     * Warning!!! This feature does not support of merging of arrays.
     * @param from source object.
     * @param pattern pattern object.
     * @return result JSON.
     */
    public static JsonObject merge(final JsonObject from,
            final JsonObject pattern) {
        final JsonObject result = parseJson(pattern.toString()).getAsJsonObject();

        //extract keys.
        final List<String> keys = new LinkedList<String>();
        for (final Entry<String, JsonElement> e : result.entrySet()) {
            keys.add(e.getKey());
        }

        //do merge
        for (final String key : keys) {
            final JsonElement e = result.get(key);
            if (from.has(key)) {
                final JsonElement newValue = from.get(key);
                if (newValue.isJsonNull()) {
                    result.add(key, JsonNull.INSTANCE);
                } else if (e.isJsonObject() && newValue.isJsonObject()) {
                    final JsonObject newObject = merge(newValue.getAsJsonObject(),
                            e.getAsJsonObject());
                    result.add(key, newObject);
                } else {
                    result.add(key, newValue);
                }
            }
        }

        return result;
    }
}
