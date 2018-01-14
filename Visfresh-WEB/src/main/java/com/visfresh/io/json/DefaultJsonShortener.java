/**
 *
 */
package com.visfresh.io.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.impl.json.JsonShortener;

/**
 * Warning!!! Is not thread save.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultJsonShortener implements JsonShortener {
    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, String> currentAliases = new HashMap<>();

    /**
     * Default constructor.
     */
    public DefaultJsonShortener() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.json.JsonShortener#shorten(com.google.gson.JsonObject)
     */
    @Override
    public JsonObject shorten(final JsonObject json) {
        currentAliases.putAll(aliases);
        try {
            return shortenJsonObect(json);
        } finally {
            currentAliases.clear();;
        }
    }
    /**
     * @param json
     * @return
     */
    private JsonObject shortenJsonObect(final JsonObject json) {
        final JsonObject result = new JsonObject();

        for (final Map.Entry<String, JsonElement> e : json.entrySet()) {
            final String name = getName(e.getKey());

            final JsonElement value = shortenElement(e.getValue());
            result.add(name, value);
        }

        return result;
    }
    /**
     * @param value
     * @return
     */
    private JsonElement shortenElement(final JsonElement value) {
        if (value.isJsonObject()) {
            return shortenJsonObect(value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return shortenArray(value.getAsJsonArray());
        }
        return value;
    }
    /**
     * @param array
     * @return
     */
    private JsonArray shortenArray(final JsonArray array) {
        final JsonArray result = new JsonArray();
        for (final JsonElement e : array) {
            result.add(shortenElement(e));
        }
        return result;
    }
    /**
     * @param originName
     * @return
     */
    private String getName(final String originName) {
        final String name = currentAliases.get(originName);
        return name == null ? originName : name;
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.json.JsonShortener#unShorten(com.google.gson.JsonObject)
     */
    @Override
    public JsonObject unShorten(final JsonObject json) {
        try {
            // create inverse alias map
            for (final Map.Entry<String, String> e : aliases.entrySet()) {
                currentAliases.put(e.getValue(), e.getKey());
            }
        return shortenJsonObect(json);
        } finally {
            currentAliases.clear();
        }
    }
    /**
     * @return the aliases
     */
    public Map<String, String> getAliases() {
        return aliases;
    }
}
