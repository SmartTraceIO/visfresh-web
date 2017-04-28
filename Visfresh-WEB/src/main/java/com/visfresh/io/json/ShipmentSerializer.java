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
import com.visfresh.io.KeyLocation;
import com.visfresh.io.NoteDto;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializer extends AbstractJsonSerializer {
    private static final String INTERIM_STOPS = "interimStops";
    private static final String INTERIM_LOCATIONS = "interimLocations";
    private static final String CREATED_BY = "createdBy";
    private static final String START_DATE = "startDate";
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
        this.isoFormat = createIsoFormat(user.getLanguage(), user.getTimeZone());
        this.prettyFormat = createPrettyFormat(user.getLanguage(), user.getTimeZone());
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

        final JsonElement endLocs = obj.get(ShipmentConstants.END_LOCATION_ALTERNATIVES);
        if (endLocs != null && !endLocs.isJsonNull()) {
            shp.setEndLocationAlternatives(asLongList(endLocs));
        }
        //TODO correct
        final JsonElement locs = obj.get(INTERIM_LOCATIONS);
        if (locs != null && !locs.isJsonNull()) {
            shp.setInterimLocations(asLongList(locs));
        }

        if (has(obj, ShipmentConstants.SEND_ARRIVAL_REPORT)) {
            shp.setSendArrivalReport(asBoolean(obj.get(ShipmentConstants.SEND_ARRIVAL_REPORT)));
        }
        if (has(obj, ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS)) {
            shp.setSendArrivalReportOnlyIfAlerts(asBoolean(obj.get(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS)));
        }
        final JsonElement cAccess = obj.get(ShipmentConstants.COMPANY_ACCESS);
        if (cAccess != null && !cAccess.isJsonNull()) {
            shp.getCompanyAccess().addAll(asLongList(cAccess));
        }
        final JsonElement uAccess = obj.get(ShipmentConstants.USER_ACCESS);
        if (uAccess != null && !uAccess.isJsonNull()) {
            shp.getUserAccess().addAll(asLongList(uAccess));
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

        final JsonElement locs = json.get(INTERIM_STOPS);
        if (locs != null && !locs.isJsonNull()) {
            s.setInterimStops(asLongList(locs));
        }

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

        obj.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, s.isSendArrivalReport());
        obj.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, s.isSendArrivalReportOnlyIfAlerts());

        obj.add(ShipmentConstants.CUSTOM_FIELDS, SerializerUtils.toJson(s.getCustomFields()));
        obj.addProperty(START_DATE, formatDate(s.getStartDate()));
        obj.addProperty(CREATED_BY, s.getCreatedBy());

        //end location alternatives
        if (s.getEndLocationAlternatives() != null) {
            final JsonArray array = new JsonArray();
            obj.add(ShipmentConstants.END_LOCATION_ALTERNATIVES, array);

            for (final Long l : s.getEndLocationAlternatives()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(ShipmentConstants.END_LOCATION_ALTERNATIVES, JsonNull.INSTANCE);
        }

        //interim locations
        if (s.getInterimLocations() != null) {
            final JsonArray array = new JsonArray();
            obj.add(INTERIM_LOCATIONS, array);

            for (final Long l : s.getInterimLocations()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(INTERIM_LOCATIONS, JsonNull.INSTANCE);
        }

        //interim stops
        if (s.getInterimStops() != null) {
            final JsonArray array = new JsonArray();
            obj.add(INTERIM_STOPS, array);

            for (final Long l : s.getInterimStops()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(INTERIM_STOPS, JsonNull.INSTANCE);
        }

        obj.add(ShipmentConstants.USER_ACCESS, toJsonArray(s.getUserAccess()));
        obj.add(ShipmentConstants.COMPANY_ACCESS, toJsonArray(s.getCompanyAccess()));

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
        json.addProperty("shipmentDateISO", dto.getShipmentDateISO());

        json.addProperty("palletId", dto.getPalettId());
        json.addProperty("assetNum", dto.getAssetNum());
        json.addProperty("assetType", dto.getAssetType());

        json.addProperty("shippedFrom", dto.getShippedFrom());
        json.addProperty("shippedTo", dto.getShippedTo());
        json.addProperty("estArrivalDate", dto.getEstArrivalDate());
        json.addProperty("estArrivalDateISO", dto.getEstArrivalDateISO());
        json.addProperty("actualArrivalDate", dto.getActualArrivalDate());
        json.addProperty("actualArrivalDateISO", dto.getActualArrivalDateISO());
        json.addProperty("percentageComplete", dto.getPercentageComplete());

        json.addProperty("alertProfileId", dto.getAlertProfileId());
        json.addProperty("alertProfileName", dto.getAlertProfileName());
        json.add(ShipmentConstants.ALERT_SUMMARY, SerializerUtils.toJson(dto.getAlertSummary()));
        json.addProperty(ShipmentConstants.SIBLING_COUNT, dto.getSiblingCount());

        //last reading data
        json.addProperty(ShipmentConstants.LAST_READING_TIME, dto.getLastReadingTime());
        json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, dto.getLastReadingTimeISO());
        json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE, dto.getLastReadingTemperature());
        json.addProperty("lastReadingBattery", dto.getLastReadingBattery());
        json.addProperty("lastReadingLat", dto.getLastReadingLat());
        json.addProperty("lastReadingLong", dto.getLastReadingLong());

        //first reading
        json.addProperty("firstReadingLat", dto.getFirstReadingLat());
        json.addProperty("firstReadingLong", dto.getFirstReadingLong());
        json.addProperty("firstReadingTime", dto.getFirstReadingTime());
        json.addProperty("firstReadingTimeISO", dto.getFirstReadingTimeISO());

        int i = 1;
        for (final SingleShipmentInterimStop stp : dto.getInterimStops()) {
            final String prefix = "interimStop" + i;
            json.addProperty(prefix, stp.getLocation().getName());
            json.addProperty(prefix + "Time", stp.getStopDate());
            json.addProperty(prefix + "TimeISO", stp.getStopDateIso());
            i++;
        }

        //key locations
        if (dto.getKeyLocations() != null) {
            final JsonArray kls = new JsonArray();
            json.add("keyLocations", kls);

            for (final KeyLocation kl : dto.getKeyLocations()) {
                kls.add(toJson(kl));
            }
        } else {
            json.add("keyLocations", null);
        }

        //start location
        json.addProperty("shippedFromLat", dto.getShippedFromLat());
        json.addProperty("shippedFromLong", dto.getShippedFromLong());

        //end location
        json.addProperty("shippedToLat", dto.getShippedToLat());
        json.addProperty("shippedToLong", dto.getShippedToLong());

        //arrival report
        json.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, dto.isSendArrivalReport());
        json.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, dto.isSendArrivalReportOnlyIfAlerts());

        return json;
    }
    /**
     * @param kl key location.
     * @return
     */
    public JsonObject toJson(final KeyLocation kl) {
        if (kl == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("key", kl.getKey());
        json.addProperty("lat", kl.getLatitude());
        json.addProperty("lon", kl.getLongitude());
        json.addProperty("desc", kl.getDescription());
        json.addProperty("time", kl.getPrettyTime());
        return json;
    }
    public KeyLocation parseKeyLocation(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final KeyLocation loc = new KeyLocation();
        loc.setKey(asString(json.get("key")));
        final JsonElement lat = json.get("lat");
        if (lat != null && !lat.isJsonNull()) {
            loc.setLatitude(asDouble(lat));
        }
        final JsonElement lon = json.get("lon");
        if (lon != null && !lon.isJsonNull()) {
            loc.setLongitude(asDouble(lon));
        }
        loc.setDescription(asString(json.get("desc")));
        loc.setPrettyTime(asString(json.get("time")));
        return loc;
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
        json.addProperty(ShipmentConstants.DEVICE_COLOR, dto.getDeviceColor());
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
        json.addProperty("isLatestShipment", dto.isLatestShipment());
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

            json.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, dto.isSendArrivalReport());
            json.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, dto.isSendArrivalReportOnlyIfAlerts());
            json.addProperty("arrivalReportSent", dto.isArrivalReportSent());

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
            json.add(ShipmentConstants.END_LOCATION_ALTERNATIVES, locationsToJson(dto.getEndLocationAlternatives()));
            json.add(INTERIM_LOCATIONS, locationsToJson(dto.getInterimLocationAlternatives()));

            //interim stops
            json.add(INTERIM_STOPS, interimStopsToJson(dto.getInterimStops()));

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

        //company access
        final JsonArray userAccess = new JsonArray();
        json.add("userAccess", userAccess);
        for (final ShipmentUserDto u : dto.getUserAccess()) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("userId", u.getId());
            cobj.addProperty("email", u.getEmail());
            userAccess.add(cobj);
        }

        //company access
        final JsonArray companyAccess = new JsonArray();
        json.add("companyAccess", companyAccess);
        for (final ShipmentCompanyDto c : dto.getCompanyAccess()) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("companyId", c.getId());
            cobj.addProperty("companyName", c.getName());
            companyAccess.add(cobj);
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
    private JsonArray interimStopsToJson(final List<SingleShipmentInterimStop> interimStops) {
        final JsonArray array = new JsonArray();
        for (final SingleShipmentInterimStop stop : interimStops) {
            array.add(toJson(stop));
        }
        return array;
    }
    /**
     * @param stop
     * @return
     */
    private JsonObject toJson(final SingleShipmentInterimStop stop) {
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
