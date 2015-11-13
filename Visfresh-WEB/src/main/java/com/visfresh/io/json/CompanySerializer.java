/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.CompanyConstants;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanySerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public CompanySerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param json
     * @return
     */
    public Company parseCompany(final JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        final JsonObject obj = json.getAsJsonObject();
        final Company c = new Company();
        c.setDescription(asString(obj.get(CompanyConstants.PROPERTY_DESCRIPTION)));
        c.setId(asLong(obj.get(CompanyConstants.PROPERTY_ID)));
        c.setName(asString(obj.get(CompanyConstants.PROPERTY_NAME)));
        return c;
    }
    public JsonObject toJson(final Company c) {
        if (c == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(CompanyConstants.PROPERTY_ID, c.getId());
        obj.addProperty(CompanyConstants.PROPERTY_NAME, c.getName());
        obj.addProperty(CompanyConstants.PROPERTY_DESCRIPTION, c.getDescription());
        return obj;
    }
}
