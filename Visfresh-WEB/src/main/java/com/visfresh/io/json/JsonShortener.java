/**
 *
 */
package com.visfresh.io.json;

import com.google.gson.JsonObject;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface JsonShortener {
    JsonObject shorten(JsonObject json);
    JsonObject unShorten(JsonObject json);
}
