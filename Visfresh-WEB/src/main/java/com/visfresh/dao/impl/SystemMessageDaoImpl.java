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

import com.visfresh.dao.Filter;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SystemMessageDaoImpl extends DaoImplBase<SystemMessage, SystemMessage, Long> implements SystemMessageDao {
    public static final String TABLE = "systemmessages";

    public static final String ID_FIELD = "id";
    public static final String TYPE_FIELD = "type";
    public static final String TYME_FIELD = "time";
    public static final String PROCESSOR_FIELD = "processor";
    public static final String RETRYON_FIELD = "retryon";
    public static final String NUMRETRY_FIELD = "numretry";
    public static final String MESSAGE_FIELD = "message";
    public static final String GROUP_FIELD = "group";

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
    public <A extends SystemMessage> A saveImpl(final A msg) {
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
                    , "`" + GROUP_FIELD + "`"
                ) + ")" + " values("
                    + ":"+ TYME_FIELD
                    + ", :" + TYPE_FIELD
                    + ", :" + RETRYON_FIELD
                    + ", :" + PROCESSOR_FIELD
                    + ", :" + NUMRETRY_FIELD
                    + ", :" + MESSAGE_FIELD
                    + ", :" + GROUP_FIELD
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
                + ",`" + GROUP_FIELD + "`=:" + GROUP_FIELD
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
        paramMap.put(GROUP_FIELD, msg.getGroup());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            msg.setId(keyHolder.getKey().longValue());
        }

        return msg;
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
            result.add(createEntity(row));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.SystemMessageDao#getMessagesForGoup(com.visfresh.entities.SystemMessageType, java.lang.String, int, java.util.Date)
     */
    @Override
    public List<SystemMessage> getMessagesForGoup(final SystemMessageType messageType, final String group,
            final Date readyOn, final int batchLimit) {
        final Map<String, Object> params = new HashMap<>();
        params.put("type", messageType.name());
        params.put("group", group);
        params.put("retryOn", readyOn);
        params.put("limit", batchLimit);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from " + TABLE
                + " where `type` = :type and `group` = :group and retryon <= :retryOn limit :limit", params);

        final LinkedList<SystemMessage> result = new LinkedList<SystemMessage>();
        for (final Map<String,Object> row : list) {
            result.add(createEntity(row));
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.SystemMessageDao#findTrackerEvents(boolean)
     */
    @Override
    public List<SystemMessage> findTrackerEvents(final boolean asc) {
        final Filter filter = new Filter();
        filter.addFilter(SystemMessageDaoImpl.TYPE_FIELD, SystemMessageType.Tracker.name());
        final Sorting sorting = new Sorting(asc, SystemMessageDaoImpl.ID_FIELD);
        return findAll(filter, sorting, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return new HashMap<String, String>();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final SystemMessage t, final Map<String, Object> map,
            final Map<String, Object> cache) {
        // nothing to resolve.
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected SystemMessage createEntity(final Map<String, Object> map) {
        final SystemMessage m = new SystemMessage();
        m.setId(((Number) map.get(ID_FIELD)).longValue());
        m.setTime((Date) map.get(TYME_FIELD));
        m.setType(SystemMessageType.valueOf((String) map.get(TYPE_FIELD)));
        m.setProcessor((String) map.get(PROCESSOR_FIELD));
        m.setRetryOn((Date) map.get(RETRYON_FIELD));
        m.setNumberOfRetry(((Number) map.get(NUMRETRY_FIELD)).intValue());
        m.setMessageInfo((String) map.get(MESSAGE_FIELD));
        m.setGroup((String) map.get(GROUP_FIELD));
        return m;
    }
}
