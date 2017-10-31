/**
 *
 */
package com.visfresh.io.json;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Language;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.KeyLocation;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializer extends AbstractJsonSerializer {
    private static final String CREATED_BY = "createdBy";
    private static final String START_DATE = "startDate";
    private final DateFormat isoFormat;
    private final DateFormat prettyFormat;
    private final TemperatureUnits units;

    /**
     * @param lang language.
     * @param tz time zone.
     */
    public ShipmentSerializer(final Language lang, final TimeZone tz, final TemperatureUnits units) {
        super(tz);
        this.isoFormat = createIsoFormat(lang, tz);
        this.prettyFormat = createPrettyFormat(lang, tz);
        this.units = units;
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
        final JsonElement locs = obj.get(ShipmentConstants.INTERIM_LOCATIONS);
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

        final JsonElement locs = json.get(ShipmentConstants.INTERIM_STOPS);
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
            obj.add(ShipmentConstants.INTERIM_LOCATIONS, array);

            for (final Long l : s.getInterimLocations()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(ShipmentConstants.INTERIM_LOCATIONS, JsonNull.INSTANCE);
        }

        //interim stops
        if (s.getInterimStops() != null) {
            final JsonArray array = new JsonArray();
            obj.add(ShipmentConstants.INTERIM_STOPS, array);

            for (final Long l : s.getInterimStops()) {
                array.add(new JsonPrimitive(l));
            }
        } else {
            obj.add(ShipmentConstants.INTERIM_STOPS, JsonNull.INSTANCE);
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
    public JsonObject toJson(final ListShipmentItem dto, final List<KeyLocation> keyLocations) {
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
        json.addProperty("shipmentDate", getFormatted(prettyFormat, dto.getShipmentDate()));
        json.addProperty("shipmentDateISO", getFormatted(isoFormat, dto.getShipmentDate()));

        json.addProperty("palletId", dto.getPalettId());
        json.addProperty("assetNum", dto.getAssetNum());
        json.addProperty("assetType", dto.getAssetType());

        json.addProperty("shippedFrom", dto.getShippedFrom());
        json.addProperty("shippedTo", dto.getShippedTo());
        json.addProperty("estArrivalDate", getFormatted(prettyFormat, dto.getEta()));
        json.addProperty("estArrivalDateISO", getFormatted(isoFormat, dto.getEta()));
        json.addProperty("actualArrivalDate", getFormatted(prettyFormat, dto.getActualArrivalDate()));
        json.addProperty("actualArrivalDateISO", getFormatted(isoFormat, dto.getActualArrivalDate()));
        json.addProperty("percentageComplete", dto.getPercentageComplete());

        json.addProperty("alertProfileId", dto.getAlertProfileId());
        json.addProperty("alertProfileName", dto.getAlertProfileName());
        json.add(ShipmentConstants.ALERT_SUMMARY, SerializerUtils.toJson(createAlertSummary(dto)));
        json.addProperty(ShipmentConstants.SIBLING_COUNT, dto.getSiblingCount());

        //last reading data
        json.addProperty(ShipmentConstants.LAST_READING_TIME, getFormatted(prettyFormat, dto.getLastReadingTime()));
        json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, getFormatted(isoFormat, dto.getLastReadingTime()));
        json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE,
                dto.getLastReadingTemperature() == null ? null
                        : LocalizationUtils.convertToUnits(dto.getLastReadingTemperature(), this.units));
        json.addProperty("lastReadingBattery", dto.getLastReadingBattery());
        json.addProperty("lastReadingLat", dto.getLastReadingLat());
        json.addProperty("lastReadingLong", dto.getLastReadingLong());

        //first reading
        json.addProperty("firstReadingLat", dto.getFirstReadingLat());
        json.addProperty("firstReadingLong", dto.getFirstReadingLong());
        json.addProperty("firstReadingTime", getFormatted(prettyFormat, dto.getFirstReadingTime()));
        json.addProperty("firstReadingTimeISO", getFormatted(isoFormat, dto.getFirstReadingTime()));

        int i = 1;
        for (final SingleShipmentInterimStop stp : dto.getInterimStops()) {
            final String prefix = "interimStop" + i;
            json.addProperty(prefix, stp.getLocation().getName());
            json.addProperty(prefix + "Time", getFormatted(prettyFormat, stp.getStopDate()));
            json.addProperty(prefix + "TimeISO", getFormatted(isoFormat, stp.getStopDate()));
            i++;
        }

        //key locations
        if (keyLocations != null && keyLocations.size() > 0) {
            final JsonArray kls = new JsonArray();
            json.add("keyLocations", kls);

            for (final KeyLocation kl : keyLocations) {
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
     * @param dto
     * @return
     */
    private Map<AlertType, Integer> createAlertSummary(final ListShipmentItem dto) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final AlertBean a : dto.getSentAlerts()) {
            final AlertType type = a.getType();
            if (type != AlertType.LightOff && type != AlertType.LightOn) {
                Integer numAlerts = map.get(type);
                if (numAlerts == null) {
                    numAlerts = 0;
                }
                numAlerts = numAlerts + 1;
                map.put(type, numAlerts);
            }
        }
        return map;
    }
    /**
     * @param fmt
     * @param date
     * @return
     */
    private String getFormatted(final DateFormat fmt, final Date date) {
        return date == null ? null : fmt.format(date);
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
        json.addProperty("time", getFormatted(prettyFormat, new Date(kl.getTime())));
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

    public static void main(final String[] args) throws Exception {
        final ShipmentSerializer ser = new ShipmentSerializer(Language.English, TimeZone.getDefault(),
                TemperatureUnits.Celsius);

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
