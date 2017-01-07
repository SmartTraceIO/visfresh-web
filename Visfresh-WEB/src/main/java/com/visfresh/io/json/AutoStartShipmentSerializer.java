/**
 *
 */
package com.visfresh.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.io.AutoStartShipmentDto;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentSerializer extends AbstractJsonSerializer
        implements AutoStartShipmentConstants {
    /**
     * @param tz time zone.
     */
    public AutoStartShipmentSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param e JSON element to parse.
     * @return default shipment DTO.
     */
    public AutoStartShipmentDto parseAutoStartShipmentDto(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final AutoStartShipmentDto dto = new AutoStartShipmentDto();
        dto.setId(asLong(json.get(ID)));
        dto.setPriority(asInt(json.get(PRIORITY)));

        //start locations.
        JsonArray array = json.get(START_LOCATIONS).getAsJsonArray();
        for (final JsonElement el : array) {
            dto.getStartLocations().add(el.getAsLong());
        }
        //names is readonly therefore can be absent
        if (json.has(START_LOCATION_NAMES)) {
            array = json.get(START_LOCATION_NAMES).getAsJsonArray();
            for (final JsonElement el : array) {
                dto.getStartLocationNames().add(el.getAsString());
            }
        }

        //end locations.
        array = json.get(END_LOCATIONS).getAsJsonArray();
        for (final JsonElement el : array) {
            dto.getEndLocations().add(el.getAsLong());
        }
        //names is readonly therefore can be absent
        if (json.has(END_LOCATION_NAMES)) {
            array = json.get(END_LOCATION_NAMES).getAsJsonArray();
            for (final JsonElement el : array) {
                dto.getEndLocationNames().add(el.getAsString());
            }
        }
        //start on leaving location
        if (json.has(START_ON_LEAVING_LOCATION)) {
            dto.setStartOnLeaveLocation(
                    json.get(START_ON_LEAVING_LOCATION).getAsBoolean());
        }

        //end locations.
        if (json.has(INTERIM_STOPS)) { // this check is for support older versions of UI.
            array = json.get(INTERIM_STOPS).getAsJsonArray();
            for (final JsonElement el : array) {
                dto.getInterimStops().add(el.getAsLong());
            }
            //names is readonly therefore can be absent
            if (json.has(INTERIM_STOPS_NAMES)) {
                array = json.get(INTERIM_STOPS_NAMES).getAsJsonArray();
                for (final JsonElement el : array) {
                    dto.getInterimStopsNames().add(el.getAsString());
                }
            }
        }

        dto.setAlertSuppressionMinutes(asInt(json.get(ShipmentConstants.ALERT_SUPPRESSION_MINUTES)));
        dto.setAlertProfile(asLong(json.get(ShipmentConstants.ALERT_PROFILE_ID)));
        dto.setAlertProfileName(asString(json.get(ALERT_PROFILE_NAME)));
        dto.getAlertsNotificationSchedules().addAll(toLongList(json.get(
                ShipmentConstants.ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        dto.setArrivalNotificationWithinKm(asInteger(json.get(
                ShipmentConstants.ARRIVAL_NOTIFICATION_WITHIN_KM)));
        dto.getArrivalNotificationSchedules().addAll(toLongList(
                json.get(ShipmentConstants.ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        dto.setExcludeNotificationsIfNoAlerts(asBoolean(json.get(
                ShipmentConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        dto.setShutdownDeviceAfterMinutes(asInteger(json.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_MINUTES)));
        dto.setNoAlertsAfterArrivalMinutes(asInteger(json.get(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES)));
        dto.setNoAlertsAfterStartMinutes(asInteger(json.get(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES)));
        dto.setShutDownAfterStartMinutes(asInteger(json.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES)));
        dto.setCommentsForReceiver(asString(json.get(ShipmentConstants.COMMENTS_FOR_RECEIVER)));

        dto.setName(asString(json.get(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME)));
        dto.setShipmentDescription(asString(json.get(ShipmentTemplateConstants.SHIPMENT_DESCRIPTION)));
        dto.setAddDateShipped(asBoolean(json.get(ShipmentTemplateConstants.ADD_DATE_SHIPPED)));

        if (has(json, ShipmentConstants.SEND_ARRIVAL_REPORT)) {
            dto.setSendArrivalReport(asBoolean(json.get(ShipmentConstants.SEND_ARRIVAL_REPORT)));
        }
        if (has(json, ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS)) {
            dto.setSendArrivalReportOnlyIfAlerts(asBoolean(json.get(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS)));
        }

        return dto;
    }

    /**
     * @param as
     * @return
     */
    public JsonObject toJson(final AutoStartShipmentDto as) {
        if (as == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty(PRIORITY, as.getPriority());
        json.addProperty(ID, as.getId());

        //start locations
        JsonArray ids = new JsonArray();
        json.add(START_LOCATIONS, ids);

        for (final Long id : as.getStartLocations()) {
            ids.add(new JsonPrimitive(id));
        }

        //names
        JsonArray names = new JsonArray();
        json.add(START_LOCATION_NAMES, names);

        for (final String name : as.getStartLocationNames()) {
            names.add(new JsonPrimitive(name));
        }

        //end locations
        ids = new JsonArray();
        json.add(END_LOCATIONS, ids);

        for (final Long id : as.getEndLocations()) {
            ids.add(new JsonPrimitive(id));
        }

        //names
        names = new JsonArray();
        json.add(END_LOCATION_NAMES, names);

        for (final String name : as.getEndLocationNames()) {
            names.add(new JsonPrimitive(name));
        }

        //interim stops
        ids = new JsonArray();
        json.add(INTERIM_STOPS, ids);

        for (final Long id : as.getInterimStops()) {
            ids.add(new JsonPrimitive(id));
        }

        //names
        names = new JsonArray();
        json.add(INTERIM_STOPS_NAMES, names);

        for (final String name : as.getInterimStopsNames()) {
            names.add(new JsonPrimitive(name));
        }

        //start on leaving location
        json.addProperty(START_ON_LEAVING_LOCATION, as.isStartOnLeaveLocation());

        //shipment template
        json.addProperty(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME, as.getName());
        json.addProperty(ShipmentTemplateConstants.SHIPMENT_DESCRIPTION, as.getShipmentDescription());
        json.addProperty(ShipmentTemplateConstants.ADD_DATE_SHIPPED, as.isAddDateShipped());
        json.addProperty(ShipmentTemplateConstants.ALERT_PROFILE_ID, as.getAlertProfile());
        json.addProperty(ALERT_PROFILE_NAME, as.getAlertProfileName());
        json.addProperty(ShipmentTemplateConstants.ALERT_SUPPRESSION_MINUTES, as.getAlertSuppressionMinutes());
        json.add(ShipmentTemplateConstants.ALERTS_NOTIFICATION_SCHEDULES,
                toJsonArray(as.getAlertsNotificationSchedules()));
        json.addProperty(ShipmentTemplateConstants.COMMENTS_FOR_RECEIVER, as.getCommentsForReceiver());
        json.addProperty(ShipmentTemplateConstants.ARRIVAL_NOTIFICATION_WITHIN_KM, as.getArrivalNotificationWithinKm());
        json.addProperty(ShipmentTemplateConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, as.isExcludeNotificationsIfNoAlerts());
        json.add(ShipmentTemplateConstants.ARRIVAL_NOTIFICATION_SCHEDULES,
                toJsonArray(as.getArrivalNotificationSchedules()));
        json.addProperty(ShipmentTemplateConstants.SHUTDOWN_DEVICE_AFTER_MINUTES, as.getShutdownDeviceAfterMinutes());
        json.addProperty(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES, as.getNoAlertsAfterArrivalMinutes());
        json.addProperty(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES, as.getNoAlertsAfterStartMinutes());
        json.addProperty(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES, as.getShutDownAfterStartMinutes());

        json.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, as.isSendArrivalReport());
        json.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, as.isSendArrivalReportOnlyIfAlerts());

        return json;
    }
    /**
     * @param asJsonArray
     * @return
     */
    private List<Long> toLongList(final JsonArray asJsonArray) {
        final List<Long> result = new LinkedList<Long>();
        for (final JsonElement e : asJsonArray) {
            result.add(e.getAsLong());
        }
        return result;
    }
    public static void main(final String[] args) throws IOException {
        final AutoStartShipmentSerializer ser = new AutoStartShipmentSerializer(TimeZone.getDefault());
        JsonObject json;

        final InputStream in = ShipmentTemplateSerializer.class.getResourceAsStream("req.json");
        try {
            final String str = StringUtils.getContent(in, "UTF-8");
            json = SerializerUtils.parseJson(str).getAsJsonObject();
        } finally {
            in.close();
        }

        final AutoStartShipmentDto aut = ser.parseAutoStartShipmentDto(json);
        System.out.println("Success: " + aut);
    }
}
