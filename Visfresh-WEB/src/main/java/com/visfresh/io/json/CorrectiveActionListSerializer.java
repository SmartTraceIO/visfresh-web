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
import com.visfresh.constants.CorrectiveActionsConstants;
import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionListSerializer extends AbstractJsonSerializer {
    /**
     *
     */
    private static final String REQUEST_VERIFICATION = "requestVerification";
    /**
     *
     */
    private static final String ACTION = "action";
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
    public List<CorrectiveAction> parseActions(final JsonArray array) {
        final List<CorrectiveAction> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(parseCorrectiveAction(e));
        }
        return list;
    }
    /**
     * @param list action list.
     * @return JSON array.
     */
    public JsonArray toJson(final List<CorrectiveAction> list) {
        final JsonArray array = new JsonArray();
        for (final CorrectiveAction action : list) {
            array.add(toJson(action));
        }
        return array;
    }

    /**
     * @param e JSON element.
     * @return
     */
    private CorrectiveAction parseCorrectiveAction(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final CorrectiveAction action = new CorrectiveAction();

        action.setAction(asString(json.get(ACTION)));
        action.setRequestVerification(json.get(REQUEST_VERIFICATION).getAsBoolean());

        return action;
    }
    /**
     * @param action
     * @return
     */
    private JsonObject toJson(final CorrectiveAction action) {
        final JsonObject json = new JsonObject();
        json.addProperty(ACTION, action.getAction());
        json.addProperty(REQUEST_VERIFICATION, action.isRequestVerification());
        return json;
    }

    /**
     * @param json JSON representation of critical action list
     * @return critical action list.
     */
    public CorrectiveActionList parseCorrectiveActionList(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json =  e.getAsJsonObject();

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
        if (list == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(CorrectiveActionsConstants.LIST_ID, list.getId());
        json.addProperty(CorrectiveActionsConstants.LIST_NAME, list.getName());
        json.add(CorrectiveActionsConstants.ACTIONS, toJson(list.getActions()));
        return json;
    }
}
