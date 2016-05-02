/**
 *
 */
package com.visfresh.io.json;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.InterimStopDto;
import com.visfresh.io.NoteDto;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializer extends AbstractJsonSerializer {
    private static final String INTERIM_LOCATIONS = "interimLocations";
    private static final String CREATED_BY = "createdBy";
    private static final String START_DATE = "startDate";
    private static final String JSON_SORT_COLUMN = "sc";
    private static final String JSON_SORT_ORDER = "so";
    private final User user;
    private final DateFormat isoFormat;
    private final DateFormat prettyFormat;
    private final LocationSerializer locationSerializer;
    private final NoteSerializer noteSerializer;

    /**
     * @param user user.
     */
    public ShipmentSerializer(final User user) {
        super(user.getTimeZone());
        locationSerializer = new LocationSerializer(user.getTimeZone());
        noteSerializer = new NoteSerializer(user.getTimeZone());

        this.user = user;
        this.isoFormat = createIsoFormat(user);
        this.prettyFormat = createPrettyFormat(user);
    }
    /**
     * @param obj
     * @param shp
     */
    private void parseShipmentBase(final JsonObject obj, final ShipmentBaseDto shp) {
        shp.setAlertSuppressionMinutes(asInt(obj.get(ShipmentConstants.ALERT_SUPPRESSION_MINUTES)));
        shp.setAlertProfile(asLong(obj.get(ShipmentConstants.ALERT_PROFILE_ID)));
        shp.getAlertsNotificationSchedules().addAll(asLongList(obj.get(
                ShipmentConstants.ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setArrivalNotificationWithinKm(asInteger(obj.get(
                ShipmentConstants.ARRIVAL_NOTIFICATION_WITHIN_KM)));
        shp.getArrivalNotificationSchedules().addAll(asLongList(
                obj.get(ShipmentConstants.ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setExcludeNotificationsIfNoAlerts(asBoolean(obj.get(
                ShipmentConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        shp.setShippedFrom(asLong(obj.get(ShipmentConstants.SHIPPED_FROM)));
        shp.setShippedTo(asLong(obj.get(ShipmentConstants.SHIPPED_TO)));
        shp.setShutdownDeviceAfterMinutes(asInteger(obj.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_MINUTES)));
        shp.setNoAlertsAfterArrivalMinutes(asInteger(obj.get(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES)));
        shp.setNoAlertsAfterStartMinutes(asInteger(obj.get(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES)));
        shp.setShutDownAfterStartMinutes(asInteger(obj.get(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES)));
        shp.setCommentsForReceiver(asString(obj.get(ShipmentConstants.COMMENTS_FOR_RECEIVER)));

        //TODO correct
        final JsonElement locs = obj.get(INTERIM_LOCATIONS);
        if (locs != null && !locs.isJsonNull()) {
            shp.setInterimLocations(asLongList(locs));
        }
    }
    public ShipmentDto parseShipment(final JsonObject json) {
        final ShipmentDto s = new ShipmentDto();
        parseShipmentBase(json, s);

        s.setAssetType(asString(json.get(ShipmentConstants.ASSET_TYPE)));
        s.setId(asLong(json.get(ShipmentConstants.SHIPMENT_ID)));
        s.setShipmentDescription(asString(json.get(ShipmentConstants.SHIPMENT_DESCRIPTION)));
        s.setPalletId(asString(json.get(ShipmentConstants.PALLET_ID)));
        s.setAssetNum(asString(json.get(ShipmentConstants.ASSET_NUM)));
        s.setTripCount(asInt(json.get(ShipmentConstants.TRIP_COUNT)));
        s.setPoNum(asInt(json.get(ShipmentConstants.PO_NUM)));
        s.setShipmentDate(asDate(json.get(ShipmentConstants.SHIPMENT_DATE)));
        s.setDeviceShutdownTime(parseIsoDate(json.get(ShipmentConstants.SHUTDOWN_TIME_ISO)));
        s.setArrivalDate(asDate(json.get(ShipmentConstants.ARRIVAL_DATE)));
        s.getCustomFields().putAll(SerializerUtils.parseStringMap(json.get(ShipmentConstants.CUSTOM_FIELDS)));
        s.setStatus(ShipmentStatus.valueOf(json.get(ShipmentConstants.STATUS).getAsString()));
        s.setDeviceImei(asString(json.get(ShipmentConstants.DEVICE_IMEI)));
        s.setDeviceSN(asString(json.get(ShipmentConstants.DEVICE_SN)));
        s.setDeviceName(asString(json.get(ShipmentConstants.DEVICE_NAME)));
        s.setStartDate(asDate(json.get(START_DATE)));
        s.setCreatedBy(asString(json.get(CREATED_BY)));

        return s;
    }
    /**
     * @param s shipment.
     * @return shipment serialized to JSON format.
     */
    public JsonObject toJson(final ShipmentDto s) {
        if (s == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(ShipmentConstants.STATUS, s.getStatus().name());

        obj.addProperty(ShipmentConstants.DEVICE_IMEI, s.getDeviceImei());
        obj.addProperty(ShipmentConstants.DEVICE_SN, s.getDeviceSN());
        obj.addProperty(ShipmentConstants.DEVICE_NAME, s.getDeviceName());
        obj.addProperty(ShipmentConstants.TRIP_COUNT, s.getTripCount());

        obj.addProperty(ShipmentConstants.SHIPMENT_ID, s.getId());
        obj.addProperty(ShipmentConstants.SHIPMENT_DESCRIPTION, s.getShipmentDescription());

        obj.addProperty(ShipmentConstants.PALLET_ID, s.getPalletId());
        obj.addProperty(ShipmentConstants.PO_NUM, s.getPoNum());
        obj.addProperty(ShipmentConstants.ASSET_NUM, s.getAssetNum());
        obj.addProperty(ShipmentConstants.ASSET_TYPE, s.getAssetType());

        obj.addProperty(ShipmentConstants.SHIPPED_FROM, s.getShippedFrom());
        obj.addProperty(ShipmentConstants.SHIPPED_TO, s.getShippedTo());
        obj.addProperty(ShipmentConstants.SHIPMENT_DATE, formatDate(s.getShipmentDate()));
        obj.addProperty(ShipmentConstants.ARRIVAL_DATE, formatDate(s.getArrivalDate()));
        obj.addProperty(ShipmentConstants.SHUTDOWN_TIME_ISO,
                s.getDeviceShutdownTime() == null ? null : isoFormat.format(s.getDeviceShutdownTime()));
        obj.addProperty(ShipmentConstants.DEVICE_SHUTDOWN_TIME,
                s.getDeviceShutdownTime() == null ? null : prettyFormat.format(s.getDeviceShutdownTime()));

        obj.addProperty(ShipmentConstants.ALERT_PROFILE_ID, s.getAlertProfile());
        obj.addProperty(ShipmentConstants.ALERT_SUPPRESSION_MINUTES, s.getAlertSuppressionMinutes());
        obj.add(ShipmentConstants.ALERTS_NOTIFICATION_SCHEDULES, toJsonArray(s.getAlertsNotificationSchedules()));

        obj.addProperty(ShipmentConstants.COMMENTS_FOR_RECEIVER, s.getCommentsForReceiver());
        obj.addProperty(ShipmentConstants.ARRIVAL_NOTIFICATION_WITHIN_KM, s.getArrivalNotificationWithinKm());
        obj.addProperty(ShipmentConstants.EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, s.isExcludeNotificationsIfNoAlerts());
        obj.add(ShipmentConstants.ARRIVAL_NOTIFICATION_SCHEDULES, toJsonArray(s.getArrivalNotificationSchedules()));

        obj.addProperty(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_MINUTES, s.getShutdownDeviceAfterMinutes());
        obj.addProperty(ShipmentConstants.NO_ALERTS_AFTER_ARRIVAL_MINUTES, s.getNoAlertsAfterArrivalMinutes());
        obj.addProperty(ShipmentConstants.NO_ALERTS_AFTER_START_MINUTES, s.getNoAlertsAfterStartMinutes());
        obj.addProperty(ShipmentConstants.SHUTDOWN_DEVICE_AFTER_START_MINUTES, s.getShutDownAfterStartMinutes());

        obj.add(ShipmentConstants.CUSTOM_FIELDS, SerializerUtils.toJson(s.getCustomFields()));
        obj.addProperty(START_DATE, formatDate(s.getStartDate()));
        obj.addProperty(CREATED_BY, s.getCreatedBy());

        if (s.getInterimLocations() != null) {
            final JsonArray array = new JsonArray();
            obj.add(INTERIM_LOCATIONS, array);

            for (final Long l : s.getInterimLocations()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(INTERIM_LOCATIONS, JsonNull.INSTANCE);
        }
        return obj;
    }
    /**
     * @param json JSON object.
     * @return save shipment request.
     */
    public SaveShipmentRequest parseSaveShipmentRequest(final JsonObject json) {
        final SaveShipmentRequest req = new SaveShipmentRequest();
        req.setSaveAsNewTemplate(asBoolean(json.get("saveAsNewTemplate")));
        req.setIncludePreviousData(asBoolean(json.get("includePreviousData")));
        req.setTemplateName(asString(json.get("templateName")));
        req.setShipment(parseShipment(getShipmentFromRequest(json)));
        return req;
    }
    public JsonObject toJson(final SaveShipmentRequest req) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("saveAsNewTemplate", req.isSaveAsNewTemplate());
        obj.addProperty("includePreviousData", req.isIncludePreviousData());
        obj.addProperty("templateName", req.getTemplateName());
        obj.add("shipment", toJson(req.getShipment()));
        obj.remove(ShipmentConstants.DEVICE_SN);
        obj.remove(ShipmentConstants.DEVICE_NAME);
        return obj;
    }
    /**
     * @param json
     * @return
     */
    public Long getShipmentIdFromSaveRequest(final JsonObject json) {
        final JsonObject shipment = getShipmentFromRequest(json);
        return asLong(shipment.get(ShipmentConstants.SHIPMENT_ID));
    }
    /**
     * @param json JSON save shipment request.
     * @return
     */
    public JsonObject getShipmentFromRequest(final JsonObject json) {
        return json.get("shipment").getAsJsonObject();
    }
    /**
     * @param req
     * @param shipment
     */
    public void setShipmentToRequest(final JsonObject req, final JsonObject shipment) {
        req.add("shipment", shipment);
    }

    public SaveShipmentResponse parseSaveShipmentResponse(final JsonObject json) {
        final SaveShipmentResponse resp = new SaveShipmentResponse();
        resp.setShipmentId(asLong(json.get(ShipmentConstants.SHIPMENT_ID)));
        resp.setTemplateId(asLong(json.get("templateId")));
        return resp;
    }
    /**
     * @param resp save shipment response.
     * @return JSON object.
     */
    public JsonObject toJson(final SaveShipmentResponse resp) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(ShipmentConstants.SHIPMENT_ID, resp.getShipmentId());
        obj.addProperty("templateId", resp.getTemplateId());
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
     * @param el
     * @return
     */
    private Date parseIsoDate(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        try {
            return isoFormat.parse(el.getAsString());
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
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

        json.addProperty(ShipmentConstants.DEVICE_SN, dto.getDeviceSN());
        json.addProperty(ShipmentConstants.DEVICE_NAME, dto.getDeviceName());
        json.addProperty("tripCount", dto.getTripCount());

        json.addProperty("shipmentId", dto.getShipmentId());
        json.addProperty("shipmentDescription", dto.getShipmentDescription());
        json.addProperty("shipmentDate", dto.getShipmentDate());

        json.addProperty("palletId", dto.getPalettId());
        json.addProperty("assetNum", dto.getAssetNum());
        json.addProperty("assetType", dto.getAssetType());

        json.addProperty("shippedFrom", dto.getShippedFrom());
        json.addProperty("shippedTo", dto.getShippedTo());
        json.addProperty("estArrivalDate", dto.getEstArrivalDate());
        json.addProperty("actualArrivalDate", dto.getActualArrivalDate());
        json.addProperty("percentageComplete", dto.getPercentageComplete());

        json.addProperty("alertProfileId", dto.getAlertProfileId());
        json.addProperty("alertProfileName", dto.getAlertProfileName());
        json.add(ShipmentConstants.ALERT_SUMMARY, SerializerUtils.toJson(dto.getAlertSummary()));
        json.addProperty(ShipmentConstants.SIBLING_COUNT, dto.getSiblingCount());

        //last reading data
        json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, dto.getLastReadingTimeISO());
        json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE, dto.getLastReadingTemperature());
        json.addProperty("lastReadingBattery", dto.getLastReadingBattery());
        json.addProperty("lastReadingLat", dto.getLastReadingLat());
        json.addProperty("lastReadingLong", dto.getLastReadingLong());

        //first reading
        json.addProperty("firstReadingLat", dto.getFirstReadingLat());
        json.addProperty("firstReadingLong", dto.getFirstReadingLong());

        //start location
        json.addProperty("shippedFromLat", dto.getShippedFromLat());
        json.addProperty("shippedFromLong", dto.getShippedFromLong());

        //end location
        json.addProperty("shippedToLat", dto.getShippedToLat());
        json.addProperty("shippedToLong", dto.getShippedToLong());

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
        if (json.has(JSON_SORT_ORDER)) {
            req.setSortOrder(asString(json.get(JSON_SORT_ORDER)));
        }
        if (json.has(JSON_SORT_COLUMN)) {
            req.setSortColumn(asString(json.get(JSON_SORT_COLUMN)));
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
            obj.addProperty(JSON_SORT_ORDER, r.getSortOrder());
        }
        if (r.getSortColumn() != null) {
            obj.addProperty(JSON_SORT_COLUMN, r.getSortColumn());
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
     * @param dto
     * @return
     */
    public JsonObject toJson(final SingleShipmentDto dto) {
        return toJson(dto, true);
    }

    /**
     * @param dto
     * @param isNotSibling
     * @return
     */
    protected JsonObject toJson(final SingleShipmentDto dto,
            final boolean isNotSibling) {
        if (dto == null) {
            return  null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("shipmentId", dto.getShipmentId()); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_SN, dto.getDeviceSN()); /*+*/
        if (isNotSibling) {
            json.addProperty(ShipmentConstants.DEVICE_NAME, dto.getDeviceName());
        }
        json.addProperty("tripCount", dto.getTripCount()); /*+*/

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
        json.add(ShipmentConstants.ALERT_SUMMARY, createAlertSummaryArray(dto.getAlertSummary())); /*+*/
        if (isNotSibling) {
            json.addProperty("alertYetToFire", dto.getAlertYetToFire());
            json.addProperty("alertFired", dto.getAlertFired());

            //"arrivalNotificationTimeISO": "2014-08-12 12:10",
            // NEW - ISO for actual time arrival notification sent out
            json.addProperty("arrivalNotificationTimeISO", dto.getArrivalNotificationTimeIso());
            json.addProperty("arrivalNotificationTime", dto.getArrivalNotificationTime());

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

            json.addProperty("noAlertsAfterArrivalMinutes", dto.getNoAlertsAfterArrivalMinutes());
            json.addProperty("shutDownAfterStartMinutes", dto.getShutDownAfterStartMinutes());

            json.addProperty("shutdownTimeISO", dto.getShutdownTimeIso());
            json.addProperty("shutdownTime", dto.getShutdownTime());

            json.addProperty("startLocation", dto.getStartLocation());
            json.addProperty("startTimeISO", dto.getStartTimeISO());
            json.addProperty("startTime", dto.getStartTime());
            json.add("startLocationForMap", toJson(dto.getStartLocationForMap()));

            json.addProperty("endLocation", dto.getEndLocation());
            json.addProperty("etaISO", dto.getEtaIso());
            json.addProperty("eta", dto.getEta());

            json.addProperty("arrivalTimeISO", dto.getArrivalTimeIso());
            json.addProperty("arrivalTime", dto.getArrivalTime());
            json.add("endLocationForMap", toJson(dto.getEndLocationForMap()));

            json.addProperty("lastReadingLocation", dto.getCurrentLocation());
            json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, dto.getLastReadingTimeIso());
            json.addProperty(ShipmentConstants.LAST_READING_TIME, dto.getLastReadingTime());
            json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE,
                    convertTemperature(dto.getLastReadingTemperature()));
            json.addProperty("batteryLevel", dto.getBatteryLevel());

            json.add("lastReadingForMap", toJson(dto.getCurrentLocationForMap()));

            json.addProperty("minTemp", convertTemperature(dto.getMinTemp()));
            json.addProperty("maxTemp", convertTemperature(dto.getMaxTemp()));
            json.addProperty("firstReadingTimeISO", dto.getTimeOfFirstReading());
            json.addProperty("firstReadingTime", dto.getFirstReadingTime());

            json.addProperty("alertsSuppressed", dto.isAlertsSuppressed());
            json.addProperty("alertsSuppressionTime", dto.getAlertsSuppressionTime());
            json.addProperty("alertsSuppressionTimeIso", dto.getAlertsSuppressionTimeIso());

            final JsonArray locations = new JsonArray();
            for (final SingleShipmentLocation l : dto.getLocations()) {
                locations.add(toJson(l));
            }
            json.add("locations", locations);

            final JsonArray siblings = new JsonArray();
            for (final SingleShipmentDto sibling: dto.getSiblings()) {
                siblings.add(toJson(sibling, false));
            }

            json.add("siblings", siblings);

            //alternatives
            json.add("startLocationAlternatives", locationsToJson(dto.getStartLocationAlternatives()));
            json.add("endLocationAlternatives", locationsToJson(dto.getEndLocationAlternatives()));
            json.add(INTERIM_LOCATIONS, locationsToJson(dto.getInterimLocationAlternatives()));

            //interim stops
            json.add("interimStops", interimStopsTJson(dto.getInterimStops()));

            //add notes
            final JsonArray notes = new JsonArray();
            for (final NoteDto n : dto.getNotes()) {
                notes.add(noteSerializer.toJson(n));
            }
            json.add("notes", notes);
        }

        //add device groups
        final JsonArray deviceGroups = new JsonArray();
        json.add("deviceGroups", deviceGroups);
        for (final DeviceGroupDto grp : dto.getDeviceGroups()) {
            deviceGroups.add(toJson(grp));
        }

        return json;
    }
    /**
     * @param grp
     * @return
     */
    private JsonObject toJson(final DeviceGroupDto grp) {
        if (grp == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("groupId", grp.getId());
        json.addProperty("name", grp.getName());
        json.addProperty("description", grp.getDescription());

        return json;
    }
    /**
     * @param interimStops
     * @return
     */
    private JsonArray interimStopsTJson(final List<InterimStopDto> interimStops) {
        final JsonArray array = new JsonArray();
        for (final InterimStopDto stop : interimStops) {
            array.add(toJson(stop));
        }
        return array;
    }
    /**
     * @param stop
     * @return
     */
    private JsonObject toJson(final InterimStopDto stop) {
        if (stop == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", stop.getId());
        json.addProperty("latitude", stop.getLatitude());
        json.addProperty("longitude", stop.getLongitude());
        json.addProperty("time", stop.getTime());
        json.addProperty("stopDate", stop.getStopDate());
        json.addProperty("stopDateISO", stop.getStopDateIso());
        json.add("location", toJson(stop.getLocation()));
        return json;
    }

    /**
     * @param locs locations.
     * @return JSON array of locations.
     */
    private JsonArray locationsToJson(final List<LocationProfile> locs) {
        final JsonArray array = new JsonArray();
        for (final LocationProfile loc : locs) {
            array.add(toJson(loc));
        }
        return array;
    }

    /**
     * @param loc
     * @return
     */
    protected JsonElement toJson(final LocationProfile loc) {
        return locationSerializer.toJson(loc);
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
     * @return
     */
    private JsonObject toJson(final SingleShipmentLocation l) {
        if (l == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("lat", l.getLatitude());
        json.addProperty("long", l.getLongitude());
        json.addProperty("temperature", convertTemperature(l.getTemperature()));
        json.addProperty("timeISO", l.getTimeIso());
        json.addProperty("time", l.getTime());
        json.addProperty("type", l.getType());

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

    /**
     * @param u
     * @return
     */
    private DateFormat createPrettyFormat(final User u) {
        return DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());
    }
    /**
     * @param u
     * @return
     */
    private DateFormat createIsoFormat(final User u) {
        return DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
    }

    public static void main(final String[] args) throws Exception {
        final ShipmentSerializer ser = new ShipmentSerializer(new User());

        final String req;
        final InputStream in = ShipmentSerializer.class.getResourceAsStream("req.json");
        try {
            req = StringUtils.getContent(in, "UTF-8");
        } finally {
            in.close();
        }

        final JsonObject json = SerializerUtils.parseJson(req).getAsJsonObject();

        final SaveShipmentRequest s = ser.parseSaveShipmentRequest(json);
        System.out.println(s);
    }
}
