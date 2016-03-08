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
import com.visfresh.controllers.ShipmentTemplateConstants;
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

        //end locations.
        array = json.get(END_LOCATIONS).getAsJsonArray();
        for (final JsonElement el : array) {
            dto.getEndLocations().add(el.getAsLong());
        }

        dto.setAlertSuppressionMinutes(asInt(json.get(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES)));
        dto.setAlertProfile(asLong(json.get(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID)));
        dto.getAlertsNotificationSchedules().addAll(toLongList(json.get(
                ShipmentConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        dto.setArrivalNotificationWithinKm(asInteger(json.get(
                ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM)));
        dto.getArrivalNotificationSchedules().addAll(toLongList(
                json.get(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        dto.setExcludeNotificationsIfNoAlerts(asBoolean(json.get(
                ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        dto.setShutdownDeviceAfterMinutes(asInteger(json.get(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES)));
        dto.setNoAlertsAfterArrivalMinutes(asInteger(json.get(ShipmentConstants.PROPERTY_NO_ALERTS_AFTER_ARRIVAL_MINUTES)));
        dto.setShutDownAfterStartMinutes(asInteger(json.get(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_START_MINUTES)));
        dto.setCommentsForReceiver(asString(json.get(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER)));

        dto.setName(asString(json.get(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME)));
        dto.setShipmentDescription(asString(json.get(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION)));
        dto.setAddDateShipped(asBoolean(json.get(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED)));

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
        JsonArray array = new JsonArray();
        json.add(START_LOCATIONS, array);

        for (final Long id : as.getStartLocations()) {
            array.add(new JsonPrimitive(id));
        }

        //end locations
        array = new JsonArray();
        json.add(END_LOCATIONS, array);

        for (final Long id : as.getEndLocations()) {
            array.add(new JsonPrimitive(id));
        }

        //shipment template
        json.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME, as.getName());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION, as.getShipmentDescription());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED, as.isAddDateShipped());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_ALERT_PROFILE_ID, as.getAlertProfile());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES, as.getAlertSuppressionMinutes());
        json.add(ShipmentTemplateConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES,
                toJsonArray(as.getAlertsNotificationSchedules()));
        json.addProperty(ShipmentTemplateConstants.PROPERTY_COMMENTS_FOR_RECEIVER, as.getCommentsForReceiver());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM, as.getArrivalNotificationWithinKm());
        json.addProperty(ShipmentTemplateConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, as.isExcludeNotificationsIfNoAlerts());
        json.add(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES,
                toJsonArray(as.getArrivalNotificationSchedules()));
        json.addProperty(ShipmentTemplateConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES, as.getShutdownDeviceAfterMinutes());
        json.addProperty(ShipmentConstants.PROPERTY_NO_ALERTS_AFTER_ARRIVAL_MINUTES, as.getNoAlertsAfterArrivalMinutes());
        json.addProperty(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_START_MINUTES, as.getShutDownAfterStartMinutes());
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
