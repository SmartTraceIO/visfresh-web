/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.NotificationConstants;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.User;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationDaoImpl extends DaoImplBase<Notification, Notification, Long> implements NotificationDao {
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
     * Is read flag.
     */
    public static final String ISREAD_FIELD = "isread";
    public static final String HIDDEN_FIELD = "hidden";

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

    private final Map<String, String> propertyToDbMap = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public NotificationDaoImpl() {
        super();
        propertyToDbMap.put(NotificationConstants.PROPERTY_NOTIFICATION_ID, ID_FIELD);
        propertyToDbMap.put(NotificationConstants.PROPERTY_TYPE, TYPE_FIELD);
        propertyToDbMap.put(NotificationConstants.PROPERTY_CLOSED, ISREAD_FIELD);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#findByShipment(java.lang.Long)
     */
    @Override
    public List<Notification> findForUser(final User user, final boolean excludeLight, final Sorting sorting, final Filter filter, final Page page) {
        return findAll(createByUserFilter(user, excludeLight, filter), sorting, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#getEntityCount(com.visfresh.entities.User, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final User user, final boolean excludeLight, final Filter filter) {
        return getEntityCount(createByUserFilter(user, excludeLight, filter));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#deleteByUserAndId(com.visfresh.entities.User, java.util.List)
     */
    @Override
    public void markAsReadenByUserAndId(final User user, final Set<Long> ids) {
        if (ids.size() == 0) {
            return;
        }

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("user", user.getId());
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

        jdbc.update("update " + TABLE + " set "
                + ISREAD_FIELD + " = true where user = :user and (" + idPart + ")", paramMap);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    public SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()){
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.SelectAllSupport#buildSelectBlockForFindAll(com.visfresh.dao.Filter)
             */
            @Override
            protected String buildSelectBlockForFindAll(final Filter filter) {
                final String table = getTableName();
                return "select "
                        + table
                        + ".*"
                        + " from " + table
                        + " left outer join alerts"
                        + " on " + table + "." + TYPE_FIELD + "='" + NotificationType.Alert.name() + "'"
                        + " and alerts.id=" + table + "." + ISSUE_FIELD;
            }
            /**
             * @return
             */
            @Override
            protected String buildSelectBlockForEntityCount(final Filter filter) {
                final String table = getTableName();
                return "select count(*) as count"
                        + " from " + table
                        + " left outer join alerts"
                        + " on " + table + "." + TYPE_FIELD + "='" + NotificationType.Alert.name() + "'"
                        + " and alerts.id=" + table + "." + ISSUE_FIELD;
            }
        };
    }
    /**
     * @param user
     * @param excludeLight
     * @param originFilter origin filter.
     * @return
     */
    private Filter createByUserFilter(final User user, final boolean excludeLight, final Filter originFilter) {
        final Filter f = new Filter(originFilter);
        f.addFilter(USER_FIELD, user.getId());
        if (excludeLight) {
            final DefaultCustomFilter filter = new DefaultCustomFilter();
            filter.setFilter("(alerts.type is NULL or not alerts.type in ('"
                    + AlertType.LightOff + "', '" + AlertType.LightOn + "'))");
            f.addFilter("ExcludeLights", filter);
        }
        return f;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Notification> S saveImpl(final S no) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields();

        if (no.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, no.getId());
        paramMap.put(TYPE_FIELD, no.getType().name());
        paramMap.put(ISSUE_FIELD, no.getIssue().getId());
        paramMap.put(USER_FIELD, no.getUser().getId());
        paramMap.put(ISREAD_FIELD, no.isRead());
        paramMap.put(HIDDEN_FIELD, no.isHidden());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            no.setId(keyHolder.getKey().longValue());
        }

        return no;
    }
    /**
     * @return
     */
    private List<String> getFields() {
        final List<String> fields = new LinkedList<String>();
        fields.add(ID_FIELD);
        fields.add(TYPE_FIELD);
        fields.add(ISSUE_FIELD);
        fields.add(USER_FIELD);
        fields.add(ISREAD_FIELD);
        fields.add(HIDDEN_FIELD);
        return fields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#getForIssue(java.util.Collection)
     */
    @Override
    public List<Notification> getForIssues(final Collection<Long> ids, final NotificationType type) {
        if (ids.isEmpty()) {
            return new LinkedList<>();
        }

        final String sql = "select * from " + getTableName() + " where type = :type and "
            +  ISSUE_FIELD + " in (" + StringUtils.combine(ids, ",") + ")";

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("type", type.name());

        final List<Map<String, Object>> list = jdbc.queryForList(
                sql, params);

        final Map<String, Object> cache = params;
        final List<Notification> result = new LinkedList<Notification>();
        for (final Map<String,Object> map : list) {
            final Notification t = createEntity(map);
            resolveReferences(t, map, cache);
            result.add(t);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbMap;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Notification t, final Map<String, Object> map,
            final Map<String, Object> userCache) {
        final Long userId = (Long) map.get(USER_FIELD);
        User user = (User) userCache.get(userId.toString());
        if (user == null) {
            user = userDao.findOne(userId);
            userCache.put(userId.toString(), user);
        }
        t.setUser(user);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Notification createEntity(final Map<String, Object> row) {
        final Notification no = new Notification();
        //ID
        no.setId(((Number) row.get(ID_FIELD)).longValue());
        //type
        final NotificationType type = NotificationType.valueOf((String) row.get(TYPE_FIELD));
        no.setType(type);
        //is read
        no.setRead(Boolean.TRUE.equals(row.get(ISREAD_FIELD)));

        final long issueId = ((Number) row.get(ISSUE_FIELD)).longValue();
        if (type == NotificationType.Alert) {
            no.setIssue(alertDao.findOne(issueId));
        } else if (type == NotificationType.Arrival) {
            no.setIssue(arrivalDao.findOne(issueId));
        }
        no.setHidden((Boolean) row.get(HIDDEN_FIELD));

        return no;
    }
}
