/**
 *
 */
package com.visfresh.impl.singleshipment;

import com.google.gson.JsonElement;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SingleShipmentPartBuilder {
    int MAX_PRIORITY = 1000;
    int MIN_PRIORITY = 0;

    /**
     * @return merge priority.
     */
    int getPriority();
    /**
     * @param context build context.
     */
    void build(SingleShipmentBuildContext context);
    /**
     * Fetch the data (from DB).
     */
    void fetchData();

    /**
     * @param object
     * @return
     */
    default boolean asBoolean(final Object object) {
        if (object instanceof Number) {
            return ((Number) object).intValue() != 0;
        }
        return Boolean.TRUE.equals(object);
    }
    default double asDouble(final Object object) {
        return ((Number) object).doubleValue();
    }
    default String asString(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        return e.getAsString();
    }
    /**
     * @param object
     * @return
     */
    default Long asLong(final Object object) {
        final Number num = (Number) object;
        return num == null ? null : num.longValue();
    }
    /**
     * @param object
     * @return
     */
    default Integer asInteger(final Object object) {
        if (object instanceof String) {
            return Integer.valueOf((String) object);
        }
        final Number num = (Number) object;
        return num == null ? null : num.intValue();
    }
}
