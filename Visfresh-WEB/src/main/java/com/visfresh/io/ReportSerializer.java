/**
 *
 */
package com.visfresh.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.drools.AlertDescriptionBuilder;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportSerializer extends AbstractJsonSerializer {
    private final AlertDescriptionBuilder alertBuilder;
    private final User user;
    /**
     * Default constructor.
     */
    public ReportSerializer(final User user) {
        super(user.getTimeZone());
        alertBuilder = new AlertDescriptionBuilder();
        this.user = user;
    }

    public JsonObject toJson(final SingleShipmentDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("status", dto.getStatus());
        obj.addProperty("currentLocation", dto.getCurrentLocation());
        obj.addProperty("deviceSN", dto.getDeviceSn());
        obj.addProperty("deviceName", dto.getDeviceName());
        obj.addProperty("tripCount", dto.getTripCount());
        obj.addProperty("shipmentDescription", dto.getShipmentDescription());
        obj.addProperty("palletId", dto.getPalletId());
        obj.addProperty("assetNum", dto.getAssetNum());
        obj.addProperty("assetType", dto.getAssetType());
        obj.addProperty("poNum", dto.getPoNum());
        obj.addProperty("shippedFrom", dto.getShippedFrom());
        obj.addProperty("shippedTo", dto.getShippedTo());
        obj.addProperty("shipmentDate", formatDate(dto.getShipmentDate()));
        obj.addProperty("estArrivalDate", formatDate(dto.getEstArrivalDate()));
        obj.addProperty("actualArrivalDate", formatDate(dto.getActualArrivalDate()));
        obj.addProperty("percentageComplete", dto.getPercentageComplete());
        obj.addProperty("alertProfileId", dto.getAlertProfileId());
        obj.addProperty("alertProfileName", dto.getAlertProfileName());
        obj.addProperty("maxTimesAlertFires", dto.getMaxTimesAlertFires());
        obj.addProperty("alertSuppressionMinutes", dto.getAlertSuppressionMinutes());
        obj.add("alertsNotificationSchedules", asJsonArray(dto.getAlertsNotificationSchedules()));
        obj.add("alertSummary", toJson(dto.getAlertSummary()));
        obj.add("arrivalNotificationSchedules", asJsonArray(dto.getArrivalNotificationSchedules()));
        obj.addProperty("arrivalNotificationWithinKm", dto.getArrivalNotificationWithInKm());
        obj.addProperty("excludeNotificationIfNoAlerts", dto.isExcludeNotificationsIfNoAlertsFired());

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

        json.addProperty("timestamp", formatDate(event.getTime()));
        json.add("location", toJson(new Location(event.getLatitude(), event.getLongitude())));
        final double t = event.getTemperature();
        json.addProperty("temperature",
                user.getTemperatureUnits() == TemperatureUnits.Fahrenheit ? t * 1.8 + 32 : t);
        json.addProperty("type", event.getType());

        //add alerts.
        final JsonArray alerts = new JsonArray();
        json.add("alerts", alerts);
        for (final Alert a : item.getAlerts()) {
            alerts.add(toJsonAlertDescription(a));
        }

        //add arrivals
        final JsonArray arrivals = new JsonArray();
        json.add("arrivas", arrivals);
        for (final Arrival a : item.getArrivals()) {
            arrivals.add(toJsonArrivalDescription(a));
        }

        return json;
    }
    /**
     * @param a arrival
     * @return
     */
    private JsonObject toJsonArrivalDescription(final Arrival a) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("numberOfMetersOfArrival", a.getNumberOfMettersOfArrival());

        final StringBuilder sb = new StringBuilder();
        obj.addProperty("arrivalReportSentTo", sb.toString());
        return obj;
    }
    /**
     * @param a
     * @return
     */
    private JsonObject toJsonAlertDescription(final Alert a) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("description", this.alertBuilder.buildDescription(a, user));
        obj.addProperty("type", a.getType().toString());
        return obj;

    }

    /**
     * @param dto
     * @return
     */
    public JsonObject toJson(final ShipmentStateDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("shipmentId", dto.getShipmentId());
        json.addProperty("status", dto.getStatus().toString());
        json.addProperty("deviceSN", dto.getDeviceSN());
        json.addProperty("deviceName", dto.getDeviceName());
        json.addProperty("tripCount", dto.getTripCount());
        json.addProperty("shipmentDescription", dto.getShipmentDescription());
        json.addProperty("palletId", dto.getPalettId());
        json.addProperty("assetNum", dto.getAssetNum());
        json.addProperty("assetType", dto.getAssetType());
        json.addProperty("shippedFrom", dto.getShippedFrom());
        json.addProperty("shipmentDate", formatDate(dto.getShipmentDate()));
        json.addProperty("shippedTo", dto.getShippedTo());
        json.addProperty("estArrivalDate", formatDate(dto.getEstArrivalDate()));
        json.addProperty("actualArrivalDate", formatDate(dto.getActualArrivalDate()));
        json.addProperty("percentageComplete", dto.getPercentageComplete());
        json.addProperty("alertProfileId", dto.getAlertProfileId());
        json.addProperty("alertProfileName", dto.getAlertProfileName());
        json.add("alertSummary", toJson(dto.getAlertSummary()));

        return json;
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
