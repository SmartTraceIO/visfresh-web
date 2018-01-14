/**
 *
 */
package com.visfresh.dao.impl.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.constants.NoteConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.io.json.LocationSerializer;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.NotificationIssueBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.l12n.NotificationIssueBeanBundle;
import com.visfresh.l12n.RuleBeanBundle;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBeanSerializer extends AbstractJsonSerializer {
    private final LocationSerializer locationSerializer;
    private JsonShortenerFactory shortenerFactory = new JsonShortenerFactory();

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
    private final DateFormat isoFormat;
    private final DateFormat prettyFormat;
    private final TemperatureUnits tempUnits;
    private final RuleBeanBundle ruleBundle;
    private final NotificationIssueBeanBundle alertsBundle;

    /**
     * Default constructor.
     */
    public SingleShipmentBeanSerializer() {
        this(SerializerUtils.UTС, Language.English, TemperatureUnits.Celsius);
    }
    /**
     * @param tz time zone.
     */
    public SingleShipmentBeanSerializer(final TimeZone tz, final Language language, final TemperatureUnits units) {
        super(tz);
        locationSerializer = new LocationSerializer(tz);
        isoFormat = DateTimeUtils.createIsoFormat(language, tz);
        prettyFormat = DateTimeUtils.createPrettyFormat(language, tz);
        tempUnits = units;
        ruleBundle = new RuleBeanBundle();
        alertsBundle = new NotificationIssueBeanBundle(language, tz, units);
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
    public List<ShipmentCompanyDto> parseCompanyAccessArray(final JsonArray array) {
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
    public List<ShipmentUserDto> parseUserAccessArray(final JsonArray array) {
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
     * @param item list notification schedule item.
     * @return
     */
    protected JsonObject toJson(final ListNotificationScheduleItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("notificationScheduleId", item.getNotificationScheduleId());
        json.addProperty("notificationScheduleName", item.getNotificationScheduleName());
        json.addProperty("notificationScheduleDescription", item.getNotificationScheduleDescription());
        json.addProperty("peopleToNotify", item.getPeopleToNotify());
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
        item.setNotificationScheduleDescription(asString(json.get("notificationScheduleDescription")));
        item.setPeopleToNotify(asString(json.get("peopleToNotify")));
        return item;
    }
    /**
     * @param summary
     * @return
     */
    protected JsonArray createAlertSummaryArray(final Map<AlertType, Integer> summary) {
        return createAlertSummaryArray(summary.keySet());
    }
    /**
     * @param summary
     * @return
     */
    protected JsonArray createAlertSummaryArray(final Set<AlertType> summary) {
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
    protected JsonArray locationsToJson(final List<LocationProfileBean> locs) {
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
     * @param array
     * @return
     */
    public List<InterimStopBean> parseInterimStops(final JsonArray array) {
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
    private JsonObject toJson(final InterimStopBean stop) {
        if (stop == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", stop.getId());
        json.addProperty("time", stop.getTime());
        json.addProperty("stopDate", toIsoString(stop.getStopDate()));
        json.add("location", toJson(stop.getLocation()));
        return json;
    }
    /**
     * @param e JSON element.
     * @return single shipment interim stop.
     */
    public InterimStopBean parseInterimStopBean(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final InterimStopBean stop = new InterimStopBean();
        stop.setId(asLong(json.get("id")));
        stop.setTime(asInt(json.get("time")));
        stop.setStopDate(parseIsoDate(json.get("stopDate")));
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
    public DeviceGroupDto parseDeviceGroupDto(final JsonElement e) {
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
     * @param location
     * @return
     */
    protected JsonElement toJson(final Location location) {
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
    public LocationProfileBean parseLocationProfileBean(final JsonElement el) {
        return locationSerializer.parseLocationProfileDto(el);
    }

    /**
     * @param s single shipment bean.
     * @return JSON serialized single shipment bean.
     */
    public JsonObject toJson(final SingleShipmentBean s) {
        if (s == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("shipmentId", s.getShipmentId());
        json.addProperty("companyId", s.getCompanyId());
        json.addProperty("device", s.getDevice());
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
        json.addProperty("startTime", toIsoString(s.getStartTime()));
        json.add("endLocation", toJson(s.getEndLocation()));
        json.addProperty("eta", toIsoString(s.getEta()));
        json.add("currentLocation", toJson(s.getCurrentLocation()));
        json.addProperty("currentLocationDescription", s.getCurrentLocationDescription());
        json.addProperty("percentageComplete", s.getPercentageComplete());

        json.addProperty("minTemp", s.getMinTemp());
        json.addProperty("maxTem", s.getMaxTemp());

        json.add("siblings", toJsonArray(s.getSiblings()));
        json.add("alertYetToFire", alertRulesToJson(s.getAlertYetToFire()));
        json.add("alertFired", alertRulesToJson(s.getAlertFired()));
        json.add("arrival", toJson(s.getArrival()));
        json.addProperty("shutdownTime", toIsoString(s.getShutdownTime()));
        json.addProperty("arrivalTime", toIsoString(s.getArrivalTime()));
        json.addProperty("alertsSuppressed", s.isAlertsSuppressed());
        json.addProperty("alertsSuppressionTime", toIsoString(s.getAlertsSuppressionTime()));
        json.addProperty("firstReadingTime", toIsoString(s.getFirstReadingTime()));
        json.addProperty("lastReadingTime", toIsoString(s.getLastReadingTime()));
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
        json.add("notes", noteBeansToJson(s.getNotes()));
        json.add("deviceGroups", deviceGroupsToJson(s.getDeviceGroups()));
        json.addProperty("deviceColor", s.getDeviceColor());
        json.addProperty("isLatestShipment", s.isLatestShipment());
        json.addProperty("arrivalReportSent", s.isArrivalReportSent());
        json.add("userAccess", userAcessToJson(s.getUserAccess()));
        json.add("companyAccess", companyAccessToJson(s.getCompanyAccess()));
        json.add("sentAlerts", alertsToJson(s.getSentAlerts()));
        json.add("alertProfile", toJson(s.getAlertProfile()));

        return shorten(json);
    }
    /**
     * @param arrival
     * @return
     */
    private JsonObject toJson(final ArrivalBean arrival) {
        if (arrival == null) {
            return null;
        }
        final JsonObject json = notificationIssueToJson(arrival);
        json.addProperty("mettersForArrival", arrival.getMettersForArrival());
        json.addProperty("notifiedAt", toIsoString(arrival.getNotifiedAt()));
        return json;
    }
    private ArrivalBean parseArrivalBean(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final ArrivalBean arrival = new ArrivalBean();

        jsonToNotificationIssue(json, arrival);
        arrival.setMettersForArrival(asInteger(json.get("mettersForArrival")));
        arrival.setNotifiedAt(parseIsoDate(json.get("notifiedAt")));
        return arrival;
    }
    /**
     * @param e JSON element.
     * @return single shipment bean.
     */
    public SingleShipmentBean parseSingleShipmentBean(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = unShorten(e.getAsJsonObject());
        final SingleShipmentBean s = new SingleShipmentBean();

        s.setShipmentId(asLong(json.get("shipmentId")));
        s.setCompanyId(asLong(json.get("companyId")));
        s.setDevice(asString(json.get("device")));
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
        s.setStartTime(parseIsoDate(json.get("startTime")));
        s.setEndLocation(parseLocationProfileBean(json.get("endLocation")));
        s.setEta(parseIsoDate(json.get("eta")));
        s.setCurrentLocation(parseLocation(json.get("currentLocation")));
        s.setCurrentLocationDescription(asString(json.get("currentLocationDescription")));
        s.setPercentageComplete(asInt(json.get("percentageComplete")));

        s.setMinTemp(asDouble(json.get("minTemp")));
        s.setMaxTemp(asDouble(json.get("maxTem")));

        s.getSiblings().addAll(parseLongList(json.get("siblings").getAsJsonArray()));

        s.getAlertYetToFire().addAll(parseAlertRuleBeans(
                json.get("alertYetToFire").getAsJsonArray()));
        s.getAlertFired().addAll(parseAlertRuleBeans(
                json.get("alertFired").getAsJsonArray()));

        s.setArrival(parseArrivalBean(json.get("arrival")));
        s.setShutdownTime(parseIsoDate(json.get("shutdownTime")));
        s.setArrivalTime(parseIsoDate(json.get("arrivalTime")));
        s.setAlertsSuppressed(asBoolean(json.get("alertsSuppressed")));
        s.setAlertsSuppressionTime(parseIsoDate(json.get("alertsSuppressionTime")));
        s.setFirstReadingTime(parseIsoDate(json.get("firstReadingTime")));
        s.setLastReadingTime(parseIsoDate(json.get("lastReadingTime")));
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

        s.getNotes().addAll(parseNoteBeans(json.get("notes").getAsJsonArray()));
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
     * @param json
     * @return
     */
    private JsonObject shorten(final JsonObject json) {
        if (shortenerFactory != null) {
            final JsonShortener sh = shortenerFactory.createDefaultShortener();
            final JsonObject result = sh.shorten(json);
            //add shortener version
            result.addProperty("version", JsonShortenerFactory.DEFAULT_VERSION);
            return result;
        } else {
            return json;
        }
    }
    /**
     * @param json JSON object.
     * @return unshortened JSON object.
     */
    private JsonObject unShorten(final JsonObject json) {
        if (shortenerFactory != null) {
            final JsonShortener sh = shortenerFactory.createDefaultShortener(asInteger(json.get("version")));
            return sh.unShorten(json);
        } else {
            return json;
        }
    }
    /**
     * @param loc
     * @return
     */
    public JsonObject toJson(final SingleShipmentLocationBean loc) {
        if (loc == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", loc.getId());
        json.addProperty("latitude", loc.getLatitude());
        json.addProperty("longitude", loc.getLongitude());
        json.addProperty("temperature", loc.getTemperature());
        json.addProperty("time", toIsoString(loc.getTime()));
        json.add("alerts", alertsToJson(loc.getAlerts()));
        json.addProperty("type", loc.getType().toString());
        return json;
    }
    /**
     * @param el
     * @return
     */
    public SingleShipmentLocationBean parseSingleShipmentLocationBean(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final SingleShipmentLocationBean bean = new SingleShipmentLocationBean();
        bean.setId(asLong(json.get("id")));
        bean.setLatitude(asDouble(json.get("latitude")));
        bean.setLongitude(asDouble(json.get("longitude")));
        bean.setTemperature(asDouble(json.get("temperature")));
        bean.setTime(parseIsoDate(json.get("time")));
        bean.getAlerts().addAll(parseAlertBeans(json.get("alerts").getAsJsonArray()));
        bean.setType(TrackerEventType.valueOf(asString(json.get("type"))));
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

        final JsonObject json = notificationIssueToJson(a);
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
    public AlertBean parseAlertBean(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();

        final AlertType alertType = AlertType.valueOf(asString(json.get("type")));

        final AlertBean a = alertType.isTemperatureAlert() ? new TemperatureAlertBean() : new AlertBean();
        jsonToNotificationIssue(json, a);

        a.setType(alertType);
        if (alertType.isTemperatureAlert()) {
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
    private JsonObject notificationIssueToJson(final NotificationIssueBean nb) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", nb.getId());
        json.addProperty("date", toIsoString(nb.getDate()));
        json.addProperty("trackerEventId", nb.getTrackerEventId());
        return json;
    }
    /**
     * @param json
     * @param a
     */
    private void jsonToNotificationIssue(final JsonObject json, final NotificationIssueBean a) {
        a.setId(asLong(json.get("id")));
        a.setDate(parseIsoDate(json.get("date")));
        a.setTrackerEventId(asLong(json.get("trackerEventId")));
    }
    /**
     * @param s
     * @return
     */
    protected JsonArray noteBeansToJson(final List<NoteBean> notes) {
        final JsonArray array = new JsonArray();
        for (final NoteBean note : notes) {
            array.add(toJson(note));
        }
        return array;
    }
    /**
     * @param array
     * @return
     */
    private List<NoteBean> parseNoteBeans(final JsonArray array) {
        final List<NoteBean> notes = new LinkedList<>();
        for (final JsonElement e : array) {
            notes.add(parseNoteBean(e));
        }
        return notes;
    }
    /**
     * @param note note bean.
     * @return JSON serialized note bean.
     */
    private JsonObject toJson(final NoteBean note) {
        if (note == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("noteText", note.getNoteText());
        json.addProperty("timeOnChart", toIsoString(note.getTimeOnChart()));
        json.addProperty("noteType", note.getNoteType());
        json.addProperty("noteNum", note.getNoteNum());
        json.addProperty("creationDate", toIsoString(note.getCreationDate()));
        json.addProperty("createdBy", note.getCreatedBy());
        json.addProperty("active", note.isActive());
        json.addProperty("createCreatedByName", note.getCreatedByName());
        return json;
    }
    /**
     * @param e JSON element.
     * @return note bean.
     */
    public NoteBean parseNoteBean(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final NoteBean bean = new NoteBean();
        bean.setNoteText(asString(json.get("noteText")));
        bean.setTimeOnChart(parseIsoDate(json.get("timeOnChart")));
        bean.setNoteType(asString(json.get("noteType")));
        bean.setNoteNum(asInteger(json.get("noteNum")));
        bean.setCreationDate(parseIsoDate(json.get("creationDate")));
        bean.setCreatedBy(asString(json.get("createdBy")));
        bean.setActive(asBoolean(json.get("active")));
        bean.setCreatedByName(asString(json.get("createCreatedByName")));
        return bean;
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

        return r;
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
    public CorrectiveAction parseCorrectiveAction(final JsonElement e) {
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
    /**
     * @param date date.
     * @return date as string.
     */
    private String toIsoString(final Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }
    private Date parseIsoDate(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        try {
            return dateFormat.parse(e.getAsString());
        } catch (final ParseException exc) {
            throw new RuntimeException(exc);
        }
    }
    /**
     * @param f the shortenerFactory to set
     */
    public void setShortenerFactory(final JsonShortenerFactory f) {
        this.shortenerFactory = f;
    }
    /**
     * @return the shortenerFactory
     */
    public JsonShortenerFactory getShortenerFactory() {
        return shortenerFactory;
    }
    public JsonObject exportToViewData(final SingleShipmentData data) {
        return exportToViewData(data.getBean(), data.getSiblings(), data.getLocations());
    }
    /**
     * @param bean
     * @return
     */
    private JsonObject exportToViewData(final SingleShipmentBean bean,
            final List<SingleShipmentBean> siblings, final List<SingleShipmentLocationBean> readings) {
        final JsonObject json = exportCommonWithSiblingsData(bean);
        //add not main shipment data
        json.addProperty(ShipmentConstants.DEVICE_NAME, bean.getDeviceName());
        json.addProperty("shipmentDescription", bean.getShipmentDescription());
        json.addProperty("palletId", bean.getPalletId());
        json.addProperty("assetNum", bean.getAssetNum());
        json.addProperty("assetType", bean.getAssetType());
        json.addProperty("status", bean.getStatus().name());

        final AlertProfileBean ap = bean.getAlertProfile();
        json.addProperty("alertProfileId", ap == null ? null : ap.getId());
        json.addProperty("alertProfileName", ap == null ? null : ap.getName());
        json.add("alertProfile", ap == null ? null : exportToView(ap));
        json.addProperty("alertSuppressionMinutes", bean.getAlertSuppressionMinutes());

        json.addProperty("alertPeopleToNotify", createPeopleToNotifyString(
                bean.getAlertsNotificationSchedules()));

        //alertsNotificationSchedules
        JsonArray array = new JsonArray();
        for (final ListNotificationScheduleItem item: bean.getAlertsNotificationSchedules()) {
            array.add(toJson(item));
        }
        json.add("alertsNotificationSchedules", array);
        json.addProperty("alertYetToFire", alertsToOneString(bean.getAlertYetToFire()));
        json.addProperty("alertFired", alertsToOneString(bean.getAlertFired()));

        //"arrivalNotificationTimeISO": "2014-08-12 12:10",
        // NEW - ISO for actual time arrival notification sent out
        final ArrivalBean arrival = bean.getArrival();
        json.addProperty("arrivalNotificationTimeISO", arrival == null ? null : formatIso(arrival.getDate()));
        json.addProperty("arrivalNotificationTime", arrival == null ? null : formatPretty(arrival.getDate()));

        json.addProperty("arrivalNotificationWithinKm", bean.getArrivalNotificationWithinKm());
        json.addProperty("excludeNotificationsIfNoAlerts", bean.isExcludeNotificationsIfNoAlerts());

        json.addProperty("arrivalPeopleToNotify", createPeopleToNotifyString(
                bean.getArrivalNotificationSchedules()));

        array = new JsonArray();
        for (final ListNotificationScheduleItem item: bean.getArrivalNotificationSchedules()) {
            array.add(toJson(item));
        }

        json.addProperty("commentsForReceiver", bean.getCommentsForReceiver());

        json.add("arrivalNotificationSchedules", array);
        json.addProperty("shutdownDeviceAfterMinutes", bean.getShutdownDeviceAfterMinutes());

        json.addProperty("noAlertsAfterArrivalMinutes", bean.getNoAlertsAfterArrivalMinutes());
        json.addProperty("shutDownAfterStartMinutes", bean.getShutDownAfterStartMinutes());

        json.addProperty("shutdownTimeISO", formatIso(bean.getShutdownTime()));
        json.addProperty("shutdownTime", formatPretty(bean.getShutdownTime()));

        json.addProperty("startLocation",
                bean.getStartLocation() == null ? null : bean.getStartLocation().getName());
        json.addProperty("startTimeISO", formatIso(bean.getStartTime()));
        json.addProperty("startTime", formatPretty(bean.getStartTime()));
        json.add("startLocationForMap",
                bean.getStartLocation() == null ? null : toJson(bean.getStartLocation().getLocation()));

        json.addProperty("endLocation",
                bean.getEndLocation() == null ? null : bean.getEndLocation().getName());
        json.addProperty("etaISO", formatIso(bean.getEta()));
        json.addProperty("eta", formatPretty(bean.getEta()));

        json.addProperty("arrivalTimeISO", formatIso(bean.getArrivalTime()));
        json.addProperty("arrivalTime", formatPretty(bean.getArrivalTime()));
        json.add("endLocationForMap", bean.getEndLocation() == null ? null : toJson(bean.getEndLocation().getLocation()));

        json.addProperty("lastReadingLocation", bean.getCurrentLocationDescription());
        json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, formatIso(bean.getLastReadingTime()));
        json.addProperty(ShipmentConstants.LAST_READING_TIME, formatPretty(bean.getLastReadingTime()));
        json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE,
                convertTemperature(bean.getLastReadingTemperature()));
        json.addProperty("batteryLevel", bean.getBatteryLevel());

        json.add("lastReadingForMap",
                bean.getCurrentLocation() == null ? null : toJson(bean.getCurrentLocation()));

        json.addProperty("minTemp", convertTemperature(bean.getMinTemp()));
        json.addProperty("maxTemp", convertTemperature(bean.getMaxTemp()));
        json.addProperty("firstReadingTimeISO", formatIso(bean.getFirstReadingTime()));
        json.addProperty("firstReadingTime", formatPretty(bean.getFirstReadingTime()));

        json.addProperty("alertsSuppressed", bean.isAlertsSuppressed());
        json.addProperty("alertsSuppressionTime", formatPretty(bean.getAlertsSuppressionTime()));
        json.addProperty("alertsSuppressionTimeIso", formatIso(bean.getAlertsSuppressionTime()));

        json.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, bean.isSendArrivalReport());
        json.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, bean.isSendArrivalReportOnlyIfAlerts());
        json.addProperty("arrivalReportSent", bean.isArrivalReportSent());

        //alternatives
        json.add("startLocationAlternatives", locationsToJson(bean.getStartLocationAlternatives()));
        json.add(ShipmentConstants.END_LOCATION_ALTERNATIVES, locationsToJson(bean.getEndLocationAlternatives()));
        json.add(ShipmentConstants.INTERIM_LOCATIONS, locationsToJson(bean.getInterimLocationAlternatives()));

        //interim stops
        final JsonArray interimStops = new JsonArray();
        for (final InterimStopBean stp : bean.getInterimStops()) {
            interimStops.add(exportToViewData(stp));
        }
        json.add(ShipmentConstants.INTERIM_STOPS, interimStops);

        //add notes
        final JsonArray notes = new JsonArray();
        for (final NoteBean note : bean.getNotes()) {
            notes.add(exportToViewData(note, bean));
        }
        json.add("notes", notes);

        //locations
        final Map<SingleShipmentLocationBean, JsonObject> readingsMap = new HashMap<>();

        final JsonArray locations = new JsonArray();
        for (final SingleShipmentLocationBean reading : readings) {
            final JsonObject loc = exportReadingToViewData(reading);
            readingsMap.put(reading, loc);
            locations.add(loc);
        }
        json.add("locations", locations);

        //add first and last locations
        if (locations.size() > 0) {
            //last reading
            addAlertInfo(locations.get(locations.size() - 1).getAsJsonObject(),
                    createLastReadingAlert(readings.get(readings.size() -1), bean));

            //alerts
            for (final AlertBean alert : bean.getSentAlerts()) {
                final SingleShipmentLocationBean candidate = getBestCandidate(readings, alert);
                addAlertInfo(readingsMap.get(candidate), exportToViewData(alert, candidate, bean));
            }

            //arrival
            if (arrival != null) {
                final SingleShipmentLocationBean candidate = getBestCandidate(readings, arrival);
                addAlertInfo(readingsMap.get(candidate), exportToViewData(arrival, candidate, bean));
            }
        }

        //add siblings
        array = new JsonArray();
        for (final SingleShipmentBean sibling: siblings) {
            array.add(exportCommonWithSiblingsData(sibling));
        }

        json.add("siblings", array);

        return json;
    }
    /**
     * @param items tracker events.
     * @param issue notification issue.
     * @return
     */
    private SingleShipmentLocationBean getBestCandidate(final List<SingleShipmentLocationBean> items,
            final NotificationIssueBean issue) {
        //first of all attempt to found by tracker event ID.
        final Long trackerEventId = issue.getTrackerEventId();
        if (trackerEventId != null) {
            //for new alerts the tracker event ID should not be null.
            for (final SingleShipmentLocationBean i : items) {
                if (i.getId().equals(trackerEventId)) {
                    return i;
                }
            }
        }

        //support of old version where tracker event ID is null.
        final long time = issue.getDate().getTime();
        long distance = Long.MAX_VALUE;

        SingleShipmentLocationBean best = null;
        for (final SingleShipmentLocationBean i : items) {
            final long currentDistance = Math.abs(i.getTime().getTime() - time);
            if (currentDistance < distance) {
                distance = currentDistance;
                best = i;
            }
        }

        return best;
    }
    /**
     * @param reading
     * @return
     */
    private JsonObject exportReadingToViewData(final SingleShipmentLocationBean reading) {
        if (reading == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("lat", reading.getLatitude());
        json.addProperty("long", reading.getLongitude());
        json.addProperty("temperature", convertTemperature(reading.getTemperature()));
        json.addProperty("timeISO", formatIso(reading.getTime()));
        json.addProperty("time", formatPretty(reading.getTime()));
        json.addProperty("type", eventTypeToString(reading.getType()));
        json.add("alerts", new JsonArray());

        return json;
    }
    /**
     * @param type
     * @return
     */
    private String eventTypeToString(final TrackerEventType type) {
        switch(type) {
            case INIT:
                return "SwitchedOn";
            case AUT:
                return "Reading";
            case VIB:
                return "Moving";
            case STP:
                return "Stationary";
            case BRT:
                return "LightOn";
            case DRK:
                return "LightOff";
                default:
                    return null;
        }
    }

    /**
     * @param alert
     * @return
     */
    private JsonObject exportToViewData(final AlertBean alert, final SingleShipmentLocationBean reading,
            final SingleShipmentBean shipment) {
        if (alert == null) {
            return null;
        }
        final String text = this.alertsBundle.buildDescription(alert, reading, shipment);
        return exportNotificationIssue(alert.getType().name(), text);
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject exportToViewData(final ArrivalBean alert, final SingleShipmentLocationBean reading,
            final SingleShipmentBean shipment) {
        if (alert == null) {
            return null;
        }
        final String text = this.alertsBundle.buildDescription(alert, reading, shipment);
        return exportNotificationIssue("ArrivalNotice", text);
    }
    /**
     * @param type
     * @param text
     * @return
     */
    protected JsonObject exportNotificationIssue(final String type, final String text) {
        //set title and lines
        final String[] lines = text.split("\n");


        final JsonObject json = new JsonObject();
        json.addProperty("title", lines[0]);
        for (int i = 1; i < lines.length; i++) {
            json.addProperty("Line" + i, lines[i]);
        }
        json.addProperty("type", type);
        return json;
    }
    /**
     * @param bean
     * @param isNotSibling
     * @return
     */
    protected JsonObject exportCommonWithSiblingsData(final SingleShipmentBean bean) {
        final JsonObject json = new JsonObject();

        json.addProperty("shipmentId", bean.getShipmentId()); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_SN, Device.getSerialNumber(bean.getDevice())); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_COLOR, bean.getDeviceColor());
        json.addProperty("tripCount", bean.getTripCount()); /*+*/
        json.addProperty("isLatestShipment", bean.isLatestShipment());
        json.addProperty("trackerPositionFrontPercent", bean.getTrackerPositionFrontPercent()); /*+*/
        json.addProperty("trackerPositionLeftPercent", bean.getTrackerPositionLeftPercent()); /*+*/

        //alert summary
        json.add(ShipmentConstants.ALERT_SUMMARY, createAlertSummaryArray(
                createSummaryMap(bean.getSentAlerts()))); /*+*/
        //add device groups
        json.add("deviceGroups", deviceGroupsToJson(bean.getDeviceGroups()));

        //company access
        json.add("userAccess", userAcessToJson(bean.getUserAccess()));

        //company access
        json.add("companyAccess", companyAccessToJson(bean.getCompanyAccess()));

        final List<AlertRuleBean> allRules = new LinkedList<>(bean.getAlertFired());
        allRules.addAll(bean.getAlertYetToFire());

        //alerts with corrective actions.
        final JsonArray alertsWithCorrectiveActions = new JsonArray();
        final AlertProfileBean alertProfile = bean.getAlertProfile();
        if (alertProfile != null) {
            for (final AlertBean alert : bean.getSentAlerts()) {
                final AlertRuleBean rule = createRule(alert, allRules);
                if (rule != null) { //old versions of alert may not have the rule set
                    final Long id = getCorrectiveActionListId(rule, alertProfile);
                    if (id != null) {
                        alertsWithCorrectiveActions.add(
                                exportAlertWithCorrectiveActions(alert, bean, rule, id));
                    }
                }
            }
        }
        json.add("alertsWithCorrectiveActions", alertsWithCorrectiveActions);

        return json;
    }
    /**
     * @param stop
     * @return
     */
    private JsonObject exportToViewData(final InterimStopBean stop) {
        if (stop == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", stop.getId());
        json.addProperty("latitude", stop.getLocation().getLocation().getLatitude());
        json.addProperty("longitude", stop.getLocation().getLocation().getLongitude());
        json.addProperty("time", stop.getTime());
        json.addProperty("stopDate", formatPretty(stop.getStopDate()));
        json.addProperty("stopDateISO", formatIso(stop.getStopDate()));
        json.add("location", toJson(stop.getLocation()));
        return json;
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
     * @param t temperature in C units.
     * @return temperature converted to user's units.
     */
    private double convertTemperature(final double t) {
        return Math.round(LocalizationUtils.convertToUnits(t, tempUnits) * 100) / 100.;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final AlertProfileBean alert) {
        final JsonObject obj = toJson((AlertProfileDto) alert);
        if (obj == null) {
            return null;
        }

        obj.add("lightOnCorrectiveActions", toJson(alert.getLightOnCorrectiveActions()));
        obj.add("batteryLowCorrectiveActions", toJson(alert.getBatteryLowCorrectiveActions()));

        return obj;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject exportToView(final AlertProfileBean alert) {
        return toJson((AlertProfileDto) alert);
    }
    /**
     * @param alert
     * @return
     */
    protected JsonObject toJson(final AlertProfileDto alert) {
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
    private AlertProfileBean parseAlertProfileBean(final JsonElement el) {
        if (isNull(el)) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final AlertProfileBean ap = new AlertProfileBean();
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
        ap.setLightOnCorrectiveActions(parseCorrectiveActionListBean(json.get("lightOnCorrectiveActions")));
        ap.setBatteryLowCorrectiveActions(parseCorrectiveActionListBean(json.get("batteryLowCorrectiveActions")));
        return ap;
    }
    /**
     * @param alerts
     * @return
     */
    public static  Map<AlertType, Integer> toSummaryMap(
            final List<Alert> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final Alert alert : alerts) {
            final AlertType type = alert.getType();
            if (type != AlertType.LightOff && type != AlertType.LightOn) {
                Integer numAlerts = map.get(alert.getType());
                if (numAlerts == null) {
                    numAlerts = 0;
                }
                numAlerts = numAlerts + 1;
                map.put(alert.getType(), numAlerts);
            }
        }

        return map;
    }
    public static  Map<AlertType, Integer> createSummaryMap(
            final List<AlertBean> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final AlertBean alert : alerts) {
            final AlertType type = alert.getType();
            if (type != AlertType.LightOff && type != AlertType.LightOn) {
                Integer numAlerts = map.get(alert.getType());
                if (numAlerts == null) {
                    numAlerts = 0;
                }
                numAlerts = numAlerts + 1;
                map.put(alert.getType(), numAlerts);
            }
        }

        return map;
    }
    /**
     * @param time
     * @return
     */
    protected String formatPretty(final Date time) {
        if (time == null) {
            return null;
        }
        return this.prettyFormat.format(time);
    }
    /**
     * @param time time.
     * @return
     */
    protected String formatIso(final Date time) {
        if (time == null) {
            return null;
        }
        return this.isoFormat.format(time);
    }
    /**
     * @param rules alert profile.
     * @param user current user.
     * @return temperature alerts yet to fire so user can suppress future notifications.
     */
    private String alertsToOneString(final List<AlertRuleBean> rules) {
        final List<String> list = new LinkedList<>();
        for (final AlertRuleBean rule: rules) {
            list.add(ruleBundle.buildDescription(rule, this.tempUnits));
        }
        return StringUtils.combine(list, ", ");
    }
    /**
     * @param dto note DTO.
     * @return
     */
    public JsonObject exportToViewData(final NoteBean dto, final SingleShipmentBean bean) {
        final JsonObject json = new JsonObject();

        json.addProperty(NoteConstants.ACTIVE_FLAG, dto.isActive());
        json.addProperty(NoteConstants.CREATED_BY, dto.getCreatedBy());
        json.addProperty(NoteConstants.CREATION_DATE, formatIso(dto.getCreationDate()));
        json.addProperty(NoteConstants.NOTE_NUM, dto.getNoteNum());
        json.addProperty(NoteConstants.NOTE_TEXT, dto.getNoteText());
        json.addProperty(NoteConstants.SHIPMENT_ID, bean.getShipmentId());
        json.addProperty(NoteConstants.NOTE_TYPE, dto.getNoteType());
        json.addProperty(NoteConstants.SN, Device.getSerialNumber(bean.getDevice()));
        json.addProperty(NoteConstants.TRIP, bean.getTripCount());
        json.addProperty(NoteConstants.TIME_ON_CHART, formatIso(dto.getTimeOnChart()));
        json.addProperty(NoteConstants.CREATE_CREATED_BY_NAME, dto.getCreatedByName());

        return json;
    }
    /**
     * @param a alert DTO.
     * @param rule
     * @return JSON object.
     */
    private JsonObject exportAlertWithCorrectiveActions(final AlertBean a,
            final SingleShipmentBean s, final AlertRuleBean rule, final Long correctiveActionListId) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", a.getId());
        json.addProperty("time", formatPretty(a.getDate()));
        json.addProperty("timeISO", formatIso(a.getDate()));
        json.addProperty("correctiveActionListId", correctiveActionListId);
        json.addProperty("type", a.getType().toString());
        json.addProperty("description", ruleBundle.buildDescription(rule, this.tempUnits));
        return json;
    }
    /**
     * @param a alert.
     * @param ap alert profile.
     * @return
     */
    private Long getCorrectiveActionListId(final AlertRuleBean rule, final AlertProfileBean ap) {
        if (rule instanceof TemperatureRuleBean) {
            final TemperatureRuleBean tr = (TemperatureRuleBean) rule;
            return tr.getCorrectiveActions() == null ? null : tr.getCorrectiveActions().getId();
        } else if (rule.getType() == AlertType.LightOn) {
            return ap.getLightOnCorrectiveActions() == null
                    ? null : ap.getLightOnCorrectiveActions().getId();
        } else if (rule.getType() == AlertType.Battery) {
            return ap.getBatteryLowCorrectiveActions() == null
                    ? null : ap.getBatteryLowCorrectiveActions().getId();
        }

        return null;
    }
    private AlertRuleBean createRule(final AlertBean a,
            final List<AlertRuleBean> rules) {
        if (a instanceof TemperatureAlertBean) {
            return findTemperatureRule((TemperatureAlertBean) a, rules);
        }

        final AlertRuleBean rule = new AlertRuleBean();
        rule.setType(a.getType());
        return rule;
    }
    /**
     * @param ta
     * @param ap
     * @return
     */
    private TemperatureRuleBean findTemperatureRule(
            final TemperatureAlertBean ta, final List<AlertRuleBean> rules) {
        if (ta.getRuleId() == null) {
            return null;
        }

        for (final AlertRuleBean rule : rules) {
            if (ta.getRuleId().equals(rule.getId())) {
                final TemperatureRuleBean tr = (TemperatureRuleBean) rule;
                return tr;
            }
        }
        return null;
    }

    /**
     * @param loc
     * @param exportToViewData
     */
    private void addAlertInfo(final JsonObject loc, final JsonObject exportToViewData) {
        loc.get("alerts").getAsJsonArray().add(exportToViewData);
    }
    /**
     * @param reading
     * @param shipment
     * @return
     */
    private JsonObject createLastReadingAlert(final SingleShipmentLocationBean reading,
            final SingleShipmentBean shipment) {
        final String text = this.alertsBundle.buildTrackerEventDescription(reading, shipment);
        return exportNotificationIssue("LastReading", text);
    }
    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(final DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
}
