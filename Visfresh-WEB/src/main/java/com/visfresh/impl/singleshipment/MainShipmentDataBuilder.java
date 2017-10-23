/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.impl.services.NotificationServiceImpl;
import com.visfresh.impl.services.SingleShipmentServiceImpl;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.io.json.ShipmentSessionSerializer;
import com.visfresh.io.json.SingleShipmentBeanSerializer;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MainShipmentDataBuilder implements SingleShipmentPartBuilder {
    protected final NamedParameterJdbcTemplate jdbc;

    private final Map<Long, SingleShipmentBean> beans = new HashMap<>();
    private final Map<Long, ShipmentSession> sessions = new HashMap<>();
    private final ShipmentSessionSerializer sessionSerializer = new ShipmentSessionSerializer();
    private final SingleShipmentBeanSerializer serializer = new SingleShipmentBeanSerializer();

    private final Long shipmentId;
    private final String query;

    /**
     * @param jdbc JDBC template.
     * @param shipmentId shipment ID.
     * @param companyId company ID.
     * @param siblings list of siblings.
     */
    public MainShipmentDataBuilder(final NamedParameterJdbcTemplate jdbc,
            final Long shipmentId, final Long companyId, final Set<Long> siblings) {
        super();
        this.jdbc = jdbc;
        this.shipmentId = shipmentId;
        query = loadQuery(shipmentId, companyId, siblings);
        serializer.setDateFormat(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSS"));
    }

    /**
     * @return
     */
    private String loadQuery(final Long shipment, final Long company, final Set<Long> siblings) {
        try {
            final String str = StringUtils.getContent(
                    MainShipmentDataBuilder.class.getResource("getMainData.sql"),
                    "UTF-8");
            String q = str.replace("1387", shipment.toString()).replace("123321", company.toString());
            if (siblings.size() > 0) {
                q += " or s.id in (" + StringUtils.combine(siblings, ",") + ")";
            }
            return q;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#getPriority()
     */
    @Override
    public int getPriority() {
        return MAX_PRIORITY; //highest priority;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#build(com.visfresh.impl.singleshipment.SingleShipmentBuildContext)
     */
    @Override
    public void build(final SingleShipmentBuildContext context) {
        final SingleShipmentBean mainBean = beans.remove(shipmentId);
        if (mainBean == null) {
            return;
        }

        //create result data
        final SingleShipmentData data = new SingleShipmentData();
        context.setData(data);
        data.setBean(mainBean);

        final Set<Long> allSiblingIds = new HashSet<>();
        allSiblingIds.add(mainBean.getShipmentId());

        //add sibling beans to data.
        for (final SingleShipmentBean sibling : beans.values()) {
            data.getSiblings().add(sibling);
            allSiblingIds.add(sibling.getShipmentId());
        }

        //add siblings to each bean.
        addSiblingIds(mainBean, allSiblingIds);
        for (final SingleShipmentBean sibling : beans.values()) {
            addSiblingIds(sibling, allSiblingIds);
        }
    }

    /**
     * @param bean
     * @param originIds
     */
    private void addSiblingIds(final SingleShipmentBean bean, final Set<Long> originIds) {
        for (final Long id : originIds) {
            if (!id.equals(bean.getShipmentId())) {
                bean.getSiblings().add(id);
            }
        }
    }

    /**
     * Clears the builder.
     */
    private void clear() {
        beans.clear();
        sessions.clear();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#fetchData()
     */
    @Override
    public void fetchData() {
        clear();

        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipmentId);

        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);
        for (final Map<String, Object> row : rows) {
            processRow(row);
        }
    }

    /**
     * @param row
     */
    private void processRow(final Map<String, Object> row) {
        final SingleShipmentBean bean = new SingleShipmentBean();
        final ShipmentSession session = getSession(row);

        bean.setAlertProfile(createAlertProfile(row));
        if (session != null) {
            final Date alertsSuppressedTime = session.getAlertsSuppressionDate();
            if (alertsSuppressedTime != null || session.isAlertsSuppressed()) {
                bean.setAlertsSuppressed(true);
                bean.setAlertsSuppressionTime(alertsSuppressedTime);
            }
            bean.setArrivalReportSent(NotificationServiceImpl.isArrivalReportSent(session));
        }

        bean.setAlertSuppressionMinutes(DaoImplBase.dbInteger(row.get("alertSuppressionMinutes")));

        bean.setArrivalNotificationWithinKm(DaoImplBase.dbInteger(row.get("arrivalNotificationWithinKm")));
        bean.setArrivalTime((Date) row.get("arrivalDate"));
        bean.setAssetNum((String) row.get("assetNum"));
        bean.setAssetType((String) row.get("assetType"));

        bean.setCommentsForReceiver((String) row.get("commentsForReceiver"));
        bean.setCompanyId(DaoImplBase.dbLong(row.get("company")));

        bean.setDevice((String) row.get("device"));
        bean.setDeviceColor((String) row.get("deviceColor"));
        bean.setDeviceName((String) row.get("deviceName"));
        bean.setExcludeNotificationsIfNoAlerts(Boolean.TRUE.equals(row.get("excludeNotificationsIfNoAlerts")));

        bean.setStatus(ShipmentStatus.valueOf((String) row.get("status")));
        bean.setTripCount(DaoImplBase.dbInteger(row.get("tripCount")));

        bean.setLatestShipment(bean.getTripCount() >= DaoImplBase.dbInteger(row.get("deviceTripCount"))
                && bean.getStatus() != ShipmentStatus.Ended);
        bean.setNoAlertsAfterArrivalMinutes(DaoImplBase.dbInteger(row.get("noAlertsAfterArrivalMinutes")));
        bean.setNoAlertsAfterStartMinutes(DaoImplBase.dbInteger(row.get("noAlertsAfterStartMinutes")));
        bean.setPalletId((String) row.get("palletId"));

        bean.setSendArrivalReport(Boolean.TRUE.equals(row.get("isSendArrivalReport")));
        bean.setSendArrivalReportOnlyIfAlerts(Boolean.TRUE.equals(row.get("sendArrivalReportOnlyIfAlerts")));
        bean.setShipmentDescription((String) row.get("description"));
        bean.setShipmentId(DaoImplBase.dbLong(row.get("id")));
        bean.setShipmentType(Boolean.TRUE.equals(row.get("autostart")) ? "Autostart": "Manual");
        bean.setShutDownAfterStartMinutes(DaoImplBase.dbInteger(row.get("shutDownAfterStartMinutes")));
        bean.setShutdownDeviceAfterMinutes(DaoImplBase.dbInteger(row.get("shutdownDeviceAfterMinutes")));
        bean.setStartTime((Date) row.get("startTime"));
        final Date eta = (Date) row.get("eta");
        if (eta != null) {
            bean.setPercentageComplete(SingleShipmentServiceImpl.getPercentageCompleted(
                    bean.getStartTime(), new Date(), eta));
            bean.setEta(eta);
        }

        //parse interim stops
        bean.getInterimStops().addAll(parseInterimStops(possibleBinary(row.get("interimStopsJson"))));
        //parse shipped from
        bean.setStartLocation(parseLocationProfileBean(
                SerializerUtils.parseJson(possibleBinary(row.get("shippedFromJson")))));
        //parse shipped to
        bean.setEndLocation(parseLocationProfileBean(
                SerializerUtils.parseJson(possibleBinary(row.get("shippedToJson")))));
        //parse alternative locations
        final String altLocJson = possibleBinary(row.get("altLocationJson"));
        if (altLocJson != null) {
            addAlternativeLocations(bean, altLocJson);
        }
        bean.getNotes().addAll(parseNotes(possibleBinary(row.get("notesJson"))));

        //arrival
        if (row.get("arrId") != null) {
            final ArrivalBean arr = createArrivalBean(row);
            bean.setArrival(arr);
        }

        //notification schedules
        //process arrival notifications
        processSchedules(bean.getArrivalNotificationSchedules(),
                row, "arrivalNotifSchedJson", "arrivalScheduleUsersJson");
        //process alert notifications
        processSchedules(bean.getAlertsNotificationSchedules(),
                row, "alertNotifSchedJson", "alertScheduleUsersJson");

        //device groups
        bean.getDeviceGroups().addAll(parseDeviceGroups(possibleBinary(row.get("deviceGroupsJson"))));

        //user access
        bean.getUserAccess().addAll(parseUserAccess(possibleBinary(row.get("userAccessJson"))));

        //company access
        bean.getCompanyAccess().addAll(parseCompanyAccess(possibleBinary(row.get("companyAccessJson"))));

        //alerts
        bean.getSentAlerts().addAll(parseAlerts(possibleBinary(row.get("alertsJson"))));

        //alert rules
        final List<TemperatureRule> rules = parseRules(possibleBinary(row.get("alertRulesJson")));
        bean.getAlertFired().addAll(toBeans(AbstractRuleEngine.getFailteredAlertRules(rules, session, true)));
        bean.getAlertYetToFire().addAll(toBeans(AbstractRuleEngine.getFailteredAlertRules(rules, session, false)));

        beans.put(bean.getShipmentId(), bean);
        sessions.put(bean.getShipmentId(), session);
    }
    /**
     * @param row
     * @return
     */
    private AlertProfileBean createAlertProfile(final Map<String, Object> row) {
        final Long apId = DaoImplBase.dbLong(row.get("apId"));
        if (apId == null) {
            return null;
        }

        final AlertProfileBean ap = new AlertProfileBean();
        //ap.id as apId,
        ap.setId(apId);
        //ap.name as apName,
        ap.setName((String) row.get("apName"));
        //ap.description as apDescription,
        ap.setDescription((String) row.get("apDescription"));
        //ap.onenterbright as onenterbright,
        ap.setWatchEnterBrightEnvironment(DaoImplBase.dbBoolean(row.get("onenterbright")));
        //ap.onenterdark as onenterdark,
        ap.setWatchEnterDarkEnvironment(DaoImplBase.dbBoolean(row.get("onenterdark")));
        //ap.onmovementstart as onmovementstart,
        ap.setWatchMovementStart(DaoImplBase.dbBoolean(row.get("onmovementstart")));
        //ap.onmovementstop as onmovementstop,
        ap.setWatchMovementStop(DaoImplBase.dbBoolean("onmovementstop"));
        //ap.onbatterylow as onbatterylow,
        ap.setWatchBatteryLow(DaoImplBase.dbBoolean(row.get("onbatterylow")));
        //ap.lowertemplimit as lowertemplimit,
        ap.setLowerTemperatureLimit(DaoImplBase.dbDouble(row.get("lowertemplimit")));
        //ap.uppertemplimit as uppertemplimit,
        ap.setUpperTemperatureLimit(DaoImplBase.dbDouble(row.get("uppertemplimit")));

        //battery low actions
        final Long lonaId = DaoImplBase.dbLong(row.get("lonaId"));
        if (lonaId != null) {
            final CorrectiveActionListBean lona = new CorrectiveActionListBean();
            lona.setId(lonaId);
            lona.setName((String) row.get("lonaName"));
            lona.setDescription((String) row.get("lonaDesc"));
            lona.getActions().addAll(parseCorrectiveActions((String) row.get("lonaActions")));
            ap.setLightOnCorrectiveActions(lona);
        }

        final Long bloaId = DaoImplBase.dbLong(row.get("bloaId"));
        if (bloaId != null) {
            final CorrectiveActionListBean bloa = new CorrectiveActionListBean();
            bloa.setId(bloaId);
            bloa.setName((String) row.get("bloaName"));
            bloa.setDescription((String) row.get("bloaDesc"));
            bloa.getActions().addAll(parseCorrectiveActions((String) row.get("bloaActions")));
            ap.setBatteryLowCorrectiveActions(bloa);
        }

        return ap;
    }

    /**
     * @param rules
     * @return
     */
    private List<AlertRuleBean> toBeans(final List<AlertRule> rules) {
        final List<AlertRuleBean> filtered = new LinkedList<>();
        for (final AlertRule r : rules) {
            filtered.add(new TemperatureRuleBean((TemperatureRule) r));
        }
        return filtered;
    }
    /**
     * @param data
     * @return
     */
    private List<TemperatureRule> parseRules(final String data) {
        final List<TemperatureRule> rules = new LinkedList<>();
        if (data != null) {
            for (final JsonElement e : SerializerUtils.parseJson(data).getAsJsonArray()) {
                final JsonObject json = e.getAsJsonObject();
                final TemperatureRule rule = new TemperatureRule();
                rule.setId(AbstractJsonSerializer.asLong(json.get("id")));
                //        'type', r.type,
                rule.setType(AlertType.valueOf(AbstractJsonSerializer.asString(json.get("type"))));
                //        't', r.temp,
                rule.setTemperature(AbstractJsonSerializer.asDouble(json.get("t")));
                //        'timeout', r.timeout,
                rule.setTimeOutMinutes(AbstractJsonSerializer.asInt(json.get("timeout")));
                //        'cumulative', IF(r.cumulative, 'true', false),
                rule.setCumulativeFlag(AbstractJsonSerializer.asBoolean(json.get("cumulative")));
                //        'maxrates', r.maxrateminutes,
                rule.setMaxRateMinutes(AbstractJsonSerializer.asInteger(json.get("maxrates")));

                final CorrectiveActionList actions = new CorrectiveActionList();
                //        'actionsId', r.corractions,
                actions.setId(AbstractJsonSerializer.asLong(json.get("actionsId")));
                //        'actionsName', ca.name,
                actions.setName(AbstractJsonSerializer.asString(json.get("actionsName")));
                //        'actionsDesc', ca.description,
                actions.setDescription(AbstractJsonSerializer.asString(json.get("actionsDesc")));
                //        'actionsActions', ca.actions
                final String aa = AbstractJsonSerializer.asString(json.get("actionsActions"));
                actions.getActions().addAll(parseCorrectiveActions(aa));

                rule.setCorrectiveActions(actions);
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * @param actions
     * @param str
     */
    protected List<CorrectiveAction> parseCorrectiveActions(final String str) {
        final List<CorrectiveAction> actions = new LinkedList<>();
        if (str != null) {
            for (final JsonElement ae : SerializerUtils.parseJson(str).getAsJsonArray()) {
                actions.add(serializer.parseCorrectiveAction(ae));
            }
        }
        return actions;
    }

    /**
     * @param data
     * @return
     */
    private List<ShipmentUserDto> parseUserAccess(final String data) {
        if (data != null) {
            return serializer.parseUserAccessArray(SerializerUtils.parseJson(data).getAsJsonArray());
        }
        return new LinkedList<>();
    }
    /**
     * @param data
     * @return
     */
    private List<ShipmentCompanyDto> parseCompanyAccess(final String data) {
        if (data != null) {
            return serializer.parseCompanyAccessArray(SerializerUtils.parseJson(data).getAsJsonArray());
        }
        return new LinkedList<>();
    }
    /**
     * @param data
     * @return
     */
    private List<AlertBean> parseAlerts(final String data) {
        final List<AlertBean> alerts = new LinkedList<>();
        if (data != null) {
            for (final JsonElement e : SerializerUtils.parseJson(data).getAsJsonArray()) {
                alerts.add(serializer.parseAlertBean(e));
            }
        }
        return alerts;
    }
    /**
     * @param data
     * @return
     */
    private List<DeviceGroupDto> parseDeviceGroups(final String data) {
        final List<DeviceGroupDto> groups = new LinkedList<>();
        if (data != null) {
            for (final JsonElement e : SerializerUtils.parseJson(data).getAsJsonArray()) {
                groups.add(serializer.parseDeviceGroupDto(e));
            }
        }
        return groups;
    }

    /**
     * @param bean
     * @param altLocJson
     */
    protected void addAlternativeLocations(final SingleShipmentBean bean, final String altLocJson) {
        final JsonArray array = SerializerUtils.parseJson(altLocJson).getAsJsonArray();
        for (final JsonElement e : array) {
            final JsonObject json = e.getAsJsonObject();
            final LocationProfileBean locBean = parseLocationProfileBean(json.get("location"));

            final String loctype = json.get("locType").getAsString();
            if (loctype.equals("from")) {
                bean.getStartLocationAlternatives().add(locBean);
            } else if (loctype.equals("to")) {
                bean.getEndLocationAlternatives().add(locBean);
            } else if (loctype.equals("interim")) {
                bean.getInterimLocationAlternatives().add(locBean);
            }
        }
    }

    /**
     * @param row
     * @return
     */
    protected ArrivalBean createArrivalBean(final Map<String, Object> row) {
        final ArrivalBean arr = new ArrivalBean();
        arr.setId(DaoImplBase.dbLong(row.get("arrId")));
        arr.setMettersForArrival(DaoImplBase.dbInteger(row.get("arrMeters")));
        arr.setDate((Date) row.get("arrDate"));
        arr.setNotifiedAt(arr.getDate());
        arr.setTrackerEventId(DaoImplBase.dbLong(row.get("arrEvent")));
        return arr;
    }
    /**
     * @param result
     * @param row
     * @param usersJson
     * @param schedulesJson
     */
    private void processSchedules(final List<ListNotificationScheduleItem> result,
            final Map<String, Object> row, final String schedulesJson, final String usersJson) {
        final String scheduleUsersJson = possibleBinary(row.get(usersJson));
        final JsonArray userArray;
        if (scheduleUsersJson != null) {
            userArray = SerializerUtils.parseJson(scheduleUsersJson).getAsJsonArray();
        } else {
            userArray = new JsonArray();
        }

        final String jsonStr = possibleBinary(row.get(schedulesJson));
        if (jsonStr != null) {
            final Map<Long, List<String>> scheduleUsers = new HashMap<>();

            //create schedule users map
            for (final JsonElement e : userArray) {
                final JsonObject json = e.getAsJsonObject();
                //'schedule', sched.id,
                //'firstName', u.firstname,
                //'lastName', u.lastname
                final Long schedule = json.get("schedule").getAsLong();
                List<String> users = scheduleUsers.get(schedule);
                if(users == null) {
                    users = new LinkedList<>();
                    scheduleUsers.put(schedule, users);
                }

                users.add(StringUtils.createFullUserName(
                        AbstractJsonSerializer.asString(json.get("firstName")),
                        AbstractJsonSerializer.asString(json.get("lastName"))));
            }

            //create notification schedules and notification users map map.
            final JsonArray scheds = SerializerUtils.parseJson(jsonStr).getAsJsonArray();
            for (final JsonElement e : scheds) {
                final JsonObject json = e.getAsJsonObject();
                //'id', sched.id,
                //'name', sched.name,
                //'description', sched.description
                final Long id = json.get("id").getAsLong();

                final ListNotificationScheduleItem item = new ListNotificationScheduleItem();
                item.setNotificationScheduleId(id);
                item.setNotificationScheduleName(AbstractJsonSerializer.asString(json.get("name")));
                item.setNotificationScheduleDescription(AbstractJsonSerializer.asString(json.get("description")));

                final List<String> users = scheduleUsers.get(id);
                if (users != null) {
                    item.setPeopleToNotify(StringUtils.combine(users, ","));
                } else {
                    item.setPeopleToNotify("");
                }

                result.add(item);
            }
        }
    }
    /**
     * @param str
     * @return
     */
    private List<NoteBean> parseNotes(final String str) {
        final List<NoteBean> notes = new LinkedList<>();

        if (str != null) {
            final JsonArray array = SerializerUtils.parseJson(str).getAsJsonArray();
            for (final JsonElement e : array) {
                final JsonObject json = e.getAsJsonObject();
                final NoteBean n = serializer.parseNoteBean(json);
                //         bean.setCreatedByName(asString(json.get("createCreatedByName")));
                n.setCreatedByName(StringUtils.createFullUserName(
                        AbstractJsonSerializer.asString(json.get("firstName")),
                        AbstractJsonSerializer.asString(json.get("lastName"))));
                notes.add(n);
            }
        }

        return notes;
    }

    /**
     * @param object
     * @return
     */
    private String possibleBinary(final Object object) {
        if (object == null) {
            return null;
        }

        if (object.getClass() == byte[].class) {
            try {
                return new String((byte[]) object, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return (String) object;
    }

    /**
     * @param e
     * @return
     */
    private LocationProfileBean parseLocationProfileBean(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonElement id = e.getAsJsonObject().get("locationId");
        if (id == null || id.isJsonNull()) {
            return null;
        }

        return serializer.parseLocationProfileBean(e);
    }

    /**
     * @param str
     * @return
     */
    private List<InterimStopBean> parseInterimStops(final String str) {
        final List<InterimStopBean> stops = new LinkedList<>();
        if (str != null) {
            final JsonArray array = SerializerUtils.parseJson(str).getAsJsonArray();
            return this.serializer.parseInterimStops(array);
        }
        return stops;
    }
    /**
     * @param row DB data row.
     * @return shipment session from row.
     */
    private ShipmentSession getSession(final Map<String, Object> row) {
        final String state = (String) row.get("session");
        if (state == null) {
            return null;
        }

        final ShipmentSession session = sessionSerializer.parseSession(state);
        session.setShipmentId(shipmentId);
        return session;
    }
}
