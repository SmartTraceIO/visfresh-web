/**
 *
 */
package com.visfresh.utils;

import java.io.Reader;
import java.io.StringReader;
import java.util.TimeZone;

import com.google.gson.JsonElement;
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
}
