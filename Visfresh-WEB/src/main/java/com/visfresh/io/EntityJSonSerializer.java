/**
 *
 */
package com.visfresh.io;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.constants.CompanyConstants;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.constants.LocationConstants;
import com.visfresh.constants.NotificationConstants;
import com.visfresh.constants.NotificationScheduleConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.constants.TrackerEventConstants;
import com.visfresh.constants.UserConstants;
import com.visfresh.controllers.ShipmentTemplateConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureIssue;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.services.AuthToken;
import com.visfresh.services.lists.ListShipmentTemplateItem;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EntityJSonSerializer extends AbstractJsonSerializer {
    /**
     * UTC time zone.
     */
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public EntityJSonSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param token
     * @return
     */
    public JsonObject toJson(final AuthToken token) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("token", token.getToken());
        obj.addProperty("expired", formatDate(token.getExpirationTime()));
        return obj;
    }

    /**
     * @param json
     * @return
     */
    public User parseUser(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final User u = new User();
        u.setLogin(asString(json.get(UserConstants.PROPERTY_LOGIN)));
        u.setFullName(asString(json.get(UserConstants.PROPERTY_FULL_NAME)));
        u.setTimeZone(TimeZone.getTimeZone(asString(json.get(UserConstants.PROPERTY_TIME_ZONE))));
        u.setTemperatureUnits(TemperatureUnits.valueOf(asString(json.get(
                UserConstants.PROPERTY_TEMPERATURE_UNITS))));

        final JsonArray array = json.get(UserConstants.PROPERTY_ROLES).getAsJsonArray();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            u.getRoles().add(Role.valueOf(array.get(i).getAsString()));
        }

        return u;
    }
    /**
     * @param u the user.
     * @return JSON object.
     */
    public JsonObject toJson(final User u) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(UserConstants.PROPERTY_LOGIN, u.getLogin());
        obj.addProperty(UserConstants.PROPERTY_FULL_NAME, u.getFullName());

        final JsonArray roleArray = new JsonArray();
        for (final Role r : u.getRoles()) {
            roleArray.add(new JsonPrimitive(r.name()));
        }
        obj.add(UserConstants.PROPERTY_ROLES, roleArray);

        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, u.getTimeZone().getID());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, u.getTemperatureUnits().toString());

        return obj;
    }

    /**
     * @param idFieldName ID field name
     * @param id entity ID.
     * @return JSON object.
     */
    public static JsonObject idToJson(final String idFieldName, final Long id) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(idFieldName, id);
        return obj;
    }

    /**
     * @param alert alert profile.
     * @return JSON object.
     */
    public JsonObject toJson(final AlertProfile alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        //alertProfileId, alertProfileName, alertProfileDescription, highTemperature, criticalHighTemperature, lowTemperature, criticalHighTemperature, watchEnterBrightEnvironment, watchEnterDarkEnvironment, watchMovementStart
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID, alert.getId());
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME, alert.getName());
        obj.addProperty(AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION, alert.getDescription());

        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW,
                alert.isWatchBatteryLow());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT,
                alert.isWatchEnterBrightEnvironment());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT,
                alert.isWatchEnterDarkEnvironment());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START,
                alert.isWatchMovementStart());
        obj.addProperty(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP,
                alert.isWatchMovementStop());

        final JsonArray tempIssues = new JsonArray();
        obj.add("temperatureIssues", tempIssues);
        for (final TemperatureIssue issue : alert.getTemperatureIssues()) {
            tempIssues.add(toJson(issue));
        }

        return obj;
    }
    /**
     * @param alert encoded alert profile.
     * @return decoded alert profile.
     */
    public AlertProfile parseAlertProfile(final JsonObject alert) {
        final AlertProfile p = new AlertProfile();

        p.setId(asLong(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID)));
        p.setDescription(asString(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION)));
        p.setName(asString(alert.get(AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME)));

        final JsonArray tempIssues = alert.get("temperatureIssues").getAsJsonArray();
        for (final JsonElement issue : tempIssues) {
            p.getTemperatureIssues().add(parseTemperatureIssue(issue.getAsJsonObject()));
        }

        p.setWatchBatteryLow(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW)));
        p.setWatchEnterBrightEnvironment(asBoolean(alert.get(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT)));
        p.setWatchEnterDarkEnvironment(asBoolean(alert.get(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT)));
        p.setWatchMovementStart(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START)));
        p.setWatchMovementStop(asBoolean(alert.get(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP)));

        return p;
    }
    /**
     * @param issue
     * @return
     */
    public JsonObject toJson(final TemperatureIssue issue) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", issue.getId());
        obj.addProperty("type", issue.getType().toString());
        obj.addProperty("temperature", issue.getTemperature());
        obj.addProperty("timeOutMinutes", issue.getTimeOutMinutes());
        return obj;
    }
    /**
     * @param json
     * @return
     */
    public TemperatureIssue parseTemperatureIssue(final JsonObject json) {
        final TemperatureIssue issue = new TemperatureIssue();
        issue.setId(asLong(json.get("id")));
        issue.setType(AlertType.valueOf(json.get("type").getAsString()));
        issue.setTemperature(asDouble(json.get("temperature")));
        issue.setTimeOutMinutes(asInt(json.get("timeOutMinutes")));
        return issue;
    }

    /**
     * @param obj
     * @param property
     * @return
     */
    protected boolean notNull(final JsonObject obj, final String property) {
        if (!obj.has(property)) {
            return false;
        }
        final JsonElement e = obj.get(property);
        return e != null && !e.isJsonNull();
    }

    /**
     * @param location
     * @return
     */
    public JsonElement toJson(final LocationProfile location) {
        if (location == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(LocationConstants.PROPERTY_LOCATION_ID, location.getId());
        obj.addProperty(LocationConstants.PROPERTY_LOCATION_NAME, location.getName());
        obj.addProperty(LocationConstants.PROPERTY_COMPANY_NAME, location.getCompanyName());
        obj.addProperty(LocationConstants.PROPERTY_NOTES, location.getNotes());
        obj.addProperty(LocationConstants.PROPERTY_ADDRESS, location.getAddress());

        final JsonObject loc = new JsonObject();
        obj.add(LocationConstants.PROPERTY_LOCATION, loc);
        loc.addProperty(LocationConstants.PROPERTY_LAT, location.getLocation().getLatitude());
        loc.addProperty(LocationConstants.PROPERTY_LON, location.getLocation().getLongitude());

        obj.addProperty(LocationConstants.PROPERTY_RADIUS_METERS, location.getRadius());

        obj.addProperty(LocationConstants.PROPERTY_START_FLAG, location.isStart() ? "Y" : "N");
        obj.addProperty(LocationConstants.PROPERTY_INTERIM_FLAG, location.isInterim() ? "Y" : "N");
        obj.addProperty(LocationConstants.PROPERTY_END_FLAG, location.isStop() ? "Y" : "N");

        return obj;
    }
    /**
     * @param obj encoded location profile.
     * @return location profile.
     */
    public LocationProfile parseLocationProfile(final JsonObject obj) {
        final LocationProfile location = new LocationProfile();

        location.setId(asLong(obj.get(LocationConstants.PROPERTY_LOCATION_ID)));
        location.setCompanyName(asString(obj.get(LocationConstants.PROPERTY_COMPANY_NAME)));
        location.setName(asString(obj.get(LocationConstants.PROPERTY_LOCATION_NAME)));
        location.setNotes(asString(obj.get(LocationConstants.PROPERTY_NOTES)));
        location.setAddress(asString(obj.get(LocationConstants.PROPERTY_ADDRESS)));

        location.setStart("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_START_FLAG))));
        location.setInterim("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_INTERIM_FLAG))));
        location.setStop("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_END_FLAG))));

        final JsonObject loc = obj.get(LocationConstants.PROPERTY_LOCATION).getAsJsonObject();
        location.getLocation().setLatitude(loc.get(LocationConstants.PROPERTY_LAT).getAsDouble());
        location.getLocation().setLongitude(loc.get(LocationConstants.PROPERTY_LON).getAsDouble());

        location.setRadius(asInt(obj.get(LocationConstants.PROPERTY_RADIUS_METERS)));

        return location;
    }

    /**
     * @param schedule notification schedule.
     * @return JSON object.
     */
    public JsonElement toJson(final NotificationSchedule schedule) {
        if (schedule == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION,
                schedule.getDescription());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID, schedule.getId());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME, schedule.getName());

        final JsonArray array = new JsonArray();
        obj.add("schedules", array);

        for (final PersonSchedule sphw : schedule.getSchedules()) {
            array.add(toJson(sphw));
        }

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return notification schedule.
     */
    public NotificationSchedule parseNotificationSchedule(final JsonObject obj) {
        final NotificationSchedule sched = new NotificationSchedule();

        sched.setDescription(asString(obj.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION)));
        sched.setName(asString(obj.get(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME)));
        sched.setId(asLong(obj.get(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));

        final JsonArray array = obj.get("schedules").getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            sched.getSchedules().add(parsePersonSchedule(array.get(i).getAsJsonObject()));
        }

        return sched;
    }

    /**
     * @param s schedule/person/how/when
     * @return JSON object.
     */
    public JsonObject toJson(final PersonSchedule s) {
        if (s == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty("personScheduleId", s.getId());
        obj.addProperty("firstName", s.getFirstName());
        obj.addProperty("lastName", s.getLastName());
        obj.addProperty("company", s.getCompany());
        obj.addProperty("position", s.getPosition());
        obj.addProperty("emailNotification", s.getEmailNotification());
        obj.addProperty("smsNotification", s.getSmsNotification());
        obj.addProperty("pushToMobileApp", s.isPushToMobileApp());
        obj.addProperty("fromTime", s.getFromTime());
        obj.addProperty("toTime", s.getToTime());

        final JsonArray weekDays = new JsonArray();
        for (final boolean day : s.getWeekDays()) {
            weekDays.add(new JsonPrimitive(day));
        }
        obj.add("weekDays", weekDays);

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return schedule/person/how/when
     */
    public PersonSchedule parsePersonSchedule(
            final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject obj = e.getAsJsonObject();
        final PersonSchedule s = new PersonSchedule();

        s.setCompany(asString(obj.get("company")));
        s.setEmailNotification(asString(obj.get("emailNotification")));
        s.setFirstName(asString(obj.get("firstName")));
        s.setLastName(asString(obj.get("lastName")));
        s.setPosition(asString(obj.get("position")));
        s.setSmsNotification(asString(obj.get("smsNotification")));
        s.setToTime(asInt(obj.get("toTime")));
        s.setFromTime(asInt(obj.get("fromTime")));
        s.setId(asLong(obj.get("personScheduleId")));
        s.setPushToMobileApp(asBoolean(obj.get("pushToMobileApp")));

        final JsonArray weekDays = obj.get("weekDays").getAsJsonArray();
        for (int i = 0; i < weekDays.size(); i++) {
            s.getWeekDays()[i] = weekDays.get(i).getAsBoolean();
        }

        return s;
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

        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_ID, tpl.getId());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME, tpl.getName());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION, tpl.getShipmentDescription());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED, tpl.isAddDateShipped());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPPED_FROM, getId(tpl.getShippedFrom()));
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHIPPED_TO, getId(tpl.getShippedTo()));
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_DETECT_LOCATION_FOR_SHIPPED_FROM, tpl.isDetectLocationForShippedFrom());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_USE_CURRENT_TIME_FOR_DATE_SHIPPED, tpl.isUseCurrentTimeForDateShipped());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_ALERT_PROFILE_ID, getId(tpl.getAlertProfile()));
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES, tpl.getAlertSuppressionMinutes());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_MAX_TIMES_ALERT_FIRES, tpl.getMaxTimesAlertFires());
        obj.add(ShipmentTemplateConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES, getIdList(tpl.getAlertsNotificationSchedules()));
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_COMMENTS_FOR_RECEIVER, tpl.getCommentsForReceiver());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM, tpl.getArrivalNotificationWithinKm());
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS, tpl.isExcludeNotificationsIfNoAlerts());
        obj.add(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES, getIdList(tpl.getArrivalNotificationSchedules()));
        obj.addProperty(ShipmentTemplateConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES, tpl.getShutdownDeviceTimeOut());

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return shipment template.
     */
    public ShipmentTemplate parseShipmentTemplate(final JsonObject obj) {
        final ShipmentTemplate tpl = new ShipmentTemplate();

        parseShipmentBase(obj, tpl);

        tpl.setId(asLong(obj.get(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_ID)));
        tpl.setName(asString(obj.get(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME)));
        tpl.setShipmentDescription(asString(obj.get(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION)));
        tpl.setAddDateShipped(asBoolean(obj.get(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED)));
        tpl.setUseCurrentTimeForDateShipped(asBoolean(obj.get(ShipmentTemplateConstants.PROPERTY_USE_CURRENT_TIME_FOR_DATE_SHIPPED)));
        tpl.setDetectLocationForShippedFrom(asBoolean(obj.get(ShipmentTemplateConstants.PROPERTY_DETECT_LOCATION_FOR_SHIPPED_FROM)));

        return tpl;
    }
    /**
     * @param obj
     * @param shp
     */
    private void parseShipmentBase(final JsonObject obj, final ShipmentBase shp) {
        shp.setAlertSuppressionMinutes(asInt(obj.get(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES)));
        shp.setAlertProfile(resolveAlertProfile(asLong(obj.get(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID))));
        shp.getAlertsNotificationSchedules().addAll(resolveNotificationSchedules(obj.get(
                ShipmentConstants.PROPERTY_ALERTS_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setArrivalNotificationWithinKm(asInt(obj.get(
                ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM)));
        shp.getArrivalNotificationSchedules().addAll(resolveNotificationSchedules(
                obj.get(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_SCHEDULES).getAsJsonArray()));
        shp.setExcludeNotificationsIfNoAlerts(asBoolean(obj.get(
                ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)));
        shp.setShippedFrom(resolveLocationProfile(asLong(obj.get(ShipmentConstants.PROPERTY_SHIPPED_FROM))));
        shp.setShippedTo(resolveLocationProfile(asLong(obj.get(ShipmentConstants.PROPERTY_SHIPPED_TO))));
        shp.setShutdownDeviceTimeOut(asInt(obj.get(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES)));
        shp.setMaxTimesAlertFires(asInt(obj.get(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES)));
        shp.setCommentsForReceiver(asString(obj.get(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER)));
    }
    /**
     * @param json JSON object.
     * @return device.
     */
    public Device parseDevice(final JsonObject json) {
        final Device tr = new Device();
        tr.setSn(asString(json.get(DeviceConstants.PROPERTY_SN)));
        tr.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        tr.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        tr.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        return tr;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonElement toJson(final Device d) {
        if (d == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        return obj;
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
        s.setDevice(resolveDevice(asString(json.get(ShipmentConstants.PROPERTY_DEVICE_IMEI))));

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
        obj.addProperty(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES, s.getMaxTimesAlertFires());
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
    public Notification parseNotification(final JsonObject json) {
        final Notification n = new Notification();
        n.setId(asLong(json.get(NotificationConstants.PROPERTY_ID)));
        n.setType(NotificationType.valueOf(asString(json.get(NotificationConstants.PROPERTY_TYPE))));

        switch (n.getType()) {
            case Alert:
                n.setIssue(parseAlert(json.get("issue").getAsJsonObject()));
                break;
            case Arrival:
                n.setIssue(parseArrival(json.get("issue").getAsJsonObject()));
                break;
        }

        return n;
    }
    /**
     * @param n notification.
     * @return JSON object.
     */
    public JsonObject toJson(final Notification n) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(NotificationConstants.PROPERTY_ID, n.getId());
        obj.addProperty(NotificationConstants.PROPERTY_TYPE, n.getType().name());

        final Object issue = n.getIssue();
        if (issue instanceof Alert) {
            obj.add("issue", toJson((Alert) issue));
        } else if (issue instanceof Arrival) {
            obj.add("issue", toJson((Arrival) issue));
        } else {
            throw new IllegalArgumentException("Unexpected alert issue " + issue);
        }
        return obj;
    }
    /**
     * @param asJsonObject
     * @return
     */
    public Arrival parseArrival(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        final JsonObject obj = e.getAsJsonObject();

        final Arrival a = new Arrival();
        a.setDate(asDate(obj.get("date")));
        a.setDevice(resolveDevice(asString(obj.get("device"))));
        a.setShipment(resolveShipment(asLong(obj.get("shipment"))));
        a.setId(asLong(obj.get("id")));
        a.setNumberOfMettersOfArrival(asInt(obj.get("numberOfMetersOfArrival")));
        return a;
    }
    /**
     * @param arrival
     * @return JSON object.
     */
    public JsonObject toJson(final Arrival arrival) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", arrival.getId());
        json.addProperty("numberOfMetersOfArrival", arrival.getNumberOfMettersOfArrival());
        json.addProperty("date", formatDate(arrival.getDate()));
        json.addProperty("device", arrival.getDevice().getId());
        json.addProperty("shipment", arrival.getShipment().getId());
        return json;
    }
    /**
     * @param json
     * @return
     */
    public Alert parseAlert(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final AlertType type = AlertType.valueOf(json.get("type").getAsString());
        Alert alert;
        switch (type) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(asDouble(json.get("temperature")));
                ta.setMinutes(asInt(json.get("minutes")));
                alert = ta;
                break;
            default:
                alert = new Alert();
        }

        alert.setDate(asDate(json.get("date")));
        alert.setDevice(resolveDevice(asString(json.get("device"))));
        alert.setShipment(resolveShipment(asLong(json.get("shipment"))));
        alert.setId(asLong(json.get("id")));
        alert.setType(type);

        return alert;
    }
    /**
     * @param alert
     * @return JSON object
     */
    public JsonObject toJson(final Alert alert) {
        final JsonObject json = new JsonObject();

        //add common alert properties
        json.addProperty("id", alert.getId());
        json.addProperty("date", formatDate(alert.getDate()));
        json.addProperty("device", alert.getDevice().getId());
        json.addProperty("shipment", alert.getShipment().getId());
        json.addProperty("type", alert.getType().name());

        switch (alert.getType()) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = (TemperatureAlert) alert;
                json.addProperty("temperature", ta.getTemperature());
                json.addProperty("minutes", ta.getMinutes());
                break;

                default:
                    //nothing
                    break;
        }

        return json;
    }

    /**
     * @param e tracker event.
     * @return JSON object.
     */
    public JsonObject toJson(final TrackerEvent e) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(TrackerEventConstants.PROPERTY_BATTERY, e.getBattery());
        obj.addProperty(TrackerEventConstants.PROPERTY_ID, e.getId());
        obj.addProperty(TrackerEventConstants.PROPERTY_TEMPERATURE, e.getTemperature());
        obj.addProperty(TrackerEventConstants.PROPERTY_TIME, formatDate(e.getTime()));
        obj.addProperty(TrackerEventConstants.PROPERTY_TYPE, e.getType());
        obj.addProperty(TrackerEventConstants.PROPERTY_LATITUDE, e.getLatitude());
        obj.addProperty(TrackerEventConstants.PROPERTY_LONGITUDE, e.getLongitude());
        return obj;
    }
    public TrackerEvent parseTrackerEvent(final JsonObject json) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(asInt(json.get(TrackerEventConstants.PROPERTY_BATTERY)));
        e.setId(asLong(json.get(TrackerEventConstants.PROPERTY_ID)));
        e.setTemperature(asDouble(json.get(TrackerEventConstants.PROPERTY_TEMPERATURE)));
        e.setTime(asDate(json.get(TrackerEventConstants.PROPERTY_TIME)));
        e.setType(asString(json.get(TrackerEventConstants.PROPERTY_TYPE)));
        e.setLatitude(asDouble(json.get(TrackerEventConstants.PROPERTY_LATITUDE)));
        e.setLongitude(asDouble(json.get(TrackerEventConstants.PROPERTY_LONGITUDE)));
        return e;
    }
    /**
     * @param req
     * @return
     */
    public JsonObject toJson(final SavePersonScheduleRequest req) {
        if (req == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID,
                req.getNotificationScheduleId());
        json.add("schedule", toJson(req.getSchedule()));
        return json;
    }
    public SavePersonScheduleRequest parseSavePersonScheduleRequest(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final SavePersonScheduleRequest req = new SavePersonScheduleRequest();
        req.setNotificationScheduleId(asLong(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));
        req.setSchedule(parsePersonSchedule(json.get("schedule").getAsJsonObject()));
        return req;
    }
    /**
     * @param json JSON object.
     * @return device command.
     */
    public DeviceCommand parseDeviceCommand(final JsonObject json) {
        final DeviceCommand dc = new DeviceCommand();
        dc.setDevice(resolveDevice(asString(json.get("device"))));
        dc.setCommand(asString(json.get("command")));
        return dc;
    }
    public JsonElement toJson(final DeviceCommand cmd) {
        if (cmd == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("device", cmd.getDevice().getId());
        obj.addProperty("command", cmd.getCommand());
        return obj;
    }
    /**
     * @param profile user profile.
     * @return User pforile as JSON object.
     */
    public JsonElement toJson(final UserProfile profile) {
        if (profile == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject obj = new JsonObject();

        final JsonArray array = new JsonArray();
        for (final Shipment s : profile.getShipments()) {
            array.add(new JsonPrimitive(s.getId()));
        }

        obj.add("shipments", array);
        return obj;
    }
    public UserProfile parseUserProfile(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final UserProfile p = new UserProfile();
        final JsonArray array = ((JsonObject) e).get("shipments").getAsJsonArray();
        for (final JsonElement id : array) {
            p.getShipments().add(resolveShipment(id.getAsLong()));
        }
        return p;
    }
    public DeviceDcsNativeEvent parseDeviceDcsNativeEvent(final JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final JsonObject obj = json.getAsJsonObject();

        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setBattery(asInt(obj.get(TrackerEventConstants.PROPERTY_BATTERY)));
        e.setTemperature(asDouble(obj.get(TrackerEventConstants.PROPERTY_TEMPERATURE)));
        try {
            e.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(asString(obj.get("time"))));
        } catch (final ParseException e1) {
            e1.printStackTrace();
        }
        e.setType(asString(obj.get("type")));
        e.getLocation().setLatitude(asDouble(obj.get(TrackerEventConstants.PROPERTY_LATITUDE)));
        e.getLocation().setLongitude(asDouble(obj.get(TrackerEventConstants.PROPERTY_LONGITUDE)));
        e.setImei(asString(obj.get("imei")));

        return e;
    }
    public JsonElement toJson(final DeviceDcsNativeEvent e) {
        if (e == null) {
            return JsonNull.INSTANCE;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(UTC);

        final JsonObject obj = new JsonObject();
        obj.addProperty(TrackerEventConstants.PROPERTY_BATTERY, e.getBattery());
        obj.addProperty(TrackerEventConstants.PROPERTY_TEMPERATURE, e.getTemperature());
        obj.addProperty("time", sdf.format(e.getTime()));
        obj.addProperty("type", e.getType());
        obj.addProperty(TrackerEventConstants.PROPERTY_LATITUDE, e.getLocation().getLatitude());
        obj.addProperty(TrackerEventConstants.PROPERTY_LONGITUDE, e.getLocation().getLongitude());
        obj.addProperty("imei", e.getImei());
        return obj;
    }
    /**
     * @param e JSON element.
     * @return create user request.
     */
    public CreateUserRequest parseCreateUserRequest(final JsonElement e) {
        if (e == null) {
            return null;
        }
        final JsonObject obj = e.getAsJsonObject();
        final CreateUserRequest req = new CreateUserRequest();
        req.setUser(parseUser(obj.get("user").getAsJsonObject()));
        req.setCompany(resolveCompany(obj.get("company").getAsLong()));
        req.setPassword(obj.get("password").getAsString());
        return req;
    }
    /**
     * @param req create user request.
     * @return JSON object.
     */
    public JsonElement toJson(final CreateUserRequest req) {
        if (req == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject obj = new JsonObject();
        obj.add("user", toJson(req.getUser()));
        obj.addProperty("password", req.getPassword());
        obj.addProperty("company", req.getCompany().getId());
        return obj;
    }

    /**
     * @param json
     * @return
     */
    public Company parseCompany(final JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        final JsonObject obj = json.getAsJsonObject();
        final Company c = new Company();
        c.setDescription(asString(obj.get(CompanyConstants.PROPERTY_DESCRIPTION)));
        c.setId(asLong(obj.get(CompanyConstants.PROPERTY_ID)));
        c.setName(asString(obj.get(CompanyConstants.PROPERTY_NAME)));
        return c;
    }
    public JsonElement toJson(final Company c) {
        if (c == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(CompanyConstants.PROPERTY_ID, c.getId());
        obj.addProperty(CompanyConstants.PROPERTY_NAME, c.getName());
        obj.addProperty(CompanyConstants.PROPERTY_DESCRIPTION, c.getDescription());
        return obj;
    }
    /**
     * @param req
     * @return
     */
    public JsonObject toJson(final UpdateUserDetailsRequest req) {
        if (req == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(UserConstants.PROPERTY_FULL_NAME, req.getFullName());
        obj.addProperty("password", req.getPassword());
        obj.addProperty("user", req.getUser());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, req.getTemperatureUnits().toString());
        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, req.getTimeZone().getID());
        return obj;
    }
    public UpdateUserDetailsRequest parseUpdateUserDetailsRequest(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFullName(asString(json.get(UserConstants.PROPERTY_FULL_NAME)));
        req.setPassword(asString(json.get("password")));
        req.setUser(asString(json.get("user")));
        if (json.has(UserConstants.PROPERTY_TEMPERATURE_UNITS)) {
            req.setTemperatureUnits(TemperatureUnits.valueOf(
                    json.get(UserConstants.PROPERTY_TEMPERATURE_UNITS).getAsString()));
        }
        if (json.has(UserConstants.PROPERTY_TIME_ZONE)) {
            req.setTimeZone(TimeZone.getTimeZone(json.get(UserConstants.PROPERTY_TIME_ZONE).getAsString()));
        }
        return req;
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
        json.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_TEMPLATE_ID, item.getShipmentTemplateId());

        json.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME, item.getShipmentTemplateName());
        json.addProperty(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION, item.getShipmentDescription());

        json.addProperty(ShipmentConstants.PROPERTY_SHIPPED_FROM, item.getShippedFrom());
        json.addProperty(ShipmentConstants.PROPERTY_SHIPPED_FROM_LOCATION_NAME, item.getShippedFromLocationName());

        json.addProperty(ShipmentConstants.PROPERTY_SHIPPED_TO, item.getShippedTo());
        json.addProperty(ShipmentConstants.PROPERTY_SHIPPED_TO_LOCATION_NAME, item.getShippedToLocationName());

        json.addProperty(ShipmentConstants.PROPERTY_ALERT_PROFILE, item.getAlertProfile());
        json.addProperty(ShipmentConstants.PROPERTY_ALERT_PROFILE_NAME, item.getAlertProfileName());
        return json;
    }
    public ListShipmentTemplateItem parseListShipmentTemplateItem(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final ListShipmentTemplateItem item = new ListShipmentTemplateItem();
        item.setShipmentTemplateId(asLong(json.get(ShipmentConstants.PROPERTY_SHIPMENT_TEMPLATE_ID)));

        item.setShipmentTemplateName(asString(json.get(ShipmentConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME)));
        item.setShipmentDescription(asString(json.get(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION)));

        item.setShippedFrom(asLong(json.get(ShipmentConstants.PROPERTY_SHIPPED_FROM)));
        item.setShippedFromLocationName(asString(json.get(ShipmentConstants.PROPERTY_SHIPPED_FROM_LOCATION_NAME)));

        item.setShippedTo(asLong(json.get(ShipmentConstants.PROPERTY_SHIPPED_TO)));
        item.setShippedToLocationName(asString(json.get(ShipmentConstants.PROPERTY_SHIPPED_TO_LOCATION_NAME)));

        item.setAlertProfile(asLong(json.get(ShipmentConstants.PROPERTY_ALERT_PROFILE)));
        item.setAlertProfileName(asString(json.get(ShipmentConstants.PROPERTY_ALERT_PROFILE_NAME)));

        return item;
    }

    /**
     * @param item
     * @return
     */
    public JsonObject toJson(final NotificationScheduleListItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID,
                item.getNotificationScheduleId());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME,
                item.getNotificationScheduleName());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION,
                item.getNotificationScheduleDescription());
        obj.addProperty("peopleToNotify", item.getPeopleToNotify());
        return obj;
    }
    public NotificationScheduleListItem parseNotificationScheduleListItem(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final NotificationScheduleListItem item = new NotificationScheduleListItem();
        item.setNotificationScheduleDescription(asString(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION)));
        item.setNotificationScheduleId(asLong(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));
        item.setNotificationScheduleName(asString(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME)));
        item.setPeopleToNotify(asString(json.get("peopleToNotify")));
        return item;
    }
    /**
     * @param array
     * @return
     */
    private List<NotificationSchedule> resolveNotificationSchedules(final JsonArray array) {
        final List<NotificationSchedule> list = new LinkedList<NotificationSchedule>();
        for (final JsonElement e : array) {
            list.add(resolveNotificationSchedule(e.getAsLong()));
        }
        return list;
    }
    /**
     * @param id notification schedule ID.
     * @return notification schedule.
     */
    protected NotificationSchedule resolveNotificationSchedule(final Long id) {
        return id == null ? null : getReferenceResolver().getNotificationSchedule(id);
    }
    /**
     * @param id alert profile ID.
     * @return alert profile.
     */
    protected AlertProfile resolveAlertProfile(final Long id) {
        return id == null ? null : getReferenceResolver().getAlertProfile(id);
    }
    /**
     * @param id location profile ID.
     * @return location profile.
     */
    protected LocationProfile resolveLocationProfile(final Long id) {
        return id == null ? null : getReferenceResolver().getLocationProfile(id);
    }
    /**
     * @param id device ID.
     * @return device.
     */
    protected Device resolveDevice(final String id) {
        return id == null ? null : getReferenceResolver().getDevice(id);
    }
    /**
     * @param id shipment ID.
     * @return shipment.
     */
    private Shipment resolveShipment(final Long id) {
        return id == null ? null : getReferenceResolver().getShipment(id);
    }
    /**
     * @param id company ID.
     * @return company.
     */
    private Company resolveCompany(final Long id) {
        return id == null ? null : getReferenceResolver().getCompany(id);
    }
    /**
     * @param e
     * @return
     */
    private <IDD extends Serializable & Comparable<IDD>> IDD getId(final EntityWithId<IDD> e) {
        return e == null ? null : (IDD) e.getId();
    }
    /**
     * @param entities list of entity.
     * @return JSON array with entity IDs.
     */
    private <E extends EntityWithId<Long>> JsonArray getIdList(final List<E> entities) {
        final JsonArray array= new JsonArray();
        for (final E e : entities) {
            array.add(new JsonPrimitive(e.getId()));
        }
        return array;
    }
    /**
     * @param errorCode error code.
     * @param e error.
     * @return encoded to JSON object error.
     */
    public static JsonObject createErrorStatus(final int errorCode, final Throwable e) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("code", errorCode);
        obj.addProperty("message", e.getMessage() == null ? e.toString() : e.getMessage());
        return obj;
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
