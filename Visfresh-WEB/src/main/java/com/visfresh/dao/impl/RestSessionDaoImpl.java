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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.Filter;
import com.visfresh.dao.RestSessionDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RestSessionDaoImpl extends DaoImplBase<RestSession, RestSession, Long> implements RestSessionDao {
    private static final String TABLE = "restsessions";
    private static final String PROPERTIES = "restproperties";

    private static final String ID = "id";
    private static final String USER = "user";
    private static final String TOKEN = "token";
    private static final String CLIENT = "client";
    private static final String EXPIRED_ON = "expiredon";
    private static final String CREATED_ON = "createdon";

    private final Map<String, String> propertyToDbMap = new HashMap<String, String>();

    @Autowired
    private UserDao userDao;
    /**
     * Default constructor.
     */
    public RestSessionDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends RestSession> S saveImpl(final S session) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(ID, session.getId());
        paramMap.put(USER, session.getUser().getId());
        paramMap.put(TOKEN, session.getToken().getToken());
        paramMap.put(CLIENT, session.getToken().getClientInstanceId());
        paramMap.put(EXPIRED_ON, session.getToken().getExpirationTime());
        paramMap.put(CREATED_ON, session.getToken().getCreatedTime());

        String sql;

        final List<String> fields = new LinkedList<>(paramMap.keySet());
        if (findOne(session.getId()) == null) {
            //insert
            fields.remove(ID);
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID);
        }

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            session.setId(keyHolder.getKey().longValue());
        }

        saveProperties(session);

        return session;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbMap;
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
        return ID;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final RestSession t, final Map<String, Object> row,
            final Map<String, Object> cache) {
        //resolve shipment
        final Number shipmentId = (Number) row.get(USER);
        if (shipmentId != null) {
            final String shipmentKey = "user_" + shipmentId;

            User user = (User) cache.get(shipmentKey);
            if (user == null) {
                user = userDao.findOne(shipmentId.longValue());
                cache.put(shipmentKey, user);
            }
            t.setUser(user);
        }

        //load properties
        loadProperties(t);
    }
    /**
     * @param session
     */
    private void saveProperties(final RestSession session) {
        //remove redundant references
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("session", session.getId());

        //delete personal schedule
        String sql = "delete from " + PROPERTIES + " where session = :session";

        final Set<String> keys = session.getPropertyKeys();
        if (keys.size() > 0) {
            final List<String> values = new LinkedList<>();
            int i = 0;
            for (final String key : keys) {
                final String name = "name_" + i;
                final String value = "value_" + i;
                paramMap.put(name, key);
                paramMap.put(value, session.getProperty(key));

                values.add(":" + name);
                i++;
            }
            sql += " and not name in (" + StringUtils.combine(values, ",") + ")";
        }

        jdbc.update(sql, paramMap);

        //add new references
        if (keys.size() > 0) {
            sql = "insert ignore into " + PROPERTIES + " (session, name, value) values ";

            final List<String> values = new LinkedList<>();
            for (int i = 0; i < keys.size(); i++) {
                final String name = "name_" + i;
                final String value = "value_" + i;

                values.add("(:session,:" + name + ", :" + value + ")");
            }

            jdbc.update(sql + StringUtils.combine(values, ",")
                + " on duplicate key update value = values(value)", paramMap);
        }
    }

    /**
     * @param session
     */
    private void loadProperties(final RestSession session) {
        final Map<String, Object> params = new HashMap<>();
        params.put("session", session.getId());

        final String query = "select name as name, value as value from " + PROPERTIES
                + " where session = :session";
        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);
        for (final Map<String, Object> row : rows) {
            session.putProperty((String) row.get("name"), (String) row.get("value"));
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.RestSessionDao#findByToken(java.lang.String)
     */
    @Override
    public RestSession findByToken(final String token) {
        final Filter f = new Filter();
        f.addFilter(TOKEN, token);

        final List<RestSession> all = findAll(f, null, null);
        return all.isEmpty() ? null : all.get(0);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected RestSession createEntity(final Map<String, Object> map) {
        final AuthToken token = new AuthToken((String) map.get(TOKEN));
        token.setExpirationTime((Date) map.get(EXPIRED_ON));
        token.getCreatedTime().setTime(((Date) map.get(CREATED_ON)).getTime());
        token.setClientInstanceId((String) map.get(CLIENT));

        final RestSession s = new RestSession();
        s.setId(((Number) map.get(ID)).longValue());
        s.setToken(token);

        return s;
    }
}
