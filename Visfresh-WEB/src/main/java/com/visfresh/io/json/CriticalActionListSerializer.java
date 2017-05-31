/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CriticalActionListSerializer extends AbstractJsonSerializer {
    /**
     * Default constructor.
     */
    public CriticalActionListSerializer() {
        super(TimeZone.getDefault());
    }

    /**
     * @param array
     * @return
     */
    public List<String> parseActions(final JsonArray array) {
        final List<String> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(e.getAsString());
        }
        return list;
    }
    /**
     * @param list action list.
     * @return JSON array.
     */
    public JsonArray toJson(final List<String> list) {
        final JsonArray array = new JsonArray();
        for (final String action : list) {
            array.add(new JsonPrimitive(action));
        }
        return array;
    }
}
