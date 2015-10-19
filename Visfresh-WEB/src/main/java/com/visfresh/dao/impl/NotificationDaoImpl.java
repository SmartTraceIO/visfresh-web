/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationDaoImpl extends DaoImplBase<Notification, Long> implements NotificationDao {
    /**
     * Table name.
     */
    public static final String TABLE = "notifications";
    /**
     * ID field name.
     */
    public static final String ID_FIELD = "id";
    /**
     * Notification Type field name.
     */
    public static final String TYPE_FIELD = "type";
    /**
     * Reference ID to issue.
     */
    public static final String ISSUE_FIELD = "issue";
    /**
     * Reference ID to user.
     */
    public static final String USER_FIELD = "user";
    /**
     *
     */
    private static final String ID_PLACEHOLDER = "32_497803_29475";

    /**
     * User DAO.
     */
    @Autowired
    private UserDao userDao;
    /**
     * Arrival DAO.
     */
    @Autowired
    private ArrivalDao arrivalDao;
    /**
     * Alert DAO.
     */
    @Autowired
    private AlertDao alertDao;

    /**
     * Default constructor.
     */
    public NotificationDaoImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#findByShipment(java.lang.Long)
     */
    @Override
    public List<Notification> findForUser(final User user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_PLACEHOLDER, user.getLogin());

        final Map<String, String> fields = createSelectAsMapping();

        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select " + buildSelectAs(fields) + " from " + TABLE + " where " + USER_FIELD + " = :"
                        + ID_PLACEHOLDER,
                params);
        final Map<String, User> userCache = new HashMap<String, User>();
        userCache.put(user.getLogin(), user);

        final List<Notification> result = new LinkedList<Notification>();
        for (final Map<String,Object> map : list) {
            result.add(createNotification(map, userCache));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#deleteByUserAndId(com.visfresh.entities.User, java.util.List)
     */
    @Override
    public void deleteByUserAndId(final User user, final Set<Long> ids) {
        if (ids.size() == 0) {
            return;
        }

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user", user.getLogin());
        final StringBuilder idPart = new StringBuilder();
        int i = 0;
        for (final Long id: ids) {
            if (i > 0) {
                idPart.append(" or ");
            }
            idPart.append(ID_FIELD + " = :id_" + i);
            paramMap.put("id_" + i, id);
            i++;
        }

        jdbc.update("delete from " + TABLE + " where user = :user and (" + idPart + ")", paramMap);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Notification> S save(final S no) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (no.getId() == null) {
            //insert
            paramMap.put("id", no.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    TYPE_FIELD
                    , ISSUE_FIELD
                    , USER_FIELD
                ) + ")" + " values("
                    + ":"+ TYPE_FIELD
                    + ", :" + ISSUE_FIELD
                    + ", :" + USER_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + TYPE_FIELD + "=:" + TYPE_FIELD + ","
                + ISSUE_FIELD + "=:" + ISSUE_FIELD + ","
                + USER_FIELD + "=:" + USER_FIELD
                + " where " + ID_FIELD + " = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, no.getId());
        paramMap.put(TYPE_FIELD, no.getType().name());
        paramMap.put(ISSUE_FIELD, no.getIssue().getId());
        paramMap.put(USER_FIELD, no.getUser().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            no.setId(keyHolder.getKey().longValue());
        }

        return no;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public Notification findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final List<Map<String, Object>> list = runSelectScript(id);
        return list.size() == 0 ? null : createNotification(list.get(0), new HashMap<String, User>());
    }
    /**
     * @param map
     * @return
     */
    private Notification createNotification(final Map<String, Object> map,
            final Map<String, User> userCache) {
        final Notification no = new Notification();
        no.setId(((Number) map.get(ID_FIELD)).longValue());

        final NotificationType type = NotificationType.valueOf((String) map.get(TYPE_FIELD));
        no.setType(type);

        final String userName = (String) map.get(USER_FIELD);
        User user = userCache.get(userName);
        if (user == null) {
            user = userDao.findOne(userName);
            userCache.put(userName, user);
        }
        no.setUser(user);

        final long issueId = ((Number) map.get(ISSUE_FIELD)).longValue();
        if (type == NotificationType.Alert) {
            no.setIssue(alertDao.findOne(issueId));
        } else if (type == NotificationType.Arrival) {
            no.setIssue(arrivalDao.findOne(issueId));
        }

        return no;
    }
    /**
     * @param id
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_PLACEHOLDER, id);

        final Map<String, String> fields = createSelectAsMapping();

        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE
                + (id == null ? "" : " where " + ID_FIELD + " = :" + ID_PLACEHOLDER),
                params);
        return list;
    }
    /**
     * @return
     */
    private Map<String, String> createSelectAsMapping() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(ID_FIELD,  ID_FIELD);
        map.put(TYPE_FIELD, TYPE_FIELD);
        map.put(ISSUE_FIELD, ISSUE_FIELD);
        map.put(USER_FIELD, USER_FIELD);
        return map;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<Notification> findAll() {
        final List<Map<String, Object>> list = runSelectScript(null);

        final Map<String, User> userCache = new HashMap<String, User>();
        final List<Notification> result = new LinkedList<Notification>();
        for (final Map<String,Object> map : list) {
            result.add(createNotification(map, userCache));
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
}
