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
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AlertRule;
import com.visfresh.services.lists.ListAlertProfileItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public AlertProfileSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param alert alert profile.
     * @return JSON object.
     */
    public JsonObject toJson(final AlertProfile alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        //alertProfileId, alertProfileName, alertProfileDescription, highTemperature, criticalHighTemperature, lowTemperature, criticalHighTemperature, watchEnterBrightEnvironment, watchEnterDarkEnvironment, watchMovementStart
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID, alert.getId());
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME, alert.getName());
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION, alert.getDescription());

        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW,
                alert.isWatchBatteryLow());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT,
                alert.isWatchEnterBrightEnvironment());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT,
                alert.isWatchEnterDarkEnvironment());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START,
                alert.isWatchMovementStart());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP,
                alert.isWatchMovementStop());

        final JsonArray tempIssues = new JsonArray();
        obj.add("temperatureIssues", tempIssues);
        for (final AlertRule issue : alert.getAlertRules()) {
            tempIssues.add(toJson(issue));
        }

        return obj;
    }
    /**
     * @param alert encoded alert profile.
     * @return decoded alert profile.
     */
    public AlertProfile parseAlertProfile(final JsonObject alert) {
        final AlertProfile p = new AlertProfile();

        p.setId(asLong(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID)));
        p.setDescription(asString(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION)));
        p.setName(asString(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME)));

        final JsonArray tempIssues = alert.get("temperatureIssues").getAsJsonArray();
        for (final JsonElement issue : tempIssues) {
            p.getAlertRules().add(parseTemperatureIssue(issue.getAsJsonObject()));
        }

        p.setWatchBatteryLow(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW)));
        p.setWatchEnterBrightEnvironment(asBoolean(alert.get(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT)));
        p.setWatchEnterDarkEnvironment(asBoolean(alert.get(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT)));
        p.setWatchMovementStart(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START)));
        p.setWatchMovementStop(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP)));

        return p;
    }
    /**
     * @param issue temperature issue.
     * @return
     */
    public JsonObject toJson(final AlertRule issue) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", issue.getId());
        obj.addProperty("type", issue.getType().toString());
        obj.addProperty("temperature", issue.getTemperature());
        obj.addProperty("timeOutMinutes", issue.getTimeOutMinutes());
        obj.addProperty("cumulativeFlag", issue.isCumulativeFlag());
        return obj;
    }
    /**
     * @param json JSON object.
     * @return temperature issue.
     */
    public AlertRule parseTemperatureIssue(final JsonObject json) {
        final AlertRule issue = new AlertRule();
        issue.setId(asLong(json.get("id")));
        issue.setType(AlertType.valueOf(json.get("type").getAsString()));
        issue.setTemperature(asDouble(json.get("temperature")));
        issue.setTimeOutMinutes(asInt(json.get("timeOutMinutes")));
        issue.setCumulativeFlag(asBoolean(json.get("cumulativeFlag")));
        return issue;
    }
    public JsonObject toJson(final ListAlertProfileItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("alertProfileId", item.getAlertProfileId());
        json.addProperty("alertProfileName", item.getAlertProfileName());
        json.addProperty("alertProfileDescription", item.getAlertProfileDescription());
        json.add("alertRuleList", asJsonArray(item.getAlertRuleList()));
        return json;
    }
    public ListAlertProfileItem parseListAlertProfileItem(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final ListAlertProfileItem item = new ListAlertProfileItem();
        item.setAlertProfileId(asLong(json.get("alertProfileId")));
        item.setAlertProfileName(asString(json.get("alertProfileName")));
        item.setAlertProfileDescription(asString(json.get("alertProfileDescription")));
        item.getAlertRuleList().addAll(asStringList(json.get("alertRuleList").getAsJsonArray()));

        return item;
    }
    /**
     * @param array
     * @return
     */
    private List<String> asStringList(final JsonArray array) {
        final List<String> list = new LinkedList<String>();
        for (final JsonElement e : array) {
            list.add(e.getAsString());
        }
        return list;
    }
    /**
     * @param list string list.
     * @return JSON string array.
     */
    private JsonArray asJsonArray(final List<String> list) {
        final JsonArray array = new JsonArray();
        for (final String str : list) {
            array.add(new JsonPrimitive(str));
        }
        return array;
    }
}
