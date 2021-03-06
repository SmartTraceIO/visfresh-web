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
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileSerializer extends AbstractJsonSerializer {
    private final Long company;
    private final TemperatureUnits tempUnits;
    private final CorrectiveActionListSerializer correctiveActionsSerializer;

    /**
     * @param tz time zone.
     */
    public AlertProfileSerializer(final Long company, final TimeZone tz, final TemperatureUnits tempUnits) {
        super(tz);
        this.tempUnits = tempUnits;
        this.company = company;
        correctiveActionsSerializer = new CorrectiveActionListSerializer(company);
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
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_ID, alert.getId());
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_NAME, alert.getName());
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_DESCRIPTION, alert.getDescription());

        obj.addProperty(AlertProfileConstants.WATCH_BATTERY_LOW,
                alert.isWatchBatteryLow());
        obj.addProperty(AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT,
                alert.isWatchEnterBrightEnvironment());
        obj.addProperty(AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT,
                alert.isWatchEnterDarkEnvironment());
        obj.addProperty(AlertProfileConstants.WATCH_MOVEMENT_START,
                alert.isWatchMovementStart());
        obj.addProperty(AlertProfileConstants.WATCH_MOVEMENT_STOP,
                alert.isWatchMovementStop());
        obj.addProperty(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getLowerTemperatureLimit(), tempUnits));
        obj.addProperty(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getUpperTemperatureLimit(), tempUnits));

        final JsonArray tempIssues = new JsonArray();
        obj.add("temperatureIssues", tempIssues);
        for (final TemperatureRule issue : alert.getAlertRules()) {
            tempIssues.add(toJson(issue));
        }
        obj.add("lightOnCorrectiveActions", correctiveActionsSerializer.toJson(
                alert.getLightOnCorrectiveActions()));
        obj.add("batteryLowCorrectiveActions", correctiveActionsSerializer.toJson(
                alert.getBatteryLowCorrectiveActions()));

        return obj;
    }
    /**
     * @param alert encoded alert profile.
     * @return decoded alert profile.
     */
    public AlertProfile parseAlertProfile(final JsonObject alert) {
        final AlertProfile p = new AlertProfile();
        p.setCompany(company);

        p.setId(asLong(alert.get(AlertProfileConstants.ALERT_PROFILE_ID)));
        p.setDescription(asString(alert.get(AlertProfileConstants.ALERT_PROFILE_DESCRIPTION)));
        p.setName(asString(alert.get(AlertProfileConstants.ALERT_PROFILE_NAME)));

        final JsonArray tempIssues = alert.get("temperatureIssues").getAsJsonArray();
        for (final JsonElement issue : tempIssues) {
            p.getAlertRules().add(parseTemperatureIssue(issue.getAsJsonObject()));
        }

        if (has(alert, AlertProfileConstants.WATCH_BATTERY_LOW)) {
            p.setWatchBatteryLow(asBoolean(alert.get(AlertProfileConstants.WATCH_BATTERY_LOW)));
        }
        if (has(alert, AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT)) {
            p.setWatchEnterBrightEnvironment(asBoolean(alert.get(
                    AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT)));
        }
        if (has(alert, AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT)) {
            p.setWatchEnterDarkEnvironment(asBoolean(alert.get(
                    AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT)));
        }
        if (has(alert, AlertProfileConstants.WATCH_MOVEMENT_START)) {
            p.setWatchMovementStart(asBoolean(alert.get(AlertProfileConstants.WATCH_MOVEMENT_START)));
        }
        if (has(alert, AlertProfileConstants.WATCH_MOVEMENT_STOP)) {
            p.setWatchMovementStop(asBoolean(alert.get(AlertProfileConstants.WATCH_MOVEMENT_STOP)));
        }
        if (has(alert, AlertProfileConstants.LOWER_TEMPERATURE_LIMIT)) {
            p.setLowerTemperatureLimit(LocalizationUtils.convertFromUnits(
                    asDouble(alert.get(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT)), tempUnits));
        }
        if (has(alert, AlertProfileConstants.UPPER_TEMPERATURE_LIMIT)) {
            p.setUpperTemperatureLimit(LocalizationUtils.convertFromUnits(
                    asDouble(alert.get(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT)), tempUnits));
        }
        if (has(alert, "lightOnCorrectiveActions")) {
            p.setLightOnCorrectiveActions(correctiveActionsSerializer.parseCorrectiveActionList(
                    alert.get("lightOnCorrectiveActions")));
        }
        if (has(alert, "batteryLowCorrectiveActions")) {
            p.setBatteryLowCorrectiveActions(correctiveActionsSerializer.parseCorrectiveActionList(
                    alert.get("batteryLowCorrectiveActions")));
        }

        return p;
    }
    /**
     * @param issue temperature issue.
     * @return
     */
    public JsonObject toJson(final TemperatureRule issue) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", issue.getId());
        obj.addProperty("type", issue.getType().toString());
        obj.addProperty("temperature", LocalizationUtils.convertToUnits(issue.getTemperature(), tempUnits));
        obj.addProperty("timeOutMinutes", issue.getTimeOutMinutes());
        obj.addProperty("cumulativeFlag", issue.isCumulativeFlag());
        obj.addProperty("maxRateMinutes", issue.getMaxRateMinutes());

        obj.add("correctiveActions", correctiveActionsSerializer.toJson(issue.getCorrectiveActions()));

        return obj;
    }
    /**
     * @param json JSON object.
     * @return temperature issue.
     */
    public TemperatureRule parseTemperatureIssue(final JsonObject json) {
        final TemperatureRule issue = new TemperatureRule();
        issue.setId(asLong(json.get("id")));
        issue.setType(AlertType.valueOf(json.get("type").getAsString()));
        issue.setTemperature(LocalizationUtils.convertFromUnits(asDouble(json.get("temperature")), tempUnits));
        issue.setTimeOutMinutes(asInt(json.get("timeOutMinutes")));
        issue.setCumulativeFlag(asBoolean(json.get("cumulativeFlag")));
        issue.setMaxRateMinutes(asInteger(json.get("maxRateMinutes")));

        issue.setCorrectiveActions(correctiveActionsSerializer.parseCorrectiveActionList(
                json.get("correctiveActions")));

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
