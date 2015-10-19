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
import com.visfresh.DeviceMessageBase;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.ResolvedDeviceMessage;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MessageDao {
    //fields
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

    //tables
    public static final String DEVICE_MESSAGES_TABLE = "devicemsg";
    public static final String RESOLVED_MESSAGES_TABLE = "resolvedmsg";

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

    public void create(final DeviceMessageBase msg) {
        if (msg instanceof DeviceMessage) {
            createDeviceMessage((DeviceMessage) msg);
        } else if (msg instanceof ResolvedDeviceMessage) {
            createResolvedMessage((ResolvedDeviceMessage) msg);
        }
    }

    public void markDeviceMessagesForProcess(final String processorId, final int limit) {
        markMessagesToProcess(DEVICE_MESSAGES_TABLE, processorId, limit);
    }
    public void markResolvedMessagesForProcess(final String processorId, final int limit) {
        markMessagesToProcess(RESOLVED_MESSAGES_TABLE, processorId, limit);
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
                "select * from " + DEVICE_MESSAGES_TABLE + " where "
                        + PROCESSOR_FIELD + " = :processor order by " + TIME_FIELD, params);

        final List<DeviceMessage> messages = new LinkedList<DeviceMessage>();
        for (final Map<String,Object> map : list) {
            final DeviceMessage msg = new DeviceMessage();
            addBaseParameters(msg, map);

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
    public List<ResolvedDeviceMessage> getResolvedMessagesForProcess(final String processorId) {
        //create query parameters
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("processor", processorId);

        //run query
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "select * from " + RESOLVED_MESSAGES_TABLE
                + " where " + PROCESSOR_FIELD + " = :processor order by "
                + TIME_FIELD, params);

        final List<ResolvedDeviceMessage> messages = new LinkedList<ResolvedDeviceMessage>();
        for (final Map<String,Object> map : list) {
            final ResolvedDeviceMessage msg = new ResolvedDeviceMessage();
            addBaseParameters(msg, map);

            //parse station signals
            final Location loc = new Location();
            msg.setLocation(loc);
            loc.setLatitude(((Number) map.get(LATITUDE_FIELD)).intValue() / 10000);
            loc.setLongitude(((Number) map.get(LONGITUDE_FIELD)).intValue() / 10000);

            messages.add(msg);
        }

        return messages;
    }
    /**
     * @param msg resolved device message.
     */
    private void createResolvedMessage(final ResolvedDeviceMessage msg) {
        final Map<String, Object> params = createBaseMessageParameters(msg);

        //encode station signals
        params.put("latitude", msg.getLocation().getLatitude() * 10000);
        params.put("longitude", msg.getLocation().getLongitude() * 10000);

        final GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update("INSERT INTO "
                + RESOLVED_MESSAGES_TABLE
            + "(" + IMEI_FIELD
            + ", " + TYPE_FIELD
            + ", " + TIME_FIELD
            + ", " + BATTERY_FIELD
            + ", " + TEMPERATURE_FIELD
            + ", " + NUMRETRY_FIELD
            + ", " + RETRYON_FIELD
            + ", " + LATITUDE_FIELD
            + ", " + LONGITUDE_FIELD + ") "
            + "VALUES(:imei, :type, :time, :battery, :temperature, :numretry, :readyon, :latitude, :longitude)"
            , new MapSqlParameterSource(params), holder);

        msg.setId(holder.getKey().longValue());
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
                + DEVICE_MESSAGES_TABLE
            + "(" + IMEI_FIELD
            + ", " + TYPE_FIELD
            + ", " + TIME_FIELD
            + ", " + BATTERY_FIELD
            + ", " + TEMPERATURE_FIELD
            + ", " + NUMRETRY_FIELD
            + ", " + RETRYON_FIELD
            + ", " + STATIONS_FIELD + ") "
            + "VALUES(:imei, :type, :time, :battery, :temperature, :numretry, :readyon, :stations)"
            , new MapSqlParameterSource(params), holder);

        msg.setId(holder.getKey().longValue());
    }

    /**
     * @param msg
     * @return
     */
    private Map<String, Object> createBaseMessageParameters(final DeviceMessageBase msg) {
        final Map<String, Object> map = new HashMap<String, Object>();
        //imei varchar(15) NOT NULL,
        map.put("imei", msg.getImei());
        //type varchar(3) NOT NULL,
        map.put("type", msg.getType().name());
        //time datetime NOT NULL,
        map.put("time", msg.getTime());
        //battery int NOT NULL,
        map.put("battery", new Integer(msg.getBattery()));
        //temperature float NOT NULL,
        map.put("temperature", new Float(msg.getTemperature()));
        //current retry number.
        map.put("numretry", msg.getNumberOfRetry());
        //the ready time.
        map.put("readyon", msg.getRetryOn());

        return map;
    }
    /**
     * @param msg
     * @param map
     */
    private void addBaseParameters(final DeviceMessageBase msg,
            final Map<String, Object> map) {
        msg.setId(((Number) map.get(ID_FIELD)).longValue());
        msg.setImei((String) map.get(IMEI_FIELD));
        msg.setType(DeviceMessageType.valueOf((String) map.get(TYPE_FIELD)));
        msg.setTime(new Date(((Date) map.get(TIME_FIELD)).getTime()));
        msg.setBattery((Integer) map.get(BATTERY_FIELD));
        msg.setTemperature((Float) map.get(TEMPERATURE_FIELD));
        msg.setRetryOn(new Date(((Date) map.get(RETRYON_FIELD)).getTime()));
        msg.setNumberOfRetry((Integer) map.get(NUMRETRY_FIELD));
    }

    /**
     * @param msg the message.
     */
    public void delete(final DeviceMessageBase msg) {
        String table = null;
        if (msg instanceof ResolvedDeviceMessage) {
            table = RESOLVED_MESSAGES_TABLE;
        } else {
            table = DEVICE_MESSAGES_TABLE;
        }

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("msgid", msg.getId());
        jdbcTemplate.update("delete from " + table + " where id = :msgid", params);
    }

    /**
     * @param msg message.
     */
    public void saveForRetry(final DeviceMessageBase msg) {
        String table = null;
        if (msg instanceof ResolvedDeviceMessage) {
            table = RESOLVED_MESSAGES_TABLE;
        } else {
            table = DEVICE_MESSAGES_TABLE;
        }

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("msgid", msg.getId());
        params.put("retryOn", msg.getRetryOn());
        params.put("numRetry", msg.getNumberOfRetry());

        jdbcTemplate.update("update " + table + " set "
                + RETRYON_FIELD + " = :retryOn, "
                + NUMRETRY_FIELD + " = :numRetry "
                + "where " + ID_FIELD + " = :msgid", params);
    }
}
