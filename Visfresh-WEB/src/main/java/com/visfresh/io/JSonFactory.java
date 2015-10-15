/**
 *
 */
package com.visfresh.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Role;
import com.visfresh.entities.SchedulePersonHowWhen;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JSonFactory {
    /**
     * The date format.
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public JSonFactory() {
        super();
    }

    /**
     * @param token
     * @return
     */
    public JsonObject toJson(final AuthToken token) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("token", token.getToken());
        obj.addProperty("expired", timeToString(token.getExpirationTime()));
        return obj;
    }


    /**
     * @param json
     * @return
     */
    public User parseUser(final JsonObject json) {
        final User u = new User();
        u.setLogin(asString(json.get("login")));
        u.setFullName(asString(json.get("fullName")));

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

        return obj;
    }

    /**
     * @param id entity ID.
     * @return JSON object.
     */
    public JsonObject idToJson(final Long id) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        return obj;
    }

    /**
     * @param alert alert profile.
     * @return JSON object.
     */
    public JsonObject toJson(final AlertProfile alert) {
        final JsonObject obj = new JsonObject();

        obj.addProperty("id", alert.getId());
        obj.addProperty("description", alert.getDescription());
        obj.addProperty("name", alert.getName());
        obj.addProperty("criticalHighTemperatureForMoreThen", alert.getCriticalHighTemperatureForMoreThen());
        obj.addProperty("criticalHighTemperature", alert.getCriticalHighTemperature());
        obj.addProperty("criticalLowTemperatureForMoreThen", alert.getCriticalLowTemperatureForMoreThen());
        obj.addProperty("criticalLowTemperature", alert.getCriticalLowTemperature());
        obj.addProperty("highTemperature", alert.getHighTemperature());
        obj.addProperty("highTemperatureForMoreThen", alert.getHighTemperatureForMoreThen());
        obj.addProperty("lowTemperature", alert.getLowTemperature());
        obj.addProperty("lowTemperatureForMoreThen", alert.getLowTemperatureForMoreThen());
        obj.addProperty("watchBatteryLow", alert.isWatchBatteryLow());
        obj.addProperty("watchEnterBrightEnvironment", alert.isWatchEnterBrightEnvironment());
        obj.addProperty("watchEnterDarkEnvironment", alert.isWatchEnterDarkEnvironment());
        obj.addProperty("watchShock", alert.isWatchShock());

        return obj;
    }
    /**
     * @param alert encoded alert profile.
     * @return decoded alert profile.
     */
    public AlertProfile parseAlertProfile(final JsonObject alert) {
        final AlertProfile p = new AlertProfile();

        p.setId(asLong(alert.get("id")));
        p.setDescription(asString(alert.get("description")));
        p.setName(asString(alert.get("name")));
        p.setCriticalHighTemperature(asDouble(alert.get("criticalHighTemperature")));
        p.setCriticalHighTemperatureForMoreThen(asInt(alert.get("criticalHighTemperatureForMoreThen")));
        p.setCriticalLowTemperature(asDouble(alert.get("criticalLowTemperature")));
        p.setCriticalLowTemperatureForMoreThen(asInt(alert.get("criticalLowTemperatureForMoreThen")));
        p.setHighTemperature(asDouble(alert.get("highTemperature")));
        p.setHighTemperatureForMoreThen(asInt(alert.get("highTemperatureForMoreThen")));
        p.setLowTemperature(asDouble(alert.get("lowTemperature")));
        p.setLowTemperatureForMoreThen(asInt(alert.get("lowTemperatureForMoreThen")));
        p.setWatchBatteryLow(asBoolean(alert.get("watchBatteryLow")));
        p.setWatchEnterBrightEnvironment(asBoolean(alert.get("watchEnterBrightEnvironment")));
        p.setWatchEnterDarkEnvironment(asBoolean(alert.get("watchEnterDarkEnvironment")));
        p.setWatchShock(asBoolean(alert.get("watchShock")));

        return p;
    }

    /**
     * @param location
     * @return
     */
    public JsonObject toJson(final LocationProfile location) {
        final JsonObject obj = new JsonObject();

        obj.addProperty("id", location.getId());
        obj.addProperty("company", location.getCompany());
        obj.addProperty("name", location.getName());
        obj.addProperty("notes", location.getNotes());
        obj.addProperty("address", location.getAddress());

        final JsonObject loc = new JsonObject();
        obj.add("location", loc);
        loc.addProperty("lat", location.getLocation().getLatitude());
        loc.addProperty("lon", location.getLocation().getLongitude());

        obj.addProperty("radius", location.getRadius());

        return obj;
    }
    /**
     * @param obj encoded location profile.
     * @return location profile.
     */
    public LocationProfile parseLocationProfile(final JsonObject obj) {
        final LocationProfile location = new LocationProfile();

        location.setId(asLong(obj.get("id")));
        location.setCompany(asString(obj.get("company")));
        location.setName(asString(obj.get("name")));
        location.setNotes(asString(obj.get("notes")));
        location.setAddress(asString(obj.get("address")));

        final JsonObject loc = obj.get("location").getAsJsonObject();
        location.getLocation().setLatitude(loc.get("lat").getAsDouble());
        location.getLocation().setLongitude(loc.get("lon").getAsDouble());

        location.setRadius(asInt(obj.get("radius")));

        return location;
    }

    /**
     * @param schedule notification schedule.
     * @return JSON object.
     */
    public JsonObject toJson(final NotificationSchedule schedule) {
        final JsonObject obj = new JsonObject();

        obj.addProperty("description", schedule.getDescription());
        obj.addProperty("name", schedule.getName());
        obj.addProperty("id", schedule.getId());

        final JsonArray array = new JsonArray();
        obj.add("schedules", array);

        for (final SchedulePersonHowWhen sphw : schedule.getSchedules()) {
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

        sched.setDescription(asString(obj.get("description")));
        sched.setName(asString(obj.get("name")));
        sched.setId(asLong(obj.get("id")));

        final JsonArray array = obj.get("schedules").getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            sched.getSchedules().add(parseSchedulePersonHowWhen(array.get(i).getAsJsonObject()));
        }

        return sched;
    }

    /**
     * @param s schedule/person/how/when
     * @return JSON object.
     */
    public JsonObject toJson(final SchedulePersonHowWhen s) {
        final JsonObject obj = new JsonObject();

        obj.addProperty("company", s.getCompany());
        obj.addProperty("emailNotification", s.getEmailNotification());
        obj.addProperty("firstName", s.getFirstName());
        obj.addProperty("lastName", s.getLastName());
        obj.addProperty("position", s.getPosition());
        obj.addProperty("smsNotification", s.getSmsNotification());
        obj.addProperty("toTime", s.getToTime());
        obj.addProperty("fromTime", s.getFromTime());
        obj.addProperty("id", s.getId());
        obj.addProperty("pushToMobileApp", s.isPushToMobileApp());

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
    public SchedulePersonHowWhen parseSchedulePersonHowWhen(
            final JsonObject obj) {
        final SchedulePersonHowWhen s = new SchedulePersonHowWhen();

        s.setCompany(asString(obj.get("company")));
        s.setEmailNotification(asString(obj.get("emailNotification")));
        s.setFirstName(asString(obj.get("firstName")));
        s.setLastName(asString(obj.get("lastName")));
        s.setPosition(asString(obj.get("position")));
        s.setSmsNotification(asString(obj.get("smsNotification")));
        s.setToTime(asInt(obj.get("toTime")));
        s.setFromTime(asInt(obj.get("fromTime")));
        s.setId(asLong(obj.get("id")));
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
    public JsonObject toJson(final ShipmentTemplate tpl) {
        final JsonObject obj = new JsonObject();

        addShipmentBase(tpl, obj);
        obj.addProperty("addDateShipped", tpl.isAddDateShipped());
        obj.addProperty("useCurrentTimeForDateShipped", tpl.isUseCurrentTimeForDateShipped());
        obj.addProperty("detectLocationForShippedFrom", tpl.isDetectLocationForShippedFrom());

        return obj;
    }
    /**
     * @param shpb
     * @param obj
     */
    private void addShipmentBase(final ShipmentBase shpb, final JsonObject obj) {
        obj.addProperty("name", shpb.getName());
        obj.addProperty("shipmentDescription", shpb.getShipmentDescription());
        obj.addProperty("alertSuppressionDuringCoolDown", shpb.getAlertSuppressionDuringCoolDown());
        obj.addProperty("id", shpb.getId());
        obj.addProperty("alertProfile", getId(shpb.getAlertProfile()));
        obj.add("alertsNotificationSchedules", getIdList(shpb.getAlertsNotificationSchedules()));
        obj.addProperty("arrivalNotificationWithIn", shpb.getArrivalNotificationWithIn());
        obj.add("arrivalNotificationSchedules", getIdList(shpb.getArrivalNotificationSchedules()));
        obj.addProperty("excludeNotificationsIfNoAlertsFired", shpb.isexcludeNotificationsIfNoAlertsFired());
        obj.addProperty("shippedFrom", getId(shpb.getShippedFrom()));
        obj.addProperty("shippedTo", getId(shpb.getShippedTo()));
        obj.addProperty("shutdownDevice", shpb.getShutdownDeviceTimeOut());
    }
    /**
     * @param obj JSON object.
     * @return shipment template.
     */
    public ShipmentTemplate parseShipmentTemplate(final JsonObject obj) {
        final ShipmentTemplate tpl = new ShipmentTemplate();

        parseShipmentBase(obj, tpl);
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
        shp.setName(asString(obj.get("name")));
        shp.setShipmentDescription(asString(obj.get("shipmentDescription")));
        shp.setAlertSuppressionDuringCoolDown(asInt(obj.get("alertSuppressionDuringCoolDown")));
        shp.setId(asLong(obj.get("id")));
        shp.setAlertProfile(resolveAlertProfile(asLong(obj.get("alertProfile"))));
        shp.getAlertsNotificationSchedules().addAll(
                resolveNotificationSchedules(obj.get("alertsNotificationSchedules").getAsJsonArray()));
        shp.setArrivalNotificationWithIn(asInt(obj.get("arrivalNotificationWithIn")));
        shp.getArrivalNotificationSchedules().addAll(
                resolveNotificationSchedules(obj.get("arrivalNotificationSchedules").getAsJsonArray()));
        shp.setexcludeNotificationsIfNoAlertsFired(asBoolean(obj.get("excludeNotificationsIfNoAlertsFired")));
        shp.setShippedFrom(resolveLocationProfile(asLong(obj.get("shippedFrom"))));
        shp.setShippedTo(resolveLocationProfile(asLong(obj.get("shippedTo"))));
        shp.setShutdownDeviceTimeOut(asInt(obj.get("shutdownDevice")));
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
    public JsonObject toJson(final Device d) {
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

        s.setPalletId(asString(json.get("palletId")));
        s.setPoNum(asString(json.get("poNum")));
        s.setShipmentDescriptionDate(asDate(json.get("shipmentDescriptionDate")));
        s.setCustomFields(asString(json.get("customFields")));
        s.setStatus(ShipmentStatus.valueOf(json.get("status").getAsString()));

        final JsonArray array = json.get("devices").getAsJsonArray();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            final String imei = array.get(i).getAsString();
            final Device t = resolveDevice(imei);
            if (t != null) {
                s.getDevices().add(t);
            }
        }

        return s;
    }
    /**
     * @param s shipment.
     * @return shipment serialized to JSON format.
     */
    public JsonObject toJson(final Shipment s) {
        final JsonObject obj = new JsonObject();
        addShipmentBase(s, obj);

        obj.addProperty("palletId", s.getPalletId());
        obj.addProperty("poNum", s.getPoNum());
        obj.addProperty("shipmentDescriptionDate", timeToString(s.getShipmentDescriptionDate()));
        obj.addProperty("customFields", s.getCustomFields());
        obj.addProperty("status", s.getStatus().name());

        final JsonArray array = new JsonArray();
        for (final Device t : s.getDevices()) {
            array.add(new JsonPrimitive(t.getImei()));
        }
        obj.add("devices", array);

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
    public Arrival parseArrival(final JsonObject obj) {
        final Arrival a = new Arrival();
        a.setDate(asDate(obj.get("date")));
        a.setDevice(resolveDevice(asString(obj.get("device"))));
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
        json.addProperty("date", timeToString(arrival.getDate()));
        json.addProperty("device", arrival.getDevice().getImei());
        return json;
    }
    /**
     * @param json
     * @return
     */
    public Alert parseAlert(final JsonObject json) {
        final AlertType type = AlertType.valueOf(json.get("type").getAsString());
        Alert alert;
        switch (type) {
            case CriticalHighTemperature:
            case CriticalLowTemperature:
            case HighTemperature:
            case LowTemperature:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(asDouble(json.get("temperature")));
                ta.setMinutes(asInt(json.get("minutes")));
                alert = ta;
                break;
            default:
                alert = new Alert();
        }

        alert.setDate(asDate(json.get("date")));
        alert.setDescription(asString(json.get("description")));
        alert.setDevice(resolveDevice(asString(json.get("device"))));
        alert.setId(asLong(json.get("id")));
        alert.setName(asString(json.get("name")));
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
        json.addProperty("description", alert.getDescription());
        json.addProperty("name", alert.getName());
        json.addProperty("id", alert.getId());
        json.addProperty("date", timeToString(alert.getDate()));
        json.addProperty("device", alert.getDevice().getImei());
        json.addProperty("type", alert.getType().name());

        switch (alert.getType()) {
            case CriticalHighTemperature:
            case CriticalLowTemperature:
            case HighTemperature:
            case LowTemperature:
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
        obj.addProperty("time", timeToString(e.getTime()));
        obj.addProperty("type", e.getType().name());
        return obj;
    }
    public TrackerEvent parseTrackerEvent(final JsonObject json) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(asInt(json.get("battery")));
        e.setId(asLong(json.get("id")));
        e.setTemperature(asDouble(json.get("temperature")));
        e.setTime(asDate(json.get("time")));
        e.setType(TrackerEventType.valueOf(asString(json.get("type"))));
        return e;
    }
    /**
     * @param d shipment data.
     * @return JSON object.
     */
    public JsonObject toJson(final ShipmentData d) {
        final JsonObject json = new JsonObject();
        json.addProperty("shipment", d.getShipment().getId());

        final JsonArray array = new JsonArray();
        for (final DeviceData deviceData : d.getDeviceData()) {
            array.add(toJson(deviceData));
        }
        json.add("data", array);

        return json;
    }
    public ShipmentData parseShipmentData(final JsonObject json) {
        final ShipmentData s = new ShipmentData();
        s.setShipment(resolveShipment(asLong(json.get("shipment"))));

        final JsonArray array = json.get("data").getAsJsonArray();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            s.getDeviceData().add(parseDeviceData(array.get(i).getAsJsonObject()));
        }

        return s;
    }
    /**
     * @param deviceData
     * @return
     */
    public JsonObject toJson(final DeviceData deviceData) {
        final JsonObject json = new JsonObject();
        json.addProperty("device", deviceData.getDevice().getImei());

        //add alerts
        JsonArray array = new JsonArray();
        for (final Alert a : deviceData.getAlerts()) {
            array.add(toJson(a));
        }
        json.add("alerts", array);

        //add events
        array = new JsonArray();
        for (final TrackerEvent e : deviceData.getEvents()) {
            array.add(toJson(e));
        }
        json.add("events", array);

        return json;
    }
    /**
     * @param json JSON object.
     * @return device data.
     */
    public DeviceData parseDeviceData(final JsonObject json) {
        final DeviceData d = new DeviceData();
        d.setDevice(resolveDevice(asString(json.get("device"))));

        //alerts
        JsonArray array = json.get("alerts").getAsJsonArray();
        int size = array.size();
        for (int i = 0; i < size; i++) {
            d.getAlerts().add(parseAlert(array.get(i).getAsJsonObject()));
        }

        //events
        array = json.get("events").getAsJsonArray();
        size = array.size();
        for (int i = 0; i < size; i++) {
            d.getEvents().add(parseTrackerEvent(array.get(i).getAsJsonObject()));
        }

        return d;
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
    public JsonObject toJson(final DeviceCommand cmd) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("device", cmd.getDevice().getImei());
        obj.addProperty("command", cmd.getCommand());
        return obj;
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
     * @param imei device IMEI code.
     * @return device.
     */
    protected Device resolveDevice(final String imei) {
        return imei == null ? null : getReferenceResolver().getDevice(imei);
    }
    /**
     * @param id shipment ID.
     * @return shipment.
     */
    private Shipment resolveShipment(final Long id) {
        return id == null ? null : getReferenceResolver().getShipment(id);
    }

    /**
     * @param e
     * @return
     */
    private double asDouble(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsDouble();
    }
    /**
     * @param e
     * @return
     */
    private int asInt(final JsonElement e) {
        return e == null || e.isJsonNull() ? 0 : e.getAsInt();
    }
    /**
     * @param e JSON element.
     * @return JSON element as long.
     */
    private Long asLong(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsLong();
    }
    /**
     * @param e JSON element.
     * @return JSON element as string.
     */
    private String asString(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }
    /**
     * @param e JSON element.
     * @return JSON element as boolean.
     */
    private Boolean asBoolean(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : e.getAsBoolean();
    }
    /**
     * @param e JSON string.
     * @return JSON string as boolean.
     */
    private Date asDate(final JsonElement e) {
        return e == null || e.isJsonNull() ? null : parseDate(e.getAsString());
    }
    /**
     * @param str
     * @return
     */
    public static Date parseDate(final String str) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(str);
        } catch (final ParseException exc) {
            throw new RuntimeException(exc);
        }
    }
    /**
     * @param date
     * @return
     */
    public static String formatDate(final Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }
    /**
     * @param e
     * @return
     */
    private Long getId(final EntityWithId e) {
        return e == null ? null : e.getId();
    }
    /**
     * @param entities list of entity.
     * @return JSON array with entity IDs.
     */
    private <E extends EntityWithId> JsonArray getIdList(final List<E> entities) {
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
    public JsonObject createErrorStatus(final int errorCode, final Throwable e) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("code", errorCode);
        obj.addProperty("message", e.getMessage() == null ? e.toString() : e.getMessage());
        return obj;
    }
    /**
     * @param time
     * @return the time in 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format.
     */
    private String timeToString(final Date time) {
        return time == null ? null : new SimpleDateFormat(DATE_FORMAT).format(time);
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
