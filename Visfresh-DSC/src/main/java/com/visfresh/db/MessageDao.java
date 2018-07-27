/**
 *
 */
package com.visfresh.db;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MessageDao {
    //fields
    public static final String RADIO_FIELD = "radio";
    public static final String ID_FIELD = "id";
    public static final String NUMRETRY_FIELD = "numretry";
    public static final String RETRYON_FIELD = "retryon";
    public static final String STATIONS_FIELD = "stations";
    public static final String TEMPERATURE_FIELD = "temperature";
    public static final String BATTERY_FIELD = "battery";
    public static final String TIME_FIELD = "time";
    public static final String TYPE_FIELD = "type";
    public static final String IMEI_FIELD = "imei";
    public static final String LONGITUDE_FIELD = "longitude";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String PROCESSOR_FIELD = "processor";
    public static final String HUMIDITY_FIELD = "humidity";
    public static final String GATEWAY_FIELD = "gateway";

    //tables
    public static final String TABLE = "devicemsg";

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Default constructor.
     */
    public MessageDao() {
        super();
    }

    public void create(final DeviceMessage msg) {
        createDeviceMessage(msg);
    }

    public void markDeviceMessagesForProcess(final String processorId, final int limit) {
        markMessagesToProcess(TABLE, processorId, limit);
    }
    /**
     * @param tableName table name.
     * @param processorId processor ID.
     * @param limit limit.
     */
    private void markMessagesToProcess(final String tableName,
            final String processorId, final int limit) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("retryOn", new Date());

        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select id as ID from "
                + tableName
                + " where "+ PROCESSOR_FIELD + " is NULL "
                + " and not " + RETRYON_FIELD + " > :retryOn "
                + " order by "
                + TIME_FIELD + " limit " + limit, params);

        if (!list.isEmpty()) {
            //build ID list
            final StringBuilder sb = new StringBuilder();
            for (final Map<String, Object> row : list) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(row.get("ID"));
            }

            //mark
            params = new HashMap<String, Object>();
            params.put("processor", processorId);
            jdbcTemplate.update("update " + tableName
                    + " set " + PROCESSOR_FIELD + "=:processor where  id in (" + sb + ")"
                    , params);
        }
    }
    public List<DeviceMessage> getDeviceMessagesForProcess(final String processorId) {
        //create query parameters
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("processor", processorId);

        //run query
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select * from " + TABLE + " where "
                        + PROCESSOR_FIELD + " = :processor order by " + TIME_FIELD, params);

        final List<DeviceMessage> messages = new LinkedList<DeviceMessage>();
        for (final Map<String,Object> map : list) {
            final DeviceMessage msg = new DeviceMessage();
            msg.setId(((Number) map.get(ID_FIELD)).longValue());
            msg.setImei((String) map.get(IMEI_FIELD));
            msg.setType(DeviceMessageType.valueOf((String) map.get(TYPE_FIELD)));
            msg.setTime(new Date(((Date) map.get(TIME_FIELD)).getTime()));
            msg.setBattery((Integer) map.get(BATTERY_FIELD));
            final Number humidity = (Number) map.get(HUMIDITY_FIELD);
            if (humidity != null) {
                msg.setHumidity(humidity.intValue());
            }
            msg.setRadio((String) map.get(RADIO_FIELD));
            msg.setTemperature((Float) map.get(TEMPERATURE_FIELD));
            msg.setRetryOn(new Date(((Date) map.get(RETRYON_FIELD)).getTime()));
            msg.setGateway((String) map.get(GATEWAY_FIELD));
            msg.setNumberOfRetry((Integer) map.get(NUMRETRY_FIELD));

            //parse station signals
            final String encodedStations = ((String) map.get(STATIONS_FIELD)).trim();
            if (encodedStations.length() > 0) {
                final String[] str = encodedStations.split("\n");
                for (final String s : str) {
                    msg.getStations().add(DeviceMessageParser.parseStationSignal(s));
                }
            }

            messages.add(msg);
        }

        return messages;
    }
    /**
     * @param msg device message.
     */
    private void createDeviceMessage(final DeviceMessage msg) {
        final Map<String, Object> params = createBaseMessageParameters(msg);

        //encode station signals
        final StringBuilder sb = new StringBuilder();
        for (final StationSignal station : msg.getStations()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(station.getMcc()).append('|');
            sb.append(station.getMnc()).append('|');
            sb.append(station.getLac()).append('|');
            sb.append(station.getCi()).append('|');
            sb.append(station.getLevel()).append('|');
        }

        //stations varchar(256) NOT NULL,
        params.put(STATIONS_FIELD, sb.toString());

        final GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update("INSERT INTO "
                + TABLE
            + "(" + IMEI_FIELD
            + ", " + TYPE_FIELD
            + ", " + TIME_FIELD
            + ", " + BATTERY_FIELD
            + ", " + HUMIDITY_FIELD
            + ", " + TEMPERATURE_FIELD
            + ", " + NUMRETRY_FIELD
            + ", " + RETRYON_FIELD
            + ", " + STATIONS_FIELD
            + ", " + RADIO_FIELD
            + ", " + GATEWAY_FIELD
            + ") "
            + "VALUES(:imei, :type, :time, :battery, :humidity, :temperature, :numretry, :readyon,"
            + " :stations, :radio, :gateway)"
            , new MapSqlParameterSource(params), holder);

        msg.setId(holder.getKey().longValue());
    }

    /**
     * @param msg
     * @return
     */
    private Map<String, Object> createBaseMessageParameters(final DeviceMessage msg) {
        final Map<String, Object> map = new HashMap<String, Object>();
        //imei varchar(15) NOT NULL,
        map.put("imei", msg.getImei());
        //type varchar(3) NOT NULL,
        map.put("type", msg.getType().name());
        //time datetime NOT NULL,
        map.put("time", msg.getTime());
        //battery int NOT NULL,
        map.put("battery", msg.getBattery());
        map.put("humidity", msg.getHumidity());
        map.put("humidity", msg.getHumidity());
        //temperature float NOT NULL,
        map.put("temperature", new Float(msg.getTemperature()));
        map.put("radio", msg.getRadio() == null ? null : msg.getRadio().name());
        //current retry number.
        map.put("numretry", msg.getNumberOfRetry());
        //the ready time.
        map.put("readyon", msg.getRetryOn());
        map.put(RADIO_FIELD, msg.getRadio() == null ? null : msg.getRadio().name());
        map.put("gateway", msg.getGateway());

        return map;
    }

    /**
     * @param msg the message.
     */
    public void delete(final DeviceMessage msg) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("msgid", msg.getId());
        jdbcTemplate.update("delete from " + TABLE
                + " where id = :msgid", params);
    }

    /**
     * @param msg message.
     */
    public void saveForRetry(final DeviceMessage msg) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("msgid", msg.getId());
        params.put("retryOn", msg.getRetryOn());
        params.put("numRetry", msg.getNumberOfRetry());

        jdbcTemplate.update("update " + TABLE + " set "
                + RETRYON_FIELD + " = :retryOn, "
                + NUMRETRY_FIELD + " = :numRetry "
                + "where " + ID_FIELD + " = :msgid", params);
    }
}
