/**
 *
 */
package com.visfresh.io.json;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.UserResolver;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentDtoNew;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.io.shipment.SingleShipmentTimeItem;
import com.visfresh.rules.AlertDescriptionBuilder;
import com.visfresh.services.lists.ListNotificationScheduleItem;
import com.visfresh.services.lists.ListShipmentItem;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializer extends AbstractJsonSerializer {
    private ReferenceResolver referenceResolver;
    private NotificationScheduleSerializer notificationScheduleSerializer;
    private final AlertDescriptionBuilder alertBuilder;
    private final User user;

    /**
     * @param user user.
     */
    public ShipmentSerializer(final User user) {
        super(user.getTimeZone());
        notificationScheduleSerializer = new NotificationScheduleSerializer(user.getTimeZone());
        this.user = user;
        alertBuilder = new AlertDescriptionBuilder();
    }

    /**
     * @param obj
     * @param shp
     */
    private void parseShipmentBase(final JsonObject obj, final ShipmentBase shp) {
        shp.setAlertSuppressionMinutes(asInt(obj.get(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES)));
        shp.setAlertProfile(getReferenceResolver().getAlertProfile(asLong(obj.get(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID))));
        shp.getAlertsNotificationSchedules().addAll(resolveNotificationSchedules(obj.get(
                ShipmentConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setArrivalNotificationWithinKm(asInteger(obj.get(
                ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM)));
        shp.getArrivalNotificationSchedules().addAll(resolveNotificationSchedules(
                obj.get(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setExcludeNotificationsIfNoAlerts(asBoolean(obj.get(
                ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        shp.setShippedFrom(resolveLocationProfile(asLong(obj.get(ShipmentConstants.PROPERTY_SHIPPED_FROM))));
        shp.setShippedTo(resolveLocationProfile(asLong(obj.get(ShipmentConstants.PROPERTY_SHIPPED_TO))));
        shp.setShutdownDeviceTimeOut(asInteger(obj.get(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES)));
        shp.setCommentsForReceiver(asString(obj.get(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER)));
    }
    public Shipment parseShipment(final JsonObject json) {
        final Shipment s = new Shipment();
        parseShipmentBase(json, s);

        s.setAssetType(asString(json.get(ShipmentConstants.PROPERTY_ASSET_TYPE)));
        s.setId(asLong(json.get(ShipmentConstants.PROPERTY_SHIPMENT_ID)));
        s.setShipmentDescription(asString(json.get(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION)));
        s.setPalletId(asString(json.get(ShipmentConstants.PROPERTY_PALLET_ID)));
        s.setAssetNum(asString(json.get(ShipmentConstants.PROPERTY_ASSET_NUM)));
        s.setTripCount(asInt(json.get(ShipmentConstants.PROPERTY_TRIP_COUNT)));
        s.setPoNum(asInt(json.get(ShipmentConstants.PROPERTY_PO_NUM)));
        s.setShipmentDate(asDate(json.get(ShipmentConstants.PROPERTY_SHIPMENT_DATE)));
        s.getCustomFields().putAll(parseStringMap(json.get(ShipmentConstants.PROPERTY_CUSTOM_FIELDS)));
        s.setStatus(ShipmentStatus.valueOf(json.get(ShipmentConstants.PROPERTY_STATUS).getAsString()));
        s.setDevice(getReferenceResolver().getDevice(asString(json.get(ShipmentConstants.PROPERTY_DEVICE_IMEI))));

        return s;
    }
    /**
     * @param s shipment.
     * @return shipment serialized to JSON format.
     */
    public JsonElement toJson(final Shipment s) {
        if (s == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(ShipmentConstants.PROPERTY_STATUS, s.getStatus().name());

        if (s.getDevice() != null) {
            obj.addProperty(ShipmentConstants.PROPERTY_DEVICE_IMEI, s.getDevice().getImei());
            obj.addProperty("deviceSN", s.getDevice().getSn());
            obj.addProperty("deviceName", s.getDevice().getName());
        }
        obj.addProperty(ShipmentConstants.PROPERTY_TRIP_COUNT, s.getTripCount());

        obj.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_ID, s.getId());
        obj.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION, s.getShipmentDescription());

        obj.addProperty(ShipmentConstants.PROPERTY_PALLET_ID, s.getPalletId());
        obj.addProperty(ShipmentConstants.PROPERTY_PO_NUM, s.getPoNum());
        obj.addProperty(ShipmentConstants.PROPERTY_ASSET_NUM, s.getAssetNum());
        obj.addProperty(ShipmentConstants.PROPERTY_ASSET_TYPE, s.getAssetType());

        obj.addProperty(ShipmentConstants.PROPERTY_SHIPPED_FROM, getId(s.getShippedFrom()));
        obj.addProperty(ShipmentConstants.PROPERTY_SHIPPED_TO, getId(s.getShippedTo()));
        obj.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_DATE, formatDate(s.getShipmentDate()));

        obj.addProperty(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID, getId(s.getAlertProfile()));
        obj.addProperty(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES, s.getAlertSuppressionMinutes());
        obj.add(ShipmentConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES, getIdList(s.getAlertsNotificationSchedules()));

        obj.addProperty(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER, s.getCommentsForReceiver());
        obj.addProperty(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM, s.getArrivalNotificationWithinKm());
        obj.addProperty(ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, s.isExcludeNotificationsIfNoAlerts());
        obj.add(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES, getIdList(s.getArrivalNotificationSchedules()));

        obj.addProperty(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES, s.getShutdownDeviceTimeOut());

        obj.add(ShipmentConstants.PROPERTY_CUSTOM_FIELDS, toJson(s.getCustomFields()));
        return obj;
    }
    /**
     * @param json JSON object.
     * @return save shipment request.
     */
    public SaveShipmentRequest parseSaveShipmentRequest(final JsonObject json) {
        final SaveShipmentRequest req = new SaveShipmentRequest();
        req.setSaveAsNewTemplate(asBoolean(json.get("saveAsNewTemplate")));
        req.setTemplateName(asString(json.get("templateName")));
        req.setShipment(parseShipment(json.get("shipment").getAsJsonObject()));
        return req;
    }
    public JsonObject toJson(final SaveShipmentRequest req) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("saveAsNewTemplate", req.isSaveAsNewTemplate());
        obj.addProperty("templateName", req.getTemplateName());
        obj.add("shipment", toJson(req.getShipment()));
        obj.remove("deviceSN");
        obj.remove("deviceName");
        return obj;
    }

    public SaveShipmentResponse parseSaveShipmentResponse(final JsonObject json) {
        final SaveShipmentResponse resp = new SaveShipmentResponse();
        resp.setShipmentId(asLong(json.get(ShipmentConstants.PROPERTY_SHIPMENT_ID)));
        resp.setTemplateId(asLong(json.get("templateId")));
        return resp;
    }
    /**
     * @param resp save shipment response.
     * @return JSON object.
     */
    public JsonObject toJson(final SaveShipmentResponse resp) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_ID, resp.getShipmentId());
        obj.addProperty("templateId", resp.getTemplateId());
        return obj;
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

    public JsonObject toJson(final SingleShipmentDto dto) {
        if (dto == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty("shipmentId", dto.getShipmentId());
        obj.addProperty("deviceSN", dto.getDeviceSn());
        obj.addProperty("deviceName", dto.getDeviceName());
        obj.addProperty("tripCount", dto.getTripCount());

        obj.addProperty("shipmentDescription", dto.getShipmentDescription());

        obj.addProperty("palletId", dto.getPalletId());
        obj.addProperty("poNum", dto.getPoNum());
        obj.addProperty("assetNum", dto.getAssetNum());
        obj.addProperty("assetType", dto.getAssetType());

        obj.addProperty("status", dto.getStatus());

        obj.addProperty("shippedFrom", dto.getShippedFrom());
        obj.addProperty("shippedTo", dto.getShippedTo());
        obj.addProperty("shipmentDate", formatDate(dto.getShipmentDate()));
        obj.addProperty("currentLocation", dto.getCurrentLocation());
        obj.addProperty("estArrivalDate", formatDate(dto.getEstArrivalDate()));
        obj.addProperty("actualArrivalDate", formatDate(dto.getActualArrivalDate()));
        obj.addProperty("percentageComplete", dto.getPercentageComplete());

        obj.addProperty("alertProfileId", dto.getAlertProfileId());
        obj.addProperty("alertProfileName", dto.getAlertProfileName());
        obj.addProperty("alertSuppressionMinutes", dto.getAlertSuppressionMinutes());

        obj.addProperty("maxTimesAlertFires", dto.getMaxTimesAlertFires());
        obj.add("alertsNotificationSchedules", scheduleItemsAsJsonArray(dto.getAlertsNotificationSchedules()));
        obj.add("alertSummary", toJson(dto.getAlertSummary()));

        obj.addProperty("arrivalNotificationWithinKm", dto.getArrivalNotificationWithInKm());
        obj.addProperty("excludeNotificationIfNoAlerts", dto.isExcludeNotificationsIfNoAlertsFired());
        obj.add("arrivalNotificationSchedules", scheduleItemsAsJsonArray(dto.getArrivalNotificationSchedules()));
        obj.addProperty("commentsForReceiver", dto.getCommentsForReceiver());

        //serialize time items
        final JsonArray items = new JsonArray();
        obj.add("items", items);
        for (final SingleShipmentTimeItem item : dto.getItems()) {
            items.add(toJson(item));
        }

        return obj;
    }

    /**
     * @param sched
     * @return
     */
    private String createPeopleToNotifyString(final List<ListNotificationScheduleItem> sched) {
        final List<String> list = new LinkedList<>();
        for (final ListNotificationScheduleItem s : sched) {
            list.add(s.getPeopleToNotify());
        }
        return StringUtils.combine(list, ", ");
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
        json.addProperty("temperature", convertTemperature(event.getTemperature()));
        json.addProperty("type", event.getType().toString());

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
     * @param t
     * @return
     */
    protected double convertTemperature(final double t) {
        double value = user.getTemperatureUnits() == TemperatureUnits.Fahrenheit ? t * 1.8 + 32 : t;
        //cut extra decimal signs.
        value = Math.round(value * 100) / 100.;
        return value;
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
    public JsonObject toJson(final ListShipmentItem dto) {
        if (dto == null) {
            return null;
        }
        final JsonObject json = new JsonObject();

        json.addProperty("status", dto.getStatus().toString());

        json.addProperty("deviceSN", dto.getDeviceSN());
        json.addProperty("deviceName", dto.getDeviceName());
        json.addProperty("tripCount", dto.getTripCount());

        json.addProperty("shipmentId", dto.getShipmentId());
        json.addProperty("shipmentDescription", dto.getShipmentDescription());
        json.addProperty("shipmentDate", formatDate(dto.getShipmentDate()));

        json.addProperty("palletId", dto.getPalettId());
        json.addProperty("assetNum", dto.getAssetNum());
        json.addProperty("assetType", dto.getAssetType());

        json.addProperty("shippedFrom", dto.getShippedFrom());
        json.addProperty("shippedTo", dto.getShippedTo());
        json.addProperty("estArrivalDate", formatDate(dto.getEstArrivalDate()));
        json.addProperty("actualArrivalDate", formatDate(dto.getActualArrivalDate()));
        json.addProperty("percentageComplete", dto.getPercentageComplete());

        json.addProperty("alertProfileId", dto.getAlertProfileId());
        json.addProperty("alertProfileName", dto.getAlertProfileName());
        json.add("alertSummary", toJson(dto.getAlertSummary()));
        json.addProperty("siblingCount", dto.getSiblingCount());

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
     * @param json JSON object.
     * @return filtered shipment request.
     */
    public GetFilteredShipmentsRequest parseGetFilteredShipmentsRequest(final JsonObject json) {
        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        req.setAlertsOnly(asBoolean(json.get("alertsOnly")));
        req.setDeviceImei(asString(json.get("deviceImei")));
        if (json.has("last2Days")) {
            req.setLast2Days(asBoolean(json.get("last2Days")));
        }
        if (json.has("lastDay")) {
            req.setLastDay(asBoolean(json.get("lastDay")));
        }
        if (json.has("lastMonth")) {
            req.setLastMonth(asBoolean(json.get("lastMonth")));
        }
        if (json.has("lastWeek")) {
            req.setLastWeek(asBoolean(json.get("lastWeek")));
        }
        if (json.has("shipmentDateFrom")) {
            req.setShipmentDateFrom(parseDate(asString(json.get("shipmentDateFrom"))));
        }
        if (json.has("shipmentDateTo")) {
            req.setShipmentDateTo(parseDate(asString(json.get("shipmentDateTo"))));
        }
        req.setShipmentDescription(asString(json.get("shipmentDescription")));
        if (json.has("shippedFrom")) {
            req.setShippedFrom(asLongList(json.get("shippedFrom")));
        }
        if (json.has("shippedTo")) {
            req.setShippedTo(asLongList(json.get("shippedTo")));
        }
        if (json.has("status")) {
            final String statusString = asString(json.get("status"));
            req.setStatus(statusString == null ? null : ShipmentStatus.valueOf(statusString));
        }
        if (json.has("pageIndex")) {
            req.setPageIndex(asInt(json.get("pageIndex")));
        }
        if (json.has("pageSize")) {
            req.setPageSize(asInt(json.get("pageSize")));
        }
        if (json.has("sortOrder")) {
            req.setSortOrder(asString(json.get("sortOrder")));
        }
        if (json.has("sortColumn")) {
            req.setSortColumn(asString(json.get("sortColumn")));
        }

        return req;
    }
    /**
     * @param r filtered shipments request.
     * @return request as JSON object.
     */
    public JsonObject toJson(final GetFilteredShipmentsRequest r) {
        if (r == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("alertsOnly", r.isAlertsOnly());
        if (r.getDeviceImei() != null) {
            obj.addProperty("deviceImei", r.getDeviceImei());
        }
        if (r.getLast2Days() != null) {
            obj.addProperty("last2Days", r.getLast2Days());
        }
        if (r.getLastDay() != null) {
            obj.addProperty("lastDay", r.getLastDay());
        }
        if (r.getLastMonth() != null) {
            obj.addProperty("lastMonth", r.getLastMonth());
        }
        if (r.getLastWeek() != null) {
            obj.addProperty("lastWeek", r.getLastWeek());
        }
        if (r.getShipmentDateFrom() != null) {
            obj.addProperty("shipmentDateFrom", formatDate(r.getShipmentDateFrom()));
        }
        if (r.getShipmentDateTo() != null) {
            obj.addProperty("shipmentDateTo", formatDate(r.getShipmentDateTo()));
        }
        if (r.getShipmentDescription() != null) {
            obj.addProperty("shipmentDescription", r.getShipmentDescription());
        }
        if (r.getShippedFrom() != null && !r.getShippedFrom().isEmpty()) {
            obj.add("shippedFrom", asJsonArray(r.getShippedFrom()));
        }
        if (r.getShippedTo() != null && !r.getShippedTo().isEmpty()) {
            obj.add("shippedTo", asJsonArray(r.getShippedTo()));
        }
        if (r.getStatus() != null) {
            obj.addProperty("status", r.getStatus().toString());
        }
        if (r.getPageIndex() != null) {
            obj.addProperty("pageIndex", r.getPageIndex());
        }
        if (r.getPageSize() != null) {
            obj.addProperty("pageSize", r.getPageSize());
        }
        if (r.getSortOrder() != null) {
            obj.addProperty("sortOrder", r.getSortOrder());
        }
        if (r.getSortColumn() != null) {
            obj.addProperty("sortColumn", r.getSortColumn());
        }

        return obj;
    }
    /**
     * @param lsit
     * @return
     */
    private JsonArray asJsonArray(final List<Long> lsit) {
        final JsonArray array = new JsonArray();
        for (final Long item : lsit) {
            array.add(new JsonPrimitive(item));
        }
        return array;
    }

    /**
     * @param e JSON elmeent.
     * @return
     */
    private List<Long> asLongList(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonArray array = e.getAsJsonArray();
        final List<Long> list = new LinkedList<Long>();
        for (final JsonElement id : array) {
            list.add(id.getAsLong());
        }
        return list;
    }

    /**
     * @param items
     * @return
     */
    private JsonArray scheduleItemsAsJsonArray(final List<ListNotificationScheduleItem> items) {
        final JsonArray array = new JsonArray();
        if (items != null) {
            for (final ListNotificationScheduleItem i : items) {
                array.add(notificationScheduleSerializer.toJson(i));
            }
        }
        return array;
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
    public void setUserResolver(final UserResolver r) {
        notificationScheduleSerializer.setUserResolver(r);
    }
    /**
     * @param dto
     * @return
     */
    public JsonObject toJson(final SingleShipmentDtoNew dto) {
        return toJson(dto, true);
    }

    /**
     * @param dto
     * @param isNotSibling
     * @return
     */
    protected JsonObject toJson(final SingleShipmentDtoNew dto,
            final boolean isNotSibling) {
        if (dto == null) {
            return  null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("shipmentId", dto.getShipmentId()); /*+*/
        json.addProperty("deviceSN", dto.getDeviceSN()); /*+*/
        if (isNotSibling) {
            json.addProperty("deviceName", dto.getDeviceName());
        }
        json.addProperty("tripCount", dto.getTripCount()); /*+*/
        json.addProperty("siblingColor", dto.getSiblingColor());

        if (isNotSibling) {
            json.addProperty("shipmentDescription", dto.getShipmentDescription());
            json.addProperty("palletId", dto.getPalletId());
            json.addProperty("assetNum", dto.getAssetNum());
            json.addProperty("assetType", dto.getAssetType());
            json.addProperty("status", dto.getStatus().name());
        }
        json.addProperty("trackerPositionFrontPercent", dto.getTrackerPositionFrontPercent()); /*+*/
        json.addProperty("trackerPositionLeftPercent", dto.getTrackerPositionLeftPercent()); /*+*/

        if (isNotSibling) {
            json.addProperty("alertProfileId", dto.getAlertProfileId());
            json.addProperty("alertProfileName", dto.getAlertProfileName());
            json.addProperty("alertSuppressionMinutes", dto.getAlertSuppressionMinutes());

            json.addProperty("alertPeopleToNotify", createPeopleToNotifyString(
                    dto.getAlertsNotificationSchedules()));

            //alertsNotificationSchedules
            final JsonArray array = new JsonArray();
            for (final ListNotificationScheduleItem item: dto.getAlertsNotificationSchedules()) {
                array.add(toJson(item));
            }
            json.add("alertsNotificationSchedules", array);
        }

        //alert summary
        json.add("alertSummary", createAlertSummaryArray(dto.getAlertSummary())); /*+*/
        if (isNotSibling) {
            json.addProperty("alertYetToFire", dto.getAlertYetToFire());

            //"arrivalNotificationTimeStr": "9:00am 12 AUG 2014",
            // NEW - the actual time arrival notification was sent out
            json.addProperty("arrivalNotificationTimeStr", dto.getArrivalNotificationTimeStr());

            //"arrivalNotificationTimeISO": "2014-08-12 12:10",
            // NEW - ISO for actual time arrival notification sent out
            json.addProperty("arrivalNotificationTimeISO", dto.getArrivalNotificationTimeIso());

            json.addProperty("arrivalNotificationWithinKm", dto.getArrivalNotificationWithinKm());
            json.addProperty("excludeNotificationsIfNoAlerts", dto.isExcludeNotificationsIfNoAlerts());

            json.addProperty("arrivalPeopleToNotify", createPeopleToNotifyString(
                    dto.getArrivalNotificationSchedules()));

            final JsonArray array = new JsonArray();
            for (final ListNotificationScheduleItem item: dto.getArrivalNotificationSchedules()) {
                array.add(toJson(item));
            }

            json.addProperty("commentsForReceiver", dto.getCommentsForReceiver());

            json.add("arrivalNotificationSchedules", array);
            json.addProperty("shutdownDeviceAfterMinutes", dto.getShutdownDeviceAfterMinutes());

            json.addProperty("shutdownTimeStr", dto.getShutdownTimeStr());
            json.addProperty("shutdownTimeISO", dto.getShutdownTimeIso());

            json.addProperty("startLocation", dto.getStartLocation());
            json.addProperty("startTimeStr", dto.getStartTimeStr());
            json.addProperty("startTimeISO", dto.getStartTimeISO());
            json.add("startLocationForMap", toJson(dto.getStartLocationForMap()));

            json.addProperty("endLocation", dto.getEndLocation());
            json.addProperty("etaStr", dto.getEtaStr());
            json.addProperty("etaISO", dto.getEta());

            json.addProperty("arrivalTimeStr", dto.getArrivalTimeStr());
            json.addProperty("arrivalTimeISO", dto.getArrivalTimeIso());
            json.add("endLocationForMap", toJson(dto.getEndLocationForMap()));

            json.addProperty("lastReadingLocation", dto.getCurrentLocation());
            json.addProperty("lastReadingTimeStr", dto.getLastReadingTimeStr());
            json.addProperty("lastReadingTimeISO", dto.getLastReadingTimeIso());
            json.addProperty("lastReadingTemperature", convertTemperature(dto.getLastReadingTemperature()));

            json.add("lastReadingForMap", toJson(dto.getCurrentLocationForMap()));

            json.addProperty("minTemp", convertTemperature(dto.getMinTemp()));
            json.addProperty("maxTemp", convertTemperature(dto.getMaxTemp()));
            json.addProperty("firstReadingTimeISO", dto.getTimeOfFirstReading());
        }

        final JsonArray locations = new JsonArray();
        for (final SingleShipmentLocation l : dto.getLocations()) {
            locations.add(toJson(l, isNotSibling));
        }
        json.add(isNotSibling ? "locations" : "readings", locations);

        if (isNotSibling) {
            final JsonArray siblings = new JsonArray();
            for (final SingleShipmentDtoNew sibling: dto.getSiblings()) {
                siblings.add(toJson(sibling, false));
            }

            json.add("siblings", siblings);
        }

        return json;
    }
    /**
     * @param summary
     * @return
     */
    private JsonArray createAlertSummaryArray(final Set<AlertType> summary) {
        final JsonArray array = new JsonArray();
        final List<AlertType> list = new LinkedList<>(summary);
        Collections.sort(list);

        for (final AlertType t : list) {
            array.add(new JsonPrimitive(t.name()));
        }
        return array;
    }

    /**
     * @param item list notification schedule item.
     * @return
     */
    private JsonObject toJson(final ListNotificationScheduleItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("notificationScheduleId", item.getNotificationScheduleId());
        json.addProperty("notificationScheduleName", item.getNotificationScheduleName());
        return json;
    }

    /**
     * @param l location.
     * @param isNotSibling is not sibling flag.
     * @return
     */
    private JsonObject toJson(final SingleShipmentLocation l, final boolean isNotSibling) {
        if (l == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        if (isNotSibling) {
            json.addProperty("lat", l.getLatitude());
            json.addProperty("long", l.getLongitude());
        }
        json.addProperty("temperature", convertTemperature(l.getTemperature()));
        json.addProperty("timeISO", l.getTimeIso());

        final JsonArray alerts = new JsonArray();
        for (final SingleShipmentAlert alert : l.getAlerts()) {
            alerts.add(toJson(alert));
        }
        json.add("alerts", alerts);

        return json;
    }

    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final SingleShipmentAlert alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("title", alert.getTitle());

        //add lines
        int i = 1;
        for (final String line : alert.getLines()) {
            json.addProperty("Line" + i, line);
            i++;
        }
        json.addProperty("type", alert.getType());
        return json;
    }
}
