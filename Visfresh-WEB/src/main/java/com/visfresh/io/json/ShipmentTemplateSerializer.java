/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.lists.ListShipmentTemplateItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateSerializer extends AbstractJsonSerializer {
    private ReferenceResolver referenceResolver;

    /**
     * @param tz time zone.
     */
    public ShipmentTemplateSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param tpl shipment template.
     * @return JSON object.
     */
    public JsonElement toJson(final ShipmentTemplate tpl) {
        if (tpl == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_ID, tpl.getId());
        obj.addProperty(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME, tpl.getName());
        obj.addProperty(ShipmentTemplateConstants.SHIPMENT_DESCRIPTION, tpl.getShipmentDescription());
        obj.addProperty(ShipmentTemplateConstants.ADD_DATE_SHIPPED, tpl.isAddDateShipped());
        obj.addProperty(ShipmentTemplateConstants.SHIPPED_FROM, getId(tpl.getShippedFrom()));
        obj.addProperty(ShipmentTemplateConstants.SHIPPED_TO, getId(tpl.getShippedTo()));
        obj.addProperty(ShipmentTemplateConstants.DETECT_LOCATION_FOR_SHIPPED_FROM, tpl.isDetectLocationForShippedFrom());
        obj.addProperty(ShipmentTemplateConstants.ALERT_PROFILE_ID, getId(tpl.getAlertProfile()));
        obj.addProperty(ShipmentTemplateConstants.ALERT_SUPPRESSION_MINUTES, tpl.getAlertSuppressionMinutes());
        obj.add(ShipmentTemplateConstants.ALERTS_NOTIFICATION_SCHEDULES, getIdList(tpl.getAlertsNotificationSchedules()));
        obj.addProperty(ShipmentTemplateConstants.COMMENTS_FOR_RECEIVER, tpl.getCommentsForReceiver());
        obj.addProperty(ShipmentTemplateConstants.ARRIVAL_NOTIFICATION_WITHIN_KM, tpl.getArrivalNotificationWithinKm());
        obj.addProperty(ShipmentTemplateConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, tpl.isExcludeNotificationsIfNoAlerts());
        obj.add(ShipmentTemplateConstants.ARRIVAL_NOTIFICATION_SCHEDULES, getIdList(tpl.getArrivalNotificationSchedules()));
        obj.addProperty(ShipmentTemplateConstants.SHUTDOWN_DEVICE_AFTER_MINUTES, tpl.getShutdownDeviceAfterMinutes());
        obj.addProperty(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES, tpl.getNoAlertsAfterArrivalMinutes());
        obj.addProperty(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES, tpl.getNoAlertsAfterStartMinutes());
        obj.addProperty(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES, tpl.getShutDownAfterStartMinutes());

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return shipment template.
     */
    public ShipmentTemplate parseShipmentTemplate(final JsonObject obj) {
        final ShipmentTemplate shp = new ShipmentTemplate();

        shp.setAlertSuppressionMinutes(asInt(obj.get(ShipmentConstants.ALERT_SUPPRESSION_MINUTES)));
        shp.setAlertProfile(getReferenceResolver().getAlertProfile(asLong(obj.get(ShipmentConstants.ALERT_PROFILE_ID))));
        shp.getAlertsNotificationSchedules().addAll(resolveNotificationSchedules(obj.get(
                ShipmentConstants.ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setArrivalNotificationWithinKm(asInteger(obj.get(
                ShipmentConstants.ARRIVAL_NOTIFICATION_WITHIN_KM)));
        shp.getArrivalNotificationSchedules().addAll(resolveNotificationSchedules(
                obj.get(ShipmentConstants.ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setExcludeNotificationsIfNoAlerts(asBoolean(obj.get(
                ShipmentConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        shp.setShippedFrom(resolveLocationProfile(asLong(obj.get(ShipmentConstants.SHIPPED_FROM))));
        shp.setShippedTo(resolveLocationProfile(asLong(obj.get(ShipmentConstants.SHIPPED_TO))));
        shp.setShutdownDeviceAfterMinutes(asInteger(obj.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_MINUTES)));
        shp.setNoAlertsAfterArrivalMinutes(asInteger(obj.get(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES)));
        shp.setNoAlertsAfterStartMinutes(asInteger(obj.get(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES)));
        shp.setShutDownAfterStartMinutes(asInteger(obj.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES)));
        shp.setCommentsForReceiver(asString(obj.get(ShipmentConstants.COMMENTS_FOR_RECEIVER)));

        shp.setId(asLong(obj.get(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_ID)));
        shp.setName(asString(obj.get(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME)));
        shp.setShipmentDescription(asString(obj.get(ShipmentTemplateConstants.SHIPMENT_DESCRIPTION)));
        shp.setAddDateShipped(asBoolean(obj.get(ShipmentTemplateConstants.ADD_DATE_SHIPPED)));
        shp.setDetectLocationForShippedFrom(asBoolean(obj.get(ShipmentTemplateConstants.DETECT_LOCATION_FOR_SHIPPED_FROM)));

        return shp;
    }
    /**
     * @param item
     * @return
     */
    public JsonObject toJson(final ListShipmentTemplateItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_ID, item.getShipmentTemplateId());

        json.addProperty(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME, item.getShipmentTemplateName());
        json.addProperty(ShipmentTemplateConstants.SHIPMENT_DESCRIPTION, item.getShipmentDescription());

        json.addProperty(ShipmentTemplateConstants.SHIPPED_FROM, item.getShippedFrom());
        json.addProperty(ShipmentConstants.SHIPPED_FROM_LOCATION_NAME, item.getShippedFromLocationName());

        json.addProperty(ShipmentConstants.SHIPPED_TO, item.getShippedTo());
        json.addProperty(ShipmentConstants.SHIPPED_TO_LOCATION_NAME, item.getShippedToLocationName());

        json.addProperty(ShipmentConstants.ALERT_PROFILE, item.getAlertProfile());
        json.addProperty(ShipmentConstants.ALERT_PROFILE_NAME, item.getAlertProfileName());
        return json;
    }
    public ListShipmentTemplateItem parseListShipmentTemplateItem(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final ListShipmentTemplateItem item = new ListShipmentTemplateItem();
        item.setShipmentTemplateId(asLong(json.get(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_ID)));

        item.setShipmentTemplateName(asString(json.get(ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME)));
        item.setShipmentDescription(asString(json.get(ShipmentConstants.SHIPMENT_DESCRIPTION)));

        item.setShippedFrom(asLong(json.get(ShipmentConstants.SHIPPED_FROM)));
        item.setShippedFromLocationName(asString(json.get(ShipmentConstants.SHIPPED_FROM_LOCATION_NAME)));

        item.setShippedTo(asLong(json.get(ShipmentConstants.SHIPPED_TO)));
        item.setShippedToLocationName(asString(json.get(ShipmentConstants.SHIPPED_TO_LOCATION_NAME)));

        item.setAlertProfile(asLong(json.get(ShipmentConstants.ALERT_PROFILE)));
        item.setAlertProfileName(asString(json.get(ShipmentConstants.ALERT_PROFILE_NAME)));

        return item;
    }
    /**
     * @param id
     * @return
     */
    private LocationProfile resolveLocationProfile(final Long id) {
        return getReferenceResolver().getLocationProfile(id);
    }
    /**
     * @param array
     * @return
     */
    private List<NotificationSchedule> resolveNotificationSchedules(final JsonArray array) {
        final List<NotificationSchedule> list = new LinkedList<NotificationSchedule>();
        for (final JsonElement e : array) {
            list.add(getReferenceResolver().getNotificationSchedule(e.getAsLong()));
        }
        return list;
    }
    /**
     * @return the referenceResolver
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }
    /**
     * @param referenceResolver the referenceResolver to set
     */
    public void setReferenceResolver(final ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
