/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonPropertyNamesExplorer {
    /**
     * JSON object.
     */
    private final JsonObject json;

    /**
     * @param json JSON object.
     */
    public JsonPropertyNamesExplorer(final JsonObject json) {
        super();
        this.json = json;
    }

    public void explore(final JsonPropertyNameHandler h) {
        explore(json, h);
    }

    /**
     * @param obj current JSON object.
     * @param h property names handler.
     */
    private void explore(final JsonObject obj, final JsonPropertyNameHandler h) {
        final List<JsonElement> childs = new LinkedList<>();

        //process children
        for (final Map.Entry<String, JsonElement> e : obj.entrySet()) {
            h.handlePropertyName(e.getKey());
            if (e.getValue().isJsonObject() || e.getValue().isJsonArray()) {
                childs.add(e.getValue());
            }
        }

        //process found JSON object children recursively
        for (final JsonElement o : childs) {
            if (o.isJsonObject()) {
                explore((JsonObject) o, h);
            } else if (o.isJsonArray()) {
                for (final JsonElement e : (JsonArray) o) {
                    if (e.isJsonObject()) {
                        explore((JsonObject) e, h);
                    }
                }
            }
        }
    }
}
