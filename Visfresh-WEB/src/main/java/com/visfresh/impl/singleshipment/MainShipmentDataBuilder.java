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
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.impl.services.NotificationServiceImpl;
import com.visfresh.impl.services.SingleShipmentServiceImpl;
import com.visfresh.io.json.ShipmentSessionSerializer;
import com.visfresh.io.json.SingleShipmentBeanSerializer;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.lists.ListNotificationScheduleItem;
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
     */
    public MainShipmentDataBuilder(final NamedParameterJdbcTemplate jdbc,
            final Long shipmentId, final Long companyId) {
        super();
        this.jdbc = jdbc;
        this.shipmentId = shipmentId;
        query = loadQuery(shipmentId, companyId);
        serializer.setDateFormat(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSS"));
    }

    /**
     * @return
     */
    private String loadQuery(final Long shipment, final Long company) {
        try {
            final String str = StringUtils.getContent(
                    MainShipmentDataBuilder.class.getResource("getMainData.sql"),
                    "UTF-8");
            return str.replace("1387", shipment.toString()).replace("123321", company.toString());
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

        if (session != null) {
            final Date alertsSuppressedTime = session.getAlertsSuppressionDate();
            if (alertsSuppressedTime != null || session.isAlertsSuppressed()) {
                bean.setAlertsSuppressed(true);
                bean.setAlertsSuppressionTime(alertsSuppressedTime);
            }
            bean.setArrivalReportSent(NotificationServiceImpl.isArrivalReportSent(session));
        }

        bean.setAlertSuppressionMinutes(asInteger(row.get("alertSuppressionMinutes")));

        bean.setArrivalNotificationWithinKm(asInteger(row.get("arrivalNotificationWithinKm")));
        bean.setArrivalTime((Date) row.get("arrivalDate"));
        bean.setAssetNum((String) row.get("assetNum"));
        bean.setAssetType((String) row.get("assetType"));

        bean.setCommentsForReceiver((String) row.get("commentsForReceiver"));
        bean.setCompanyId(asLong(row.get("company")));

        bean.setDevice((String) row.get("device"));
        bean.setDeviceColor((String) row.get("deviceColor"));
        bean.setDeviceName((String) row.get("deviceName"));
        bean.setExcludeNotificationsIfNoAlerts(Boolean.TRUE.equals(row.get("excludeNotificationsIfNoAlerts")));

        bean.setStatus(ShipmentStatus.valueOf((String) row.get("status")));
        bean.setTripCount(asInteger(row.get("tripCount")));

        bean.setLatestShipment(bean.getTripCount() >= asInteger(row.get("deviceTripCount"))
                && bean.getStatus() != ShipmentStatus.Ended);
        bean.setNoAlertsAfterArrivalMinutes(asInteger(row.get("noAlertsAfterArrivalMinutes")));
        bean.setNoAlertsAfterStartMinutes(asInteger(row.get("noAlertsAfterStartMinutes")));
        bean.setPalletId((String) row.get("palletId"));

        bean.setSendArrivalReport(Boolean.TRUE.equals(row.get("isSendArrivalReport")));
        bean.setSendArrivalReportOnlyIfAlerts(Boolean.TRUE.equals(row.get("sendArrivalReportOnlyIfAlerts")));
        bean.setShipmentDescription((String) row.get("description"));
        bean.setShipmentId(asLong(row.get("id")));
        bean.setShipmentType(Boolean.TRUE.equals(row.get("autostart")) ? "Autostart": "Manual");
        bean.setShutDownAfterStartMinutes(asInteger(row.get("shutDownAfterStartMinutes")));
        bean.setShutdownDeviceAfterMinutes(asInteger(row.get("shutdownDeviceAfterMinutes")));
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
        bean.getNotes().addAll(parseNotes(possibleBinary(row.get("notesJson"))));

        //arrival
        if (row.get("arrId") != null) {
            final ArrivalBean arr = new ArrivalBean();
            arr.setId(asLong(row.get("arrId")));
            arr.setMettersForArrival(asInteger(row.get("arrMeters")));
            arr.setDate((Date) row.get("arrDate"));
            arr.setNotifiedAt(arr.getDate());
            arr.setTrackerEventId(asLong(row.get("arrEvent")));
            bean.setArrival(arr);
        }

        //notification schedules
        //process arrival notifications
        processSchedules(bean.getArrivalNotificationSchedules(),
                row, "arrivalNotifSchedJson", "arrivalScheduleUsersJson");
        //process alert notifications
        processSchedules(bean.getAlertsNotificationSchedules(),
                row, "alertNotifSchedJson", "alertScheduleUsersJson");

        beans.put(bean.getShipmentId(), bean);
        sessions.put(bean.getShipmentId(), session);
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
                        asString(json.get("firstName")),
                        asString(json.get("lastName"))));
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
                item.setNotificationScheduleName(asString(json.get("name")));
                item.setNotificationScheduleDescription(asString(json.get("description")));

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
                        asString(json.get("firstName")),
                        asString(json.get("lastName"))));
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
