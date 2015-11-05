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
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EntityJSonSerializer extends AbstractJsonSerializer {
    /**
     *
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
        u.setLogin(asString(json.get("login")));
        u.setFullName(asString(json.get("fullName")));
        u.setTimeZone(TimeZone.getTimeZone(asString(json.get("timeZone"))));
        u.setTemperatureUnits(TemperatureUnits.valueOf(asString(json.get("temperatureUnits"))));

        final JsonArray array = json.get("roles").getAsJsonArray();
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
        obj.addProperty("login", u.getLogin());
        obj.addProperty("fullName", u.getFullName());

        final JsonArray roleArray = new JsonArray();
        for (final Role r : u.getRoles()) {
            roleArray.add(new JsonPrimitive(r.name()));
        }
        obj.add("roles", roleArray);

        obj.addProperty("timeZone", u.getTimeZone().getID());
        obj.addProperty("temperatureUnits", u.getTemperatureUnits().toString());

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
    public JsonElement toJson(final AlertProfile alert) {
        if (alert == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        //alertProfileId, alertProfileName, alertProfileDescription, highTemperature, criticalHighTemperature, lowTemperature, criticalHighTemperature, watchEnterBrightEnvironment, watchEnterDarkEnvironment, watchMovementStart
        obj.addProperty("alertProfileId", alert.getId());
        obj.addProperty("alertProfileName", alert.getName());
        obj.addProperty("alertProfileDescription", alert.getDescription());

        obj.addProperty("highTemperature", alert.getHighTemperature());
        obj.addProperty("highTemperatureMinutes", alert.getHighTemperatureForMoreThen());
        if (alert.getHighTemperature2() != null) {
            obj.addProperty("highTemperature2", alert.getHighTemperature2());
            obj.addProperty("highTemperatureMinutes2", alert.getHighTemperatureForMoreThen2());
        }

        obj.addProperty("criticalHighTemperature", alert.getCriticalHighTemperature());
        obj.addProperty("criticalHighTemperatureMinutes", alert.getCriticalHighTemperatureForMoreThen());
        if (alert.getCriticalHighTemperature2() != null) {
            obj.addProperty("criticalHighTemperature2", alert.getCriticalHighTemperature2());
            obj.addProperty("criticalHighTemperatureMinutes2", alert.getCriticalHighTemperatureForMoreThen2());
        }

        obj.addProperty("lowTemperature", alert.getLowTemperature());
        obj.addProperty("lowTemperatureMinutes", alert.getLowTemperatureForMoreThen());
        if (alert.getLowTemperature2() != null) {
            obj.addProperty("lowTemperature2", alert.getLowTemperature2());
            obj.addProperty("lowTemperatureMinutes2", alert.getLowTemperatureForMoreThen2());
        }

        obj.addProperty("criticalLowTemperature", alert.getCriticalLowTemperature());
        obj.addProperty("criticalLowTemperatureMinutes", alert.getCriticalLowTemperatureForMoreThen());
        if (alert.getCriticalLowTemperature2() != null) {
            obj.addProperty("criticalLowTemperature2", alert.getCriticalLowTemperature2());
            obj.addProperty("criticalLowTemperatureMinutes2", alert.getCriticalLowTemperatureForMoreThen2());
        }

        obj.addProperty("watchBatteryLow", alert.isWatchBatteryLow());
        obj.addProperty("watchEnterBrightEnvironment", alert.isWatchEnterBrightEnvironment());
        obj.addProperty("watchEnterDarkEnvironment", alert.isWatchEnterDarkEnvironment());
        obj.addProperty("watchMovementStart", alert.isWatchMovementStart());
        obj.addProperty("watchMovementStop", alert.isWatchMovementStop());

        return obj;
    }
    /**
     * @param alert encoded alert profile.
     * @return decoded alert profile.
     */
    public AlertProfile parseAlertProfile(final JsonObject alert) {
        final AlertProfile p = new AlertProfile();

        p.setId(asLong(alert.get("alertProfileId")));
        p.setDescription(asString(alert.get("alertProfileDescription")));
        p.setName(asString(alert.get("alertProfileName")));

        p.setCriticalHighTemperature(asDouble(alert.get("criticalHighTemperature")));
        p.setCriticalHighTemperatureForMoreThen(asInt(alert.get("criticalHighTemperatureMinutes")));
        if (alert.has("criticalHighTemperature2")) {
            p.setCriticalHighTemperature2(asDouble(alert.get("criticalHighTemperature2")));
            p.setCriticalHighTemperatureForMoreThen2(asInt(alert.get("criticalHighTemperatureMinutes2")));
        }

        p.setCriticalLowTemperature(asDouble(alert.get("criticalLowTemperature")));
        p.setCriticalLowTemperatureForMoreThen(asInt(alert.get("criticalLowTemperatureMinutes")));
        if (alert.has("criticalLowTemperature2")) {
            p.setCriticalLowTemperature2(asDouble(alert.get("criticalLowTemperature2")));
            p.setCriticalLowTemperatureForMoreThen2(asInt(alert.get("criticalLowTemperatureMinutes2")));
        }

        p.setHighTemperature(asDouble(alert.get("highTemperature")));
        p.setHighTemperatureForMoreThen(asInt(alert.get("highTemperatureMinutes")));
        if (alert.has("highTemperature2")) {
            p.setHighTemperature2(asDouble(alert.get("highTemperature2")));
            p.setHighTemperatureForMoreThen2(asInt(alert.get("highTemperatureMinutes2")));
        }

        p.setLowTemperature(asDouble(alert.get("lowTemperature")));
        p.setLowTemperatureForMoreThen(asInt(alert.get("lowTemperatureMinutes")));
        if (alert.has("lowTemperature2")) {
            p.setLowTemperature2(asDouble(alert.get("lowTemperature2")));
            p.setLowTemperatureForMoreThen2(asInt(alert.get("lowTemperatureMinutes2")));
        }

        p.setWatchBatteryLow(asBoolean(alert.get("watchBatteryLow")));
        p.setWatchEnterBrightEnvironment(asBoolean(alert.get("watchEnterBrightEnvironment")));
        p.setWatchEnterDarkEnvironment(asBoolean(alert.get("watchEnterDarkEnvironment")));
        p.setWatchMovementStart(asBoolean(alert.get("watchMovementStart")));
        p.setWatchMovementStop(asBoolean(alert.get("watchMovementStop")));

        return p;
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

        obj.addProperty("locationId", location.getId());
        obj.addProperty("locationName", location.getName());
        obj.addProperty("companyName", location.getCompanyName());
        obj.addProperty("notes", location.getNotes());
        obj.addProperty("address", location.getAddress());

        final JsonObject loc = new JsonObject();
        obj.add("location", loc);
        loc.addProperty("lat", location.getLocation().getLatitude());
        loc.addProperty("lon", location.getLocation().getLongitude());

        obj.addProperty("radiusMeters", location.getRadius());

        obj.addProperty("startFlag", location.isStart() ? "Y" : "N");
        obj.addProperty("interimFlag", location.isInterim() ? "Y" : "N");
        obj.addProperty("endFlag", location.isStop() ? "Y" : "N");

        return obj;
    }
    /**
     * @param obj encoded location profile.
     * @return location profile.
     */
    public LocationProfile parseLocationProfile(final JsonObject obj) {
        final LocationProfile location = new LocationProfile();

        location.setId(asLong(obj.get("locationId")));
        location.setCompanyName(asString(obj.get("companyName")));
        location.setName(asString(obj.get("locationName")));
        location.setNotes(asString(obj.get("notes")));
        location.setAddress(asString(obj.get("address")));

        location.setStart("Y".equalsIgnoreCase(asString(obj.get("startFlag"))));
        location.setInterim("Y".equalsIgnoreCase(asString(obj.get("interimFlag"))));
        location.setStop("Y".equalsIgnoreCase(asString(obj.get("endFlag"))));

        final JsonObject loc = obj.get("location").getAsJsonObject();
        location.getLocation().setLatitude(loc.get("lat").getAsDouble());
        location.getLocation().setLongitude(loc.get("lon").getAsDouble());

        location.setRadius(asInt(obj.get("radiusMeters")));

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

        obj.addProperty("notificationScheduleDescription", schedule.getDescription());
        obj.addProperty("notificationScheduleId", schedule.getId());
        obj.addProperty("notificationScheduleName", schedule.getName());

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

        sched.setDescription(asString(obj.get("notificationScheduleDescription")));
        sched.setName(asString(obj.get("notificationScheduleName")));
        sched.setId(asLong(obj.get("notificationScheduleId")));

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

        obj.addProperty("shipmentTemplateId", tpl.getId());
        obj.addProperty("shipmentTemplateName", tpl.getName());
        obj.addProperty("shipmentDescription", tpl.getShipmentDescription());
        obj.addProperty("addDateShipped", tpl.isAddDateShipped());
        obj.addProperty("shippedFrom", getId(tpl.getShippedFrom()));
        obj.addProperty("shippedTo", getId(tpl.getShippedTo()));
        obj.addProperty("detectLocationForShippedFrom", tpl.isDetectLocationForShippedFrom());
        obj.addProperty("useCurrentTimeForDateShipped", tpl.isUseCurrentTimeForDateShipped());
        obj.addProperty("alertProfileId", getId(tpl.getAlertProfile()));
        obj.addProperty("alertSuppressionMinutes", tpl.getAlertSuppressionMinutes());
        obj.addProperty("maxTimesAlertFires", tpl.getMaxTimesAlertFires());
        obj.add("alertsNotificationSchedules", getIdList(tpl.getAlertsNotificationSchedules()));
        obj.addProperty("arrivalNotificationWithinKm", tpl.getArrivalNotificationWithinKm());
        obj.addProperty("excludeNotificationsIfNoAlerts", tpl.isExcludeNotificationsIfNoAlerts());
        obj.add("arrivalNotificationSchedules", getIdList(tpl.getArrivalNotificationSchedules()));
        obj.addProperty("shutdownDeviceAfterMinutes", tpl.getShutdownDeviceTimeOut());

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return shipment template.
     */
    public ShipmentTemplate parseShipmentTemplate(final JsonObject obj) {
        final ShipmentTemplate tpl = new ShipmentTemplate();

        parseShipmentBase(obj, tpl);

        tpl.setId(asLong(obj.get("shipmentTemplateId")));
        tpl.setName(asString(obj.get("shipmentTemplateName")));
        tpl.setShipmentDescription(asString(obj.get("shipmentDescription")));
        tpl.setAddDateShipped(asBoolean(obj.get("addDateShipped")));
        tpl.setUseCurrentTimeForDateShipped(asBoolean(obj.get("useCurrentTimeForDateShipped")));
        tpl.setDetectLocationForShippedFrom(asBoolean(obj.get("detectLocationForShippedFrom")));

        return tpl;
    }
    /**
     * @param obj
     * @param shp
     */
    private void parseShipmentBase(final JsonObject obj, final ShipmentBase shp) {
        shp.setAlertSuppressionMinutes(asInt(obj.get("alertSuppressionMinutes")));
        shp.setAlertProfile(resolveAlertProfile(asLong(obj.get("alertProfileId"))));
        shp.getAlertsNotificationSchedules().addAll(
                resolveNotificationSchedules(obj.get("alertsNotificationSchedules").getAsJsonArray()));
        shp.setArrivalNotificationWithinKm(asInt(obj.get("arrivalNotificationWithinKm")));
        shp.getArrivalNotificationSchedules().addAll(
                resolveNotificationSchedules(obj.get("arrivalNotificationSchedules").getAsJsonArray()));
        shp.setExcludeNotificationsIfNoAlerts(asBoolean(obj.get("excludeNotificationsIfNoAlerts")));
        shp.setShippedFrom(resolveLocationProfile(asLong(obj.get("shippedFrom"))));
        shp.setShippedTo(resolveLocationProfile(asLong(obj.get("shippedTo"))));
        shp.setShutdownDeviceTimeOut(asInt(obj.get("shutdownDeviceAfterMinutes")));
        shp.setMaxTimesAlertFires(asInt(obj.get("maxTimesAlertFires")));
    }
    /**
     * @param json JSON object.
     * @return device.
     */
    public Device parseDevice(final JsonObject json) {
        final Device tr = new Device();
        tr.setSn(asString(json.get("sn")));
        tr.setImei(asString(json.get("imei")));
        tr.setName(asString(json.get("name")));
        tr.setDescription(asString(json.get("description")));
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
        obj.addProperty("description", d.getDescription());
        obj.addProperty("imei", d.getImei());
        obj.addProperty("name", d.getName());
        obj.addProperty("sn", d.getSn());
        return obj;
    }
    public Shipment parseShipment(final JsonObject json) {
        final Shipment s = new Shipment();
        parseShipmentBase(json, s);

        s.setAssetType(asString(json.get("assetType")));
        s.setId(asLong(json.get("shipmentId")));
        s.setName(asString(json.get("deviceName")));
        s.setShipmentDescription(asString(json.get("shipmentDescription")));
        s.setPalletId(asString(json.get("palletId")));
        s.setAssetNum(asString(json.get("assetNum")));
        s.setTripCount(asInt(json.get("tripCount")));
        s.setPoNum(asInt(json.get("poNum")));
        s.setShipmentDate(asDate(json.get("shipmentDate")));
        s.getCustomFields().putAll(parseStringMap(json.get("customFields")));
        s.setStatus(ShipmentStatus.valueOf(json.get("status").getAsString()));
        s.setDevice(resolveDevice(asString(json.get("deviceSN"))));

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

        obj.addProperty("alertSuppressionMinutes", s.getAlertSuppressionMinutes());
        obj.addProperty("alertProfileId", getId(s.getAlertProfile()));
        obj.add("alertsNotificationSchedules", getIdList(s.getAlertsNotificationSchedules()));
        obj.addProperty("arrivalNotificationWithinKm", s.getArrivalNotificationWithinKm());
        obj.add("arrivalNotificationSchedules", getIdList(s.getArrivalNotificationSchedules()));
        obj.addProperty("assetType", s.getAssetType());
        obj.addProperty("excludeNotificationsIfNoAlerts", s.isExcludeNotificationsIfNoAlerts());
        obj.addProperty("shippedFrom", getId(s.getShippedFrom()));
        obj.addProperty("shippedTo", getId(s.getShippedTo()));
        obj.addProperty("shutdownDeviceAfterMinutes", s.getShutdownDeviceTimeOut());
        obj.addProperty("maxTimesAlertFires", s.getMaxTimesAlertFires());

        obj.addProperty("deviceName", s.getName());
        obj.addProperty("shipmentDescription", s.getShipmentDescription());
        obj.addProperty("shipmentId", s.getId());
        obj.addProperty("palletId", s.getPalletId());
        obj.addProperty("tripCount", s.getTripCount());
        obj.addProperty("poNum", s.getPoNum());
        obj.addProperty("assetNum", s.getAssetNum());
        obj.addProperty("shipmentDate", formatDate(s.getShipmentDate()));
        obj.add("customFields", toJson(s.getCustomFields()));
        obj.addProperty("status", s.getStatus().name());
        obj.addProperty("deviceSN", s.getDevice() == null ? null : s.getDevice().getId());

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
        return obj;
    }

    public SaveShipmentResponse parseSaveShipmentResponse(final JsonObject json) {
        final SaveShipmentResponse resp = new SaveShipmentResponse();
        resp.setShipmentId(asLong(json.get("shipmentId")));
        resp.setTemplateId(asLong(json.get("templateId")));
        return resp;
    }
    /**
     * @param resp save shipment response.
     * @return JSON object.
     */
    public JsonObject toJson(final SaveShipmentResponse resp) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("shipmentId", resp.getShipmentId());
        obj.addProperty("templateId", resp.getTemplateId());
        return obj;
    }
    public Notification parseNotification(final JsonObject json) {
        final Notification n = new Notification();
        n.setId(asLong(json.get("id")));
        n.setType(NotificationType.valueOf(asString(json.get("type"))));

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
        obj.addProperty("id", n.getId());
        obj.addProperty("type", n.getType().name());

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
        obj.addProperty("battery", e.getBattery());
        obj.addProperty("id", e.getId());
        obj.addProperty("temperature", e.getTemperature());
        obj.addProperty("time", formatDate(e.getTime()));
        obj.addProperty("type", e.getType());
        obj.addProperty("latitude", e.getLatitude());
        obj.addProperty("longitude", e.getLongitude());
        return obj;
    }
    public TrackerEvent parseTrackerEvent(final JsonObject json) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(asInt(json.get("battery")));
        e.setId(asLong(json.get("id")));
        e.setTemperature(asDouble(json.get("temperature")));
        e.setTime(asDate(json.get("time")));
        e.setType(asString(json.get("type")));
        e.setLatitude(asDouble(json.get("latitude")));
        e.setLongitude(asDouble(json.get("longitude")));
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
        json.addProperty("notificationScheduleId", req.getNotificationScheduleId());
        json.add("schedule", toJson(req.getSchedule()));
        return json;
    }
    public SavePersonScheduleRequest parseSavePersonScheduleRequest(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final SavePersonScheduleRequest req = new SavePersonScheduleRequest();
        req.setNotificationScheduleId(asLong(json.get("notificationScheduleId")));
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
        e.setBattery(asInt(obj.get("battery")));
        e.setTemperature(asDouble(obj.get("temperature")));
        try {
            e.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(asString(obj.get("time"))));
        } catch (final ParseException e1) {
            e1.printStackTrace();
        }
        e.setType(asString(obj.get("type")));
        e.getLocation().setLatitude(asDouble(obj.get("latitude")));
        e.getLocation().setLongitude(asDouble(obj.get("longitude")));
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
        obj.addProperty("battery", e.getBattery());
        obj.addProperty("temperature", e.getTemperature());
        obj.addProperty("time", sdf.format(e.getTime()));
        obj.addProperty("type", e.getType());
        obj.addProperty("latitude", e.getLocation().getLatitude());
        obj.addProperty("longitude", e.getLocation().getLongitude());
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
        c.setDescription(asString(obj.get("description")));
        c.setId(asLong(obj.get("id")));
        c.setName(asString(obj.get("name")));
        return c;
    }
    public JsonElement toJson(final Company c) {
        if (c == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("id", c.getId());
        obj.addProperty("name", c.getName());
        obj.addProperty("description", c.getDescription());
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
        obj.addProperty("fullName", req.getFullName());
        obj.addProperty("password", req.getPassword());
        obj.addProperty("user", req.getUser());
        obj.addProperty("temperatureUnits", req.getTemperatureUnits().toString());
        obj.addProperty("timeZone", req.getTimeZone().getID());
        return obj;
    }
    public UpdateUserDetailsRequest parseUpdateUserDetailsRequest(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFullName(asString(json.get("fullName")));
        req.setPassword(asString(json.get("password")));
        req.setUser(asString(json.get("user")));
        if (json.has("temperatureUnits")) {
            req.setTemperatureUnits(TemperatureUnits.valueOf(json.get("temperatureUnits").getAsString()));
        }
        if (json.has("timeZone")) {
            req.setTimeZone(TimeZone.getTimeZone(json.get("timeZone").getAsString()));
        }
        return req;
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
