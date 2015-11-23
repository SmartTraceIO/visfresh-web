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

import com.visfresh.constants.NotificationConstants;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
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
     * Is read flag.
     */
    public static final String ISREAD_FIELD = "isread";

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
        propertyToDbMap.put(NotificationConstants.PROPERTY_ID, ID_FIELD);
        propertyToDbMap.put(NotificationConstants.PROPERTY_TYPE, TYPE_FIELD);
        propertyToDbMap.put(NotificationConstants.PROPERTY_ISREAD, ISREAD_FIELD);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#findByShipment(java.lang.Long)
     */
    @Override
    public List<Notification> findForUser(final User user, final Sorting sorting, final Filter filter, final Page page) {
        final Filter f = new Filter(filter);
        f.addFilter(USER_FIELD, user.getId());
        return findAll(f, sorting, page);
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
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Notification> S save(final S no) {
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
        return fields;
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
     * @see com.visfresh.dao.NotificationDao#getEntityCount(com.visfresh.entities.User, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final User user, final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(USER_FIELD, user.getId());
        return getEntityCount(f);
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

        return no;
    }
}
