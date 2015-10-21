/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.SystemMessageDao;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SystemMessageDaoImpl extends DaoImplBase<SystemMessage, Long> implements SystemMessageDao {
    public static final String TABLE = "systemmessages";

    public static final String ID_FIELD = "id";
    public static final String TYPE_FIELD = "type";
    public static final String TYME_FIELD = "time";
    public static final String PROCESSOR_FIELD = "processor";
    public static final String RETRYON_FIELD = "retryon";
    public static final String NUMRETRY_FIELD = "numretry";
    public static final String MESSAGE_FIELD = "message";

    /**
     * Default constructor.
     */
    public SystemMessageDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <A extends SystemMessage> A save(final A msg) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (msg.getId() == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    TYME_FIELD
                    , TYPE_FIELD
                    , RETRYON_FIELD
                    , PROCESSOR_FIELD
                    , NUMRETRY_FIELD
                    , MESSAGE_FIELD
                ) + ")" + " values("
                    + ":"+ TYME_FIELD
                    + ", :" + TYPE_FIELD
                    + ", :" + RETRYON_FIELD
                    + ", :" + PROCESSOR_FIELD
                    + ", :" + NUMRETRY_FIELD
                    + ", :" + MESSAGE_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + TYME_FIELD + "=:" + TYME_FIELD
                + "," + TYPE_FIELD + "=:" + TYPE_FIELD
                + "," + RETRYON_FIELD + "=:" + RETRYON_FIELD
                + "," + PROCESSOR_FIELD + "=:" + PROCESSOR_FIELD
                + "," + NUMRETRY_FIELD + "=:" + NUMRETRY_FIELD
                + "," + MESSAGE_FIELD + "=:" + MESSAGE_FIELD
                + " where id = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, msg.getId());
        paramMap.put(TYME_FIELD, msg.getTime());
        paramMap.put(TYPE_FIELD, msg.getType().name());
        paramMap.put(RETRYON_FIELD, msg.getRetryOn());
        paramMap.put(PROCESSOR_FIELD, msg.getProcessor());
        paramMap.put(NUMRETRY_FIELD, msg.getNumberOfRetry());
        paramMap.put(MESSAGE_FIELD, msg.getMessageInfo());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            msg.setId(keyHolder.getKey().longValue());
        }

        return msg;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
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
        m.setType(SystemMessageType.valueOf((String) map.get(TYPE_FIELD)));
        m.setProcessor((String) map.get(PROCESSOR_FIELD));
        m.setRetryOn((Date) map.get(RETRYON_FIELD));
        m.setNumberOfRetry(((Number) map.get(NUMRETRY_FIELD)).intValue());
        m.setMessageInfo((String) map.get(MESSAGE_FIELD));
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
        return list;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<SystemMessage> findAll() {
        final String entityName = "msg";
        final String resultPrefix = "msg_";

        final List<Map<String, Object>> list = runSelectScript(null, entityName, resultPrefix);

        final List<SystemMessage> result = new LinkedList<SystemMessage>();
        for (final Map<String,Object> map : list) {
            result.add(createSystemMessage(map, resultPrefix));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SystemMessageDao#selectMessagesForProcessing(java.util.Set, java.lang.String)
     */
    @Override
    public List<SystemMessage> selectMessagesForProcessing(
            final Set<SystemMessageType> messageTypes, final String processor, final int limit, final Date beforeDate) {
        // mark to process before select
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("processor", processor);
        params.put("limit", limit);
        params.put("readyOn", beforeDate);

        final StringBuilder sql = new StringBuilder("update " + TABLE + " set "
                + PROCESSOR_FIELD + " = :processor"
                + " where " + TYPE_FIELD + " in (");

        int count = 0;
        for (final SystemMessageType t : messageTypes) {
            if (count > 0) {
                sql.append(',');
            }
            final String paramName = "type_" + count;
            sql.append(":" + paramName);
            params.put(paramName, t.name());
            count++;
        }
        sql.append(") and " + PROCESSOR_FIELD + " is NULL");
        sql.append(" and " + RETRYON_FIELD + " <= :readyOn");
        sql.append(" order by " + ID_FIELD + " limit :limit");
        jdbc.update(sql.toString(), params);

        //select previously marked
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from " + TABLE
                + " where "  + PROCESSOR_FIELD + " = :processor", params);

        final LinkedList<SystemMessage> result = new LinkedList<SystemMessage>();
        for (final Map<String,Object> row : list) {
            result.add(createSystemMessage(row, ""));
        }
        return result;
    }
}
