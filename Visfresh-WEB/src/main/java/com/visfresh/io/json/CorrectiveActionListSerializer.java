/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.CorrectiveActionsConstants;
import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionListSerializer extends AbstractJsonSerializer {
    private final Company company;

    /**
     * Default constructor.
     */
    public CorrectiveActionListSerializer(final Company c) {
        super(TimeZone.getDefault());
        this.company = c;
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

    /**
     * @param json JSON representation of critical action list
     * @return critical action list.
     */
    public CorrectiveActionList parseCorrectiveActionList(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final CorrectiveActionList list = new CorrectiveActionList();
        list.setCompany(company);
        list.setId(asLong(json.get(CorrectiveActionsConstants.LIST_ID)));
        list.setName(json.get(CorrectiveActionsConstants.LIST_NAME).getAsString());
        list.getActions().addAll(parseActions(json.get(CorrectiveActionsConstants.ACTIONS).getAsJsonArray()));
        return list;
    }

    /**
     * @param list critical action list.
     * @return JSON representation of critical action list.
     */
    public JsonObject toJson(final CorrectiveActionList list) {
        final JsonObject json = new JsonObject();
        json.addProperty(CorrectiveActionsConstants.LIST_ID, list.getId());
        json.addProperty(CorrectiveActionsConstants.LIST_NAME, list.getName());
        json.add(CorrectiveActionsConstants.ACTIONS, toJson(list.getActions()));
        return json;
    }
}
