/**
 *
 */
package com.visfresh.io.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Language;
import com.visfresh.utils.DateTimeUtils;

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
     * @param e
     * @return
     */
    public static double asDouble(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsDouble();
    }
    /**
     * @param e
     * @return
     */
    public static Double asDoubleObject(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsDouble();
    }

    /**
     * @param e
     * @return
     */
    public static int asInt(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsInt();
    }
    /**
     * @param e
     * @return
     */
    public static Integer asInteger(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsInt();
    }

    /**
     * @param e JSON element.
     * @return JSON element as long.
     */
    public static Long asLong(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsLong();
    }

    /**
     * @param e JSON element.
     * @return JSON element as string.
     */
    public static String asString(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    /**
     * @param e JSON element.
     * @return JSON element as boolean.
     */
    public static Boolean asBoolean(final JsonElement e) {
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
     * @param date date to format.
     * @return
     */
    protected String formatDate(final Date date) {
        return formatDate(date, this.timeZone);
    }
    /**
     * @param date
     * @param t
     * @return
     */
    protected String formatDate(final Date date, final TimeZone t) {
        if (date == null) {
            return null;
        }

        final SimpleDateFormat sdf = createDateFormat();
        sdf.setTimeZone(t);
        return sdf.format(date);
    }
    /**
     * @param str
     * @return
     */
    public Date parseDate(final String str) {
        return parseDate(str, timeZone);
    }
    /**
     * @param str
     * @param tz
     * @return
     */
    protected Date parseDate(final String str, final TimeZone tz) {
        if (str == null) {
            return null;
        }

        final SimpleDateFormat sdf = createDateFormat();
        sdf.setTimeZone(tz);

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
     * @param array
     * @return
     */
    public static List<Long> asLongList(final JsonArray array) {
        final List<Long> list = new LinkedList<Long>();
        for (final JsonElement l : array) {
            list.add(l.getAsLong());
        }
        return list;
    }
    /**
     * @param array
     * @return
     */
    protected List<Long> parseLongList(final JsonArray array) {
        final List<Long> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(asLong(e));
        }
        return list;
    }

    /**
     * @param e JSON elmeent.
     * @return
     */
    public static List<Long> asLongList(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        return asLongList(e.getAsJsonArray());
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
     * @param els
     * @return
     */
    public boolean isNull(final JsonElement... els) {
        if (els == null) {
            return true;
        }

        for (final JsonElement e : els) {
            if (e == null || e.isJsonNull()) {
                return true;
            }
        }
        return false;
    }
    /**
     * @param parent
     * @param name
     * @return
     */
    public boolean has(final JsonObject parent, final String name) {
        if (parent == null || !parent.has(name)) {
            return false;
        }

        final JsonElement e = parent.get(name);
        return e != null && !e.isJsonNull();
    }

    /**
     * @return date format.
     */
    protected SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT);
    }
    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
    /**
     * @param lang language.
     * @param tz time zone
     * @return
     */
    protected DateFormat createPrettyFormat(final Language lang, final TimeZone tz) {
        return DateTimeUtils.createPrettyFormat(lang, tz);
    }
    /**
     * @param lang language.
     * @param tz time zone
     * @return
     */
    protected DateFormat createIsoFormat(final Language lang, final TimeZone tz) {
        return DateTimeUtils.createIsoFormat(lang, tz);
    }
}
