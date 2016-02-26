/**
 *
 */
package com.visfresh.io.json;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractJsonSerializer {
    /**
     * The date format.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private final TimeZone timeZone;

    /**
     *
     */
    public AbstractJsonSerializer(final TimeZone tz) {
        super();
        timeZone = tz;
    }

    /**
     * @param customFields
     * @return
     */
    public static <K, V> JsonObject toJson(final Map<K, V> customFields) {
        final JsonObject obj = new JsonObject();
        for (final Map.Entry<K, V> e : customFields.entrySet()) {
            obj.addProperty(String.valueOf(e.getKey()),
                    String.valueOf(e.getValue()));
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
     * @param e
     * @return
     */
    protected Integer asInteger(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsInt();
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
    public String formatDate(final Date date) {
        if (date == null) {
            return null;
        }

        final TimeZone t = this.timeZone;
        final SimpleDateFormat sdf = createDateFormat();
        sdf.setTimeZone(t);
        return sdf.format(date);
    }
    /**
     * @param str
     * @return
     */
    public Date parseDate(final String str) {
        if (str == null) {
            return null;
        }

        final SimpleDateFormat sdf = createDateFormat();
        sdf.setTimeZone(timeZone);

        try {
            return sdf.parse(str);
        } catch (final ParseException exc) {
            throw new RuntimeException(exc);
        }
    }
    /**
     * @param entities list of entity.
     * @return JSON array with entity IDs.
     */
    protected <E extends EntityWithId<Long>> JsonArray getIdList(final List<E> entities) {
        final JsonArray array= new JsonArray();
        for (final E e : entities) {
            array.add(new JsonPrimitive(e.getId()));
        }
        return array;
    }
    /**
     * @param entities list of entity.
     * @return JSON array with entity IDs.
     */
    protected JsonArray toJsonArray(final List<Long> entities) {
        final JsonArray array= new JsonArray();
        for (final Long e : entities) {
            array.add(new JsonPrimitive(e));
        }
        return array;
    }
    /**
     * @return date format.
     */
    protected SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT);
    }
    /**
     * @param e
     * @return
     */
    protected <IDD extends Serializable & Comparable<IDD>> IDD getId(final EntityWithId<IDD> e) {
        return e == null ? null : (IDD) e.getId();
    }
    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
}
