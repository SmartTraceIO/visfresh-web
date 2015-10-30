/**
 *
 */
package com.visfresh.io;

import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportSerializer extends AbstractJsonSerializer {
    /**
     * Default constructor.
     */
    public ReportSerializer(final TimeZone tz) {
        super(tz);
    }

    public JsonObject toJson(final SingleShipmentDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("alertProfile", dto.getAlertProfile());
        obj.add("alertsNotificationSchedules", asJsonArray(dto.getAlertsNotificationSchedules()));
        obj.addProperty("alertSuppressionDuringCoolDown", dto.getAlertSuppressionDuringCoolDown());
        obj.add("arrivalNotificationSchedules", asJsonArray(dto.getArrivalNotificationSchedules()));
        obj.addProperty("arrivalNotificationWithIn", dto.getArrivalNotificationWithIn());
        obj.addProperty("assetNum", dto.getAssetNum());
        obj.addProperty("assetType", dto.getAssetType());
        obj.add("customFields", toJson(dto.getCustomFields()));
        obj.addProperty("device", dto.getDevice());
        obj.addProperty("palletId", dto.getPalletId());
        obj.addProperty("poNum", dto.getPoNum());
        obj.addProperty("shipmentDate", timeToString(dto.getShipmentDate()));
        obj.addProperty("shipmentDescription", dto.getShipmentDescription());
        obj.addProperty("shippedFrom", dto.getShippedFrom());
        obj.addProperty("shippedTo", dto.getShippedTo());
        obj.addProperty("shutdownDevice", dto.getShutdownDevice());
        obj.addProperty("status", dto.getStatus());
        obj.addProperty("tripCount", dto.getTripCount());

        //serialize time items
        final JsonArray items = new JsonArray();
        obj.add("items", items);
        for (final SingleShipmentTimeItem item : dto.getItems()) {
            items.add(toJson(item));
        }

        return obj;
    }

    /**
     * @param item
     * @return
     */
    private JsonObject toJson(final SingleShipmentTimeItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        final TrackerEvent event = item.getEvent();

        json.addProperty("timestamp", event.getTime().getTime());
        json.add("location", toJson(new Location(event.getLatitude(), event.getLongitude())));
        json.addProperty("temperature", event.getTemperature());
        json.addProperty("type", event.getType());

        //add alerts.
        final JsonArray alerts = new JsonArray();
        json.add("alerts", alerts);
        for (final Alert a : item.getAlerts()) {
            alerts.add(toJson(a));
        }

        //add arrivals
        final JsonArray arrivals = new JsonArray();
        json.add("arrivas", arrivals);
        for (final Arrival a : item.getArrivals()) {
            arrivals.add(toJson(a));
        }

        return json;
    }
    /**
     * @param arrival
     * @return
     */
    private JsonElement toJson(final Arrival arrival) {
        final JsonObject obj = EntityJSonSerializer.toJson(arrival);
        removeRefs(obj);
        return obj;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final Alert alert) {
        final JsonObject obj = EntityJSonSerializer.toJson(alert);
        removeRefs(obj);
        return obj;
    }

    /**
     * @param obj
     */
    protected void removeRefs(final JsonObject obj) {
        obj.remove("id");
        obj.remove("date");
        obj.remove("device");
        obj.remove("shipment");
    }
    /**
     * @param location
     * @return
     */
    private JsonElement toJson(final Location location) {
        if (location == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("latitude", location.getLatitude());
        json.addProperty("longitude", location.getLongitude());

        return json;
    }
    /**
     * @param items
     * @return
     */
    private JsonArray asJsonArray(final long[] items) {
        final JsonArray array = new JsonArray();
        if (items != null) {
            for (final long i : items) {
                array.add(new JsonPrimitive(i));
            }
        }
        return array;
    }
}
