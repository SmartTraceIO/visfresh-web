/**
 *
 */
package com.visfresh.io.json;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.NoteDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertDto;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NotificationIssueBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentSerializer extends AbstractJsonSerializer {
    private TemperatureUnits tempUnits;
    private final NoteSerializer noteSerializer;
    private final LocationSerializer locationSerializer;

    public SingleShipmentSerializer(final Language lang, final TimeZone tz, final TemperatureUnits units) {
        super(tz);
        locationSerializer = new LocationSerializer(tz);
        noteSerializer = new NoteSerializer(tz);
        this.tempUnits = units;
    }
    /**
     * @param dto
     * @return
     */
    public JsonObject exportToViewData(final SingleShipmentDto dto) {
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
            json.add("alertProfile", toJson(dto.getAlertProfile()));
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
            json.add(ShipmentConstants.INTERIM_LOCATIONS, locationsToJson(dto.getInterimLocationAlternatives()));

            //interim stops
            json.add(ShipmentConstants.INTERIM_STOPS, interimStopsToJson(dto.getInterimStops()));

            //add notes
            json.add("notes", notesToJson(dto.getNotes()));
        }

        //add device groups
        json.add("deviceGroups", deviceGroupsToJson(dto.getDeviceGroups()));

        //company access
        json.add("userAccess", userAcessToJson(dto.getUserAccess()));

        //company access
        json.add("companyAccess", companyAccessToJson(dto.getCompanyAccess()));

        //sent alerts
        json.add("alertsWithCorrectiveActions", sentAlertsToJson(dto.getSentAlerts()));

        return json;
    }
    /**
     * @param sentAlerts
     * @return
     */
    private JsonArray sentAlertsToJson(final List<AlertDto> sentAlerts) {
        final JsonArray array = new JsonArray();
        for (final AlertDto a : sentAlerts) {
            array.add(toJson(a));
        }
        return array;
    }
    /**
     * @param sentAlerts
     * @return
     */
    protected JsonArray alertsToJson(final List<AlertBean> sentAlerts) {
        final JsonArray array = new JsonArray();
        for (final AlertBean a : sentAlerts) {
            array.add(toJson(a));
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<AlertBean> parseAlertBeans(final JsonArray array) {
        final List<AlertBean> alerts = new LinkedList<>();
        for (final JsonElement e : array) {
            alerts.add(parseAlertBean(e));
        }
        return alerts;
    }
    /**
     * @param companyAccess
     * @return
     */
    protected JsonArray companyAccessToJson(final List<ShipmentCompanyDto> companyAccess) {
        final JsonArray array = new JsonArray();
        for (final ShipmentCompanyDto c : companyAccess) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("companyId", c.getId());
            cobj.addProperty("companyName", c.getName());
            array.add(cobj);
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<ShipmentCompanyDto> parseCompanyAccessArray(final JsonArray array) {
        final List<ShipmentCompanyDto> companies = new LinkedList<>();
        for (final JsonElement e : array) {
            final JsonObject json = e.getAsJsonObject();
            final ShipmentCompanyDto c = new ShipmentCompanyDto();
            c.setId(asLong(json.get("companyId")));
            c.setName(asString(json.get("companyName")));

            companies.add(c);
        }
        return companies;
    }
    /**
     * @param userAccess
     * @return
     */
    protected JsonArray userAcessToJson(final List<ShipmentUserDto> userAccess) {
        final JsonArray array = new JsonArray();
        for (final ShipmentUserDto u : userAccess) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("userId", u.getId());
            cobj.addProperty("email", u.getEmail());
            array.add(cobj);
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<ShipmentUserDto> parseUserAccessArray(final JsonArray array) {
        final List<ShipmentUserDto> users = new LinkedList<>();
        for (final JsonElement e : array) {
            final JsonObject json = e.getAsJsonObject();
            final ShipmentUserDto user = new ShipmentUserDto();

            user.setId(asLong(json.get("userId")));
            user.setEmail(asString(json.get("email")));
            users.add(user);
        }
        return users;
    }
    /**
     * @param dg
     * @return
     */
    protected JsonArray deviceGroupsToJson(final List<DeviceGroupDto> dg) {
        final JsonArray deviceGroups = new JsonArray();
        for (final DeviceGroupDto grp : dg) {
            deviceGroups.add(toJson(grp));
        }
        return deviceGroups;
    }
    /**
     * @param array
     * @return
     */
    private List<DeviceGroupDto> parseDeviceGroups(final JsonArray array) {
        final List<DeviceGroupDto> groups = new LinkedList<>();
        for (final JsonElement e : array) {
            groups.add(parseDeviceGroupDto(e));
        }
        return groups;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final AlertProfileDto alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject obj = alertProfileBeanToJson(alert);
        obj.addProperty(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getLowerTemperatureLimit(), tempUnits));
        obj.addProperty(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getUpperTemperatureLimit(), tempUnits));

        return obj;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject alertProfileBeanToJson(final AlertProfileDto alert) {
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
        obj.addProperty(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT, alert.getLowerTemperatureLimit());
        obj.addProperty(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT, alert.getUpperTemperatureLimit());

        return obj;
    }
    /**
     * @param el
     * @return
     */
    private AlertProfileDto parseAlertProfileBean(final JsonElement el) {
        if (isNull(el)) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final AlertProfileDto ap = new AlertProfileDto();
        ap.setId(asLong(json.get(AlertProfileConstants.ALERT_PROFILE_ID)));
        ap.setName(asString(json.get(AlertProfileConstants.ALERT_PROFILE_NAME)));
        ap.setDescription(asString(json.get(AlertProfileConstants.ALERT_PROFILE_DESCRIPTION)));
        ap.setWatchBatteryLow(asBoolean(json.get(AlertProfileConstants.WATCH_BATTERY_LOW)));
        ap.setWatchEnterBrightEnvironment(asBoolean(json.get(AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT)));
        ap.setWatchEnterDarkEnvironment(asBoolean(json.get(AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT)));
        ap.setWatchMovementStart(asBoolean(json.get(AlertProfileConstants.WATCH_MOVEMENT_START)));
        ap.setWatchMovementStop(asBoolean(json.get(AlertProfileConstants.WATCH_MOVEMENT_STOP)));
        ap.setLowerTemperatureLimit(asDouble(json.get(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT)));
        ap.setUpperTemperatureLimit(asDouble(json.get(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT)));
        return ap;
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
     * @param e
     * @return
     */
    private ListNotificationScheduleItem parseListNotificationScheduleItem(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final ListNotificationScheduleItem item = new ListNotificationScheduleItem();
        item.setNotificationScheduleId(asLong(json.get("notificationScheduleId")));
        item.setNotificationScheduleName(asString(json.get("notificationScheduleName")));
        return item;
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
     * @param locs locations.
     * @return JSON array of locations.
     */
    private JsonArray locationsToJson(final List<LocationProfileBean> locs) {
        final JsonArray array = new JsonArray();
        for (final LocationProfileBean loc : locs) {
            array.add(toJson(loc));
        }
        return array;
    }
    /**
     * @param jsonElement
     * @return
     */
    private List<LocationProfileBean> parseLocationProfileBans(final JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        final List<LocationProfileBean> locs = new LinkedList<>();
        for (final JsonElement el : jsonElement.getAsJsonArray()) {
            locs.add(parseLocationProfileBean(el));
        }
        return locs;
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
     * @param array
     * @return
     */
    private List<InterimStopBean> parseInterimStops(final JsonArray array) {
        final List<InterimStopBean> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(parseInterimStopBean(e));
        }
        return list;
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
     * @param stop
     * @return
     */
    private JsonObject toJson(final InterimStopBean stop) {
        if (stop == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", stop.getId());
        json.addProperty("time", stop.getTime());
        json.addProperty("stopDate", formatDate(stop.getStopDate()));
        json.add("location", toJson(stop.getLocation()));
        return json;
    }
    /**
     * @param e JSON element.
     * @return single shipment interim stop.
     */
    private InterimStopBean parseInterimStopBean(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final InterimStopBean stop = new InterimStopBean();
        stop.setId(asLong(json.get("id")));
        stop.setTime(asInt(json.get("time")));
        stop.setStopDate(asDate(json.get("stopDate")));
        stop.setLocation(parseLocationProfileBean(json.get("location")));
        return stop;
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
     * @param e
     * @return
     */
    private DeviceGroupDto parseDeviceGroupDto(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final DeviceGroupDto group = new DeviceGroupDto();
        group.setId(asLong(json.get("groupId")));
        group.setName(asString(json.get("name")));
        group.setDescription(asString(json.get("description")));
        return group;
    }
    /**
     * @param a alert DTO.
     * @return JSON object.
     */
    private JsonObject toJson(final AlertDto a) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", a.getId());
        json.addProperty("description", a.getDescription());
        json.addProperty("time", a.getTime());
        json.addProperty("timeISO", a.getTimeISO());
        json.addProperty("correctiveActionListId", a.getCorrectiveActionListId());
        json.addProperty("type", a.getType().toString());
        return json;
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
     * @param el
     * @return
     */
    private Location parseLocation(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final Location loc = new Location();
        loc.setLatitude(asDouble(json.get("latitude")));
        loc.setLongitude(asDouble(json.get("longitude")));

        return loc;
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
        double value = tempUnits == TemperatureUnits.Fahrenheit ? t * 1.8 + 32 : t;
        //cut extra decimal signs.
        value = Math.round(value * 100) / 100.;
        return value;
    }
    /**
     * @param location
     * @return
     */
    protected JsonElement toJson(final LocationProfileBean location) {
        return locationSerializer.toJson(location);
    }
    /**
     * @param el JSON element.
     * @return location profile bean.
     */
    private LocationProfileBean parseLocationProfileBean(final JsonElement el) {
        return locationSerializer.parseLocationProfileDto(el);
    }

    //Single shipment bean
    public JsonObject toJson(final SingleShipmentBean s) {
        if (s == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("shipmentId", s.getShipmentId());
        json.addProperty("companyId", s.getCompanyId());
        json.addProperty("deviceSN", s.getDeviceSN());
        json.addProperty("deviceName", s.getDeviceName());
        json.addProperty("tripCount", s.getTripCount());
        json.addProperty("shipmentDescription", s.getShipmentDescription());
        json.addProperty("palletId", s.getPalletId());
        json.addProperty("assetNum", s.getAssetNum());
        json.addProperty("assetType", s.getAssetType());
        json.addProperty("status", s.getStatus().name());
        json.addProperty("alertSuppressionMinutes", s.getAlertSuppressionMinutes());
        json.add("alertsNotificationSchedules",
                createNotificationScheduleArray(s.getAlertsNotificationSchedules()));
        json.addProperty("commentsForReceiver", s.getCommentsForReceiver());
        json.addProperty("arrivalNotificationWithinKm", s.getArrivalNotificationWithinKm());
        json.addProperty("excludeNotificationsIfNoAlerts", s.isExcludeNotificationsIfNoAlerts());
        json.add("arrivalNotificationSchedules", createNotificationScheduleArray(
                s.getArrivalNotificationSchedules()));
        json.addProperty("sendArrivalReport", s.isSendArrivalReport());
        json.addProperty("sendArrivalReportOnlyIfAlerts", s.isSendArrivalReportOnlyIfAlerts());
        json.addProperty("shutdownDeviceAfterMinutes", s.getShutdownDeviceAfterMinutes());
        json.addProperty("noAlertsAfterArrivalMinutes", s.getNoAlertsAfterArrivalMinutes());
        json.addProperty("shutDownAfterStartMinutes", s.getShutDownAfterStartMinutes());
        json.add("startLocation", toJson(s.getStartLocation()));
        json.addProperty("startTime", formatDate(s.getStartTime()));
        json.add("endLocation", toJson(s.getEndLocation()));
        json.addProperty("eta", formatDate(s.getEta()));
        json.add("currentLocation", toJson(s.getCurrentLocation()));
        json.addProperty("currentLocationDescription", s.getCurrentLocationDescription());
        json.addProperty("percentageComplete", s.getPercentageComplete());

        json.addProperty("minTemp", s.getMinTemp());
        json.addProperty("maxTem", s.getMaxTemp());
        json.addProperty("timeOfFirstReading", formatDate(s.getTimeOfFirstReading()));

        final JsonArray locations = new JsonArray();
        for (final SingleShipmentLocationBean b : s.getLocations()) {
            locations.add(toJson(b));
        }
        json.add("locations", locations);

        json.add("siblings", toJsonArray(s.getSiblings()));
        json.add("alertSummary", createAlertSummaryArray(s.getAlertSummary()));
        json.add("alertYetToFire", alertRulesToJson(s.getAlertYetToFire()));
        json.add("alertFired", alertRulesToJson(s.getAlertFired()));
        json.addProperty("arrivalNotificationTime", formatDate(s.getArrivalNotificationTime()));
        json.addProperty("shutdownTime", formatDate(s.getShutdownTime()));
        json.addProperty("arrivalTime", formatDate(s.getArrivalTime()));
        json.addProperty("alertsSuppressed", s.isAlertsSuppressed());
        json.addProperty("alertsSuppressionTime", formatDate(s.getAlertsSuppressionTime()));
        json.addProperty("firstReadingTime", formatDate(s.getFirstReadingTime()));
        json.addProperty("lastReadingTime", formatDate(s.getLastReadingTime()));
        json.addProperty("lastReadingTemperature", s.getLastReadingTemperature());
        json.addProperty("batteryLevel", s.getBatteryLevel());
        json.addProperty("noAlertsAfterStartMinutes", s.getNoAlertsAfterStartMinutes());
        json.addProperty("shipmentType", s.getShipmentType());

        json.add("startLocationAlternatives", locationsToJson(s.getStartLocationAlternatives()));
        json.add("endLocationAlternatives", locationsToJson(s.getEndLocationAlternatives()));
        json.add("interimLocationAlternatives", locationsToJson(s.getInterimLocationAlternatives()));

        final JsonArray interimStops = new JsonArray();
        for (final InterimStopBean stp : s.getInterimStops()) {
            interimStops.add(toJson(stp));
        }
        json.add("interimStops", interimStops);
        json.add("notes", notesToJson(s.getNotes()));
        json.add("deviceGroups", deviceGroupsToJson(s.getDeviceGroups()));
        json.addProperty("deviceColor", s.getDeviceColor());
        json.addProperty("isLatestShipment", s.isLatestShipment());
        json.addProperty("arrivalReportSent", s.isArrivalReportSent());
        json.add("userAccess", userAcessToJson(s.getUserAccess()));
        json.add("companyAccess", companyAccessToJson(s.getCompanyAccess()));
        json.add("sentAlerts", alertsToJson(s.getSentAlerts()));
        json.add("alertProfile", alertProfileBeanToJson(s.getAlertProfile()));

        return json;
    }
    public SingleShipmentBean parseSingleShipmentBean(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final SingleShipmentBean s = new SingleShipmentBean();

        s.setShipmentId(asLong(json.get("shipmentId")));
        s.setCompanyId(asLong(json.get("companyId")));
        s.setDeviceSN(asString(json.get("deviceSN")));
        s.setDeviceName(asString(json.get("deviceName")));
        s.setTripCount(asInt(json.get("tripCount")));
        s.setShipmentDescription(asString(json.get("shipmentDescription")));
        s.setPalletId(asString(json.get("palletId")));
        s.setAssetNum(asString(json.get("assetNum")));
        s.setAssetType(asString(json.get("assetType")));
        s.setStatus(ShipmentStatus.valueOf(asString(json.get("status"))));
        s.setAlertSuppressionMinutes(asInteger(json.get("alertSuppressionMinutes")));

        s.getAlertsNotificationSchedules().addAll(parseNotificationSchedules(
                json.get("alertsNotificationSchedules").getAsJsonArray()));

        s.setCommentsForReceiver(asString(json.get("commentsForReceiver")));
        s.setArrivalNotificationWithinKm(asInteger(json.get("arrivalNotificationWithinKm")));
        s.setExcludeNotificationsIfNoAlerts(asBoolean(json.get("excludeNotificationsIfNoAlerts")));

        s.getArrivalNotificationSchedules().addAll(parseNotificationSchedules(
                json.get("arrivalNotificationSchedules").getAsJsonArray()));

        s.setSendArrivalReport(asBoolean(json.get("sendArrivalReport")));
        s.setSendArrivalReportOnlyIfAlerts(asBoolean(json.get("sendArrivalReportOnlyIfAlerts")));
        s.setShutdownDeviceAfterMinutes(asInteger(json.get("shutdownDeviceAfterMinutes")));
        s.setNoAlertsAfterArrivalMinutes(asInteger(json.get("noAlertsAfterArrivalMinutes")));
        s.setShutDownAfterStartMinutes(asInteger(json.get("shutDownAfterStartMinutes")));
        s.setStartLocation(parseLocationProfileBean(json.get("startLocation")));
        s.setStartTime(asDate(json.get("startTime")));
        s.setEndLocation(parseLocationProfileBean(json.get("endLocation")));
        s.setEta(asDate(json.get("eta")));
        s.setCurrentLocation(parseLocation(json.get("currentLocation")));
        s.setCurrentLocationDescription(asString(json.get("currentLocationDescription")));
        s.setPercentageComplete(asInt(json.get("percentageComplete")));

        s.setMinTemp(asDouble(json.get("minTemp")));
        s.setMaxTemp(asDouble(json.get("maxTem")));
        s.setTimeOfFirstReading(asDate(json.get("timeOfFirstReading")));

        final JsonArray locations = json.get("locations").getAsJsonArray();
        for (final JsonElement el : locations) {
            s.getLocations().add(parseSingleShipmentLocationBean(el));
        }

        s.getSiblings().addAll(parseLongList(json.get("siblings").getAsJsonArray()));

        final JsonArray alertSummary = json.get("alertSummary").getAsJsonArray();
        for (final JsonElement el : alertSummary) {
            s.getAlertSummary().add(AlertType.valueOf(asString(el)));
        }

        s.getAlertYetToFire().addAll(parseAlertRuleBeans(
                json.get("alertYetToFire").getAsJsonArray()));
        s.getAlertFired().addAll(parseAlertRuleBeans(
                json.get("alertFired").getAsJsonArray()));

        s.setArrivalNotificationTime(asDate(json.get("arrivalNotificationTime")));
        s.setShutdownTime(asDate(json.get("shutdownTime")));
        s.setArrivalTime(asDate(json.get("arrivalTime")));
        s.setAlertsSuppressed(asBoolean(json.get("alertsSuppressed")));
        s.setAlertsSuppressionTime(asDate(json.get("alertsSuppressionTime")));
        s.setFirstReadingTime(asDate(json.get("firstReadingTime")));
        s.setLastReadingTime(asDate(json.get("lastReadingTime")));
        s.setLastReadingTemperature(asDouble(json.get("lastReadingTemperature")));
        s.setBatteryLevel(asInt(json.get("batteryLevel")));
        s.setNoAlertsAfterStartMinutes(asInteger(json.get("noAlertsAfterStartMinutes")));
        s.setShipmentType(asString(json.get("shipmentType")));

        s.getStartLocationAlternatives().addAll(parseLocationProfileBans(json.get("startLocationAlternatives")));
        s.getEndLocationAlternatives().addAll(parseLocationProfileBans(
                json.get("endLocationAlternatives").getAsJsonArray()));
        s.getInterimLocationAlternatives().addAll(parseLocationProfileBans(
                json.get("interimLocationAlternatives").getAsJsonArray()));

        s.getInterimStops().addAll(parseInterimStops(json.get("interimStops").getAsJsonArray()));

        s.getNotes().addAll(parseNotes(json.get("notes").getAsJsonArray()));
        s.getDeviceGroups().addAll(parseDeviceGroups(json.get("deviceGroups").getAsJsonArray()));
        s.setDeviceColor(asString(json.get("deviceColor")));
        s.setLatestShipment(asBoolean(json.get("isLatestShipment")));
        s.setArrivalReportSent(asBoolean(json.get("arrivalReportSent")));
        s.getUserAccess().addAll(parseUserAccessArray(json.get("userAccess").getAsJsonArray()));
        s.getCompanyAccess().addAll(parseCompanyAccessArray(json.get("companyAccess").getAsJsonArray()));
        s.getSentAlerts().addAll(parseAlertBeans(json.get("sentAlerts").getAsJsonArray()));
        s.setAlertProfile(parseAlertProfileBean(json.get(("alertProfile"))));

        return s;
    }
    /**
     * @param loc
     * @return
     */
    private JsonObject toJson(final SingleShipmentLocationBean loc) {
        if (loc == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("latitude", loc.getLatitude());
        json.addProperty("longitude", loc.getLongitude());
        json.addProperty("temperature", loc.getTemperature());
        json.addProperty("time", formatDate(loc.getTime()));
        json.add("alerts", alertsToJson(loc.getAlerts()));
        json.addProperty("type", loc.getType());
        return json;
    }
    /**
     * @param el
     * @return
     */
    private SingleShipmentLocationBean parseSingleShipmentLocationBean(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final SingleShipmentLocationBean bean = new SingleShipmentLocationBean();
        bean.setLatitude(asDouble(json.get("latitude")));
        bean.setLongitude(asDouble(json.get("longitude")));
        bean.setTemperature(asDouble(json.get("temperature")));
        bean.setTime(asDate(json.get("time")));
        bean.getAlerts().addAll(parseAlertBeans(json.get("alerts").getAsJsonArray()));
        bean.setType(asString(json.get("type")));
        return bean;
    }
    /**
     * @param a
     * @return
     */
    private JsonObject toJson(final AlertBean a) {
        if (a == null) {
            return null;
        }

        final JsonObject json = notiticationIssueToJson(a);
        json.addProperty("type", a.getType().name());

        if (a instanceof TemperatureAlertBean) {
            final TemperatureAlertBean ta = (TemperatureAlertBean) a;

            json.addProperty("temperature", ta.getTemperature());
            json.addProperty("minutes", ta.getMinutes());
            json.addProperty("cumulative", ta.isCumulative());
            json.addProperty("ruleId", ta.getRuleId());
        }

        return json;
    }
    /**
     * @param el
     * @return
     */
    private AlertBean parseAlertBean(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final boolean isTemperatureAlert = json.has("temperature");

        final AlertBean a = isTemperatureAlert ? new TemperatureAlertBean() : new AlertBean();
        jsonToNotiticationIssue(json, a);

        a.setType(AlertType.valueOf(asString(json.get("type"))));
        if (isTemperatureAlert) {
            final TemperatureAlertBean ta = (TemperatureAlertBean) a;

            ta.setTemperature(asDouble(json.get("temperature")));
            ta.setMinutes(asInt(json.get("minutes")));
            ta.setCumulative(asBoolean(json.get("cumulative")));
            ta.setRuleId(asLong(json.get("ruleId")));
        }

        return a;
    }
    /**
     * @param nb
     * @return
     */
    private JsonObject notiticationIssueToJson(final NotificationIssueBean nb) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", nb.getId());
        json.addProperty("date", formatDate(nb.getDate()));
        json.addProperty("trackerEventId", nb.getTrackerEventId());
        return json;
    }
    /**
     * @param json
     * @param a
     */
    private void jsonToNotiticationIssue(final JsonObject json, final AlertBean a) {
        a.setId(asLong(json.get("id")));
        a.setDate(asDate(json.get("date")));
        a.setTrackerEventId(asLong(json.get("trackerEventId")));
    }
    /**
     * @param s
     * @return
     */
    protected JsonArray notesToJson(final List<NoteDto> notes) {
        final JsonArray array = new JsonArray();
        for (final NoteDto note : notes) {
            array.add(noteSerializer.toJson(note));
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<NoteDto> parseNotes(final JsonArray array) {
        final List<NoteDto> notes = new LinkedList<>();
        for (final JsonElement e : array) {
            notes.add(noteSerializer.parseNoteDto(e));
        }
        return notes;
    }
    /**
     * @param alerts
     * @return
     */
    private JsonArray alertRulesToJson(final List<AlertRuleBean> alerts) {
        final JsonArray array = new JsonArray();
        for (final AlertRuleBean a : alerts) {
            array.add(toJson(a));
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<AlertRuleBean> parseAlertRuleBeans(final JsonArray array) {
        final List<AlertRuleBean> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(parseAlertRuleBean(e));
        }
        return list;
    }
    /**
     * @param r
     * @return
     */
    private JsonObject toJson(final AlertRuleBean r) {
        if (r == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("type", r.getType().name());
        json.addProperty("id", r.getId());

        if (r instanceof TemperatureRuleBean) {
            final TemperatureRuleBean tr = (TemperatureRuleBean) r;
            json.addProperty("temperature", tr.getTemperature());
            json.addProperty("timeOutMinutes", tr.getTimeOutMinutes());
            json.addProperty("cumulativeFlag", tr.hasCumulativeFlag());
            json.addProperty("maxRateMinutes", tr.getMaxRateMinutes());
            json.add("correctiveActions", toJson(tr.getCorrectiveActions()));
        }

        return json;
    }
    /**
     * @param e
     * @return
     */
    private AlertRuleBean parseAlertRuleBean(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final boolean isTemperatureRule = json.has("temperature");

        final AlertRuleBean r = isTemperatureRule ? new TemperatureRuleBean() : new AlertRuleBean();
        r.setType(AlertType.valueOf(asString(json.get("type"))));
        r.setId(asLong(json.get("id")));

        if (r instanceof TemperatureRuleBean) {
            final TemperatureRuleBean tr = (TemperatureRuleBean) r;
            tr.setTemperature(asDouble(json.get("temperature")));
            tr.setTimeOutMinutes(asInt(json.get("timeOutMinutes")));
            tr.setCumulativeFlag(asBoolean(json.get("cumulativeFlag")));
            tr.setMaxRateMinutes(asInteger(json.get("maxRateMinutes")));
            tr.setCorrectiveActions(parseCorrectiveActionListBean(json.get("correctiveActions")));
        }

        return null;
    }
    /**
     * @param actions
     * @return
     */
    private JsonObject toJson(final CorrectiveActionListBean actions) {
        if (actions == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", actions.getId());
        json.addProperty("name", actions.getName());
        json.addProperty("description", actions.getDescription());

        final JsonArray array = new JsonArray();
        for (final CorrectiveAction a : actions.getActions()) {
            array.add(toJson(a));
        }
        json.add("actions", array);

        return json;
    }
    /**
     * @param jsonElement
     * @return
     */
    private CorrectiveActionListBean parseCorrectiveActionListBean(final JsonElement el) {
        if (isNull(el)) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final CorrectiveActionListBean bean = new CorrectiveActionListBean();
        bean.setId(asLong(json.get("id")));
        bean.setName(asString(json.get("name")));
        bean.setDescription(asString(json.get("description")));

        final JsonArray array = json.get("actions").getAsJsonArray();
        for (final JsonElement e : array) {
            bean.getActions().add(parseCorrectiveAction(e));
        }

        return bean;
    }
    /**
     * @param a corrective action.
     * @return JSON object.
     */
    private JsonObject toJson(final CorrectiveAction a) {
        if (a == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("action", a.getAction());
        json.addProperty("requestVerification", a.isRequestVerification());
        return json;
    }
    /**
     * @param e JSON element.
     * @return corrective action.
     */
    private CorrectiveAction parseCorrectiveAction(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final CorrectiveAction a = new CorrectiveAction();
        a.setAction(asString(json.get("action")));
        a.setRequestVerification(asBoolean(json.get("requestVerification")));
        return a;
    }
    /**
     * @param sched
     * @return
     */
    protected JsonArray createNotificationScheduleArray(final List<ListNotificationScheduleItem> sched) {
        final JsonArray arrivalNotificationSchedules = new JsonArray();
        for (final ListNotificationScheduleItem item : sched) {
            arrivalNotificationSchedules.add(toJson(item));
        }
        return arrivalNotificationSchedules;
    }
    /**
     * @param array
     * @return
     */
    private List<ListNotificationScheduleItem> parseNotificationSchedules(final JsonArray array) {
        final List<ListNotificationScheduleItem> list = new LinkedList<>();
        for (final JsonElement e : array) {
            list.add(parseListNotificationScheduleItem(e));
        }
        return list;
    }
}
