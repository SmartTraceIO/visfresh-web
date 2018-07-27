/**
 *
 */
package com.visfresh.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.DeviceMessage;
import com.visfresh.Location;
import com.visfresh.SystemMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SystemMessageDao {
    public static final String TABLE = "systemmessages";

    public static final String ID_FIELD = "id";
    public static final String TYPE_FIELD = "type";
    public static final String TYME_FIELD = "time";
    public static final String PROCESSOR_FIELD = "processor";
    public static final String RETRYON_FIELD = "retryon";
    public static final String NUMRETRY_FIELD = "numretry";
    public static final String MESSAGE_FIELD = "message";
    public static final String GROUP_FIELD = "group";

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    /**
     * Default constructor.
     */
    public SystemMessageDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    public SystemMessage save(final SystemMessage msg) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

            //insert
        sql = "insert into " + TABLE + " (" +
                TYME_FIELD
                + "," + TYPE_FIELD
                + "," + RETRYON_FIELD
                + "," + PROCESSOR_FIELD
                + "," + NUMRETRY_FIELD
                + "," + MESSAGE_FIELD
                + ",`" + GROUP_FIELD + "`"
             + ")" + " values("
                + ":"+ TYME_FIELD
                + ", :" + TYPE_FIELD
                + ", :" + RETRYON_FIELD
                + ", :" + PROCESSOR_FIELD
                + ", :" + NUMRETRY_FIELD
                + ", :" + MESSAGE_FIELD
                + ", :" + GROUP_FIELD
                + ")";

        paramMap.put(ID_FIELD, msg.getId());
        paramMap.put(TYME_FIELD, msg.getTime());
        paramMap.put(TYPE_FIELD, msg.getType());
        paramMap.put(RETRYON_FIELD, msg.getRetryOn());
        paramMap.put(PROCESSOR_FIELD, msg.getProcessor());
        paramMap.put(NUMRETRY_FIELD, msg.getNumberOfRetry());
        paramMap.put(MESSAGE_FIELD, msg.getMessageInfo());
        paramMap.put(GROUP_FIELD, msg.getGroup());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            msg.setId(keyHolder.getKey().longValue());
        }

        return msg;
    }

    public SystemMessage sendSystemMessageFor(final DeviceMessage e, final Location loc) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(UTC);

        final JsonObject obj = new JsonObject();
        obj.addProperty("battery", e.getBattery());
        obj.addProperty("temperature", e.getTemperature());
        obj.addProperty("time", sdf.format(e.getTime()));
        obj.addProperty("type", e.getType().name());
        if (loc != null) {
            obj.addProperty("latitude", loc.getLatitude());
            obj.addProperty("longitude", loc.getLongitude());
        }
        obj.addProperty("imei", e.getImei());
        obj.addProperty("gateway", e.getGateway());

        final SystemMessage sm = new SystemMessage();
        sm.setMessageInfo(obj.toString());
        sm.setGroup(e.getImei());
        save(sm);
        return sm;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    public SystemMessage findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final String entityName = "msg";
        final String resultPrefix = "msg_";

        final List<Map<String, Object>> list = runSelectScript(id, entityName,
                resultPrefix);
        return list.size() == 0 ? null : createSystemMessage(list.get(0), resultPrefix);
    }

    /**
     * @param id
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id,
            final String entityName, final String resultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
            "select * from "
            + TABLE + " " + entityName
            + (id == null ? "" : " where " + entityName + "." + ID_FIELD + " = :id"),
            params);
        return list;
    }
    /**
     * Create alert with unresolved references.
     * @param map parameter map.
     * @param resultPrefix
     * @return
     */
    public static SystemMessage createSystemMessage(final Map<String, Object> map, final String resultPrefix) {
        final SystemMessage m = new SystemMessage();
        m.setId(((Number) map.get(ID_FIELD)).longValue());
        m.setTime((Date) map.get(TYME_FIELD));
        m.setType((String) map.get(TYPE_FIELD));
        m.setProcessor((String) map.get(PROCESSOR_FIELD));
        m.setRetryOn((Date) map.get(RETRYON_FIELD));
        m.setNumberOfRetry(((Number) map.get(NUMRETRY_FIELD)).intValue());
        m.setMessageInfo((String) map.get(MESSAGE_FIELD));
        m.setGroup((String) map.get(GROUP_FIELD));
        return m;
    }

    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final String field : createFieldList()) {
            map.put(entityName + "." + field, resultPrefix + field);
        }
        return map;
    }
    /**
     * @return list of field names.
     */
    public static List<String> createFieldList() {
        final List<String> list = new LinkedList<String>();
        list.add(ID_FIELD);
        list.add(TYPE_FIELD);
        list.add(TYME_FIELD);
        list.add(PROCESSOR_FIELD);
        list.add(RETRYON_FIELD);
        list.add(NUMRETRY_FIELD);
        list.add(MESSAGE_FIELD);
        list.add("`" + GROUP_FIELD + "`");
        return list;
    }
}
