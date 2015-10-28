/**
 *
 */
package com.visfresh.io;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractJsonSerializer {
    /**
     * The date format.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     *
     */
    public AbstractJsonSerializer() {
        super();
    }

    /**
     * @param customFields
     * @return
     */
    public static JsonObject toJson(final Map<String, String> customFields) {
        final JsonObject obj = new JsonObject();
        for (final Map.Entry<String, String> e : customFields.entrySet()) {
            obj.addProperty(e.getKey(), e.getValue());
        }
        return obj;
    }

    /**
     * @param je
     * @return
     */
    public static Map<String, String> parseStringMap(final JsonElement je) {
        if (je == null || je.isJsonNull()) {
            return new HashMap<String, String>();
        }

        final JsonObject json = je.getAsJsonObject();
        final Map<String, String> map = new HashMap<String, String>();
        for (final Entry<String, JsonElement> e : json.entrySet()) {
            map.put(e.getKey(), e.getValue().getAsString());
        }
        return map;
    }

    /**
     * @param str
     * @return
     */
    public static Date parseDate(final String str) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(str);
        } catch (final ParseException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * @param e
     * @return
     */
    protected double asDouble(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsDouble();
    }

    /**
     * @param e
     * @return
     */
    protected int asInt(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsInt();
    }

    /**
     * @param e JSON element.
     * @return JSON element as long.
     */
    protected Long asLong(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsLong();
    }

    /**
     * @param e JSON element.
     * @return JSON element as string.
     */
    protected String asString(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    /**
     * @param e JSON element.
     * @return JSON element as boolean.
     */
    protected Boolean asBoolean(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsBoolean();
    }

    /**
     * @param e JSON string.
     * @return JSON string as boolean.
     */
    protected Date asDate(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : parseDate(e.getAsString());
    }

    /**
     * @param date
     * @return
     */
    public static String formatDate(final Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
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
     * @param time
     * @return the time in 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format.
     */
    protected static String timeToString(final Date time) {
        return time == null ? null : new SimpleDateFormat(DATE_FORMAT).format(time);
    }

}
