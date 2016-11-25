/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RestSessionDaoImpl extends DaoImplBase<RestSession, Long> implements RestSessionDao {
    private static final String TABLE = "restsessions";

    private static final String ID = "id";
    private static final String USER = "user";
    private static final String TOKEN = "token";
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
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<>("RestSessionDao", 10, 60, 60);
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

        final RestSession s = new RestSession();
        s.setId(((Number) map.get(ID)).longValue());
        s.setToken(token);

        return s;
    }
}
