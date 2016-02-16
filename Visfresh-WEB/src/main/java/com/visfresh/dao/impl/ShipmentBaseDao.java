/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DaoBase;
import com.visfresh.dao.Filter;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class ShipmentBaseDao<E extends ShipmentBase> extends EntityWithCompanyDaoImplBase<E, Long>
    implements DaoBase<E, Long> {

    public static final String TABLE = "shipments";
    protected static final String ARRIVALNOTIFSCHEDULES_TABLE = "arrivalnotifschedules";
    protected static final String ALERTNOTIFSCHEDULES_TABLE = "alertnotifschedules";

    protected static final String ID_FIELD = "id";
    protected static final String ISTEMPLATE_FIELD = "istemplate";
    protected static final String NAME_FIELD = "name";
    protected static final String DESCRIPTION_FIELD = "description";
    protected static final String ALERT_FIELD = "alert";
    protected static final String NOALERTIFCOODOWN_FIELD = "noalertsifcooldown";
    protected static final String ARRIVALNOTIFWITHIN_FIELD = "arrivalnotifwithIn";
    protected static final String NONOTIFSIFNOALERTS_FIELD = "nonotifsifnoalerts";
    protected static final String SHUTDOWNTIMEOUT_FIELD = "shutdownafterarrivalminutes";
    protected static final String NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD = "noalertsafterarrivalminutes";
    protected static final String SHUTDOWN_AFTER_START_TIMOUT_FIELD = "shutdownafterstartminutes";

    protected static final String COMPANY_FIELD = "company";
    protected static final String SHIPPEDTO_FIELD = "shippedto";
    protected static final String SHIPPEDFROM_FIELD = "shippedfrom";
    protected static final String COMMENTS_FIELD = "comments";

    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private CompanyDao companyDao;

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends E> S save(final S s) {
        final Map<String, Object> paramMap = createParameterMap(s);

        String sql;

        if (s.getId() == null) {
            //insert
            sql = "insert into " + TABLE + " (" + StringUtils.combine(paramMap.keySet(), ",") + ")"
                    + " values(" + ":"+ StringUtils.combine(paramMap.keySet(), ", :") + ")";
        } else {
            //update
            //create update statements
            final StringBuilder sb = new StringBuilder();
            for (final String key : paramMap.keySet()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(key);
                sb.append("=:");
                sb.append(key);
            }

            sql = "update " + TABLE + " set " + sb + " where " + ID_FIELD + " = :" + ID_FIELD;
        }

        paramMap.put(ID_FIELD, s.getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            s.setId(keyHolder.getKey().longValue());
        }

        updateReferences(s);
        return s;
    }
    protected void updateReferences(final E s) {
        clearManyToManyRefs(s.getId());
        createManyToManyRefs(s);
    }
    /**
     * @param s
     */
    protected void createManyToManyRefs(final E s) {
        createRefs(ALERTNOTIFSCHEDULES_TABLE, s.getId(), "notification", s.getAlertsNotificationSchedules());
        createRefs(ARRIVALNOTIFSCHEDULES_TABLE, s.getId(), "notification", s.getArrivalNotificationSchedules());
    }
    /**
     * @param table
     * @param e
     */
    private <M extends EntityWithId<Long>> void createRefs(final String table,
            final Long id, final String fieldName,  final Collection<M> e) {
        final Set<Long> ids = new HashSet<Long>();
        for (final M m : e) {
            ids.add(m.getId());
        }

        for (final Long m : ids) {
            final HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("shipment", id);
            params.put(fieldName, m);
            jdbc.update("insert into " + table + " (shipment, " + fieldName + ")"
                    + " values(:shipment,:" + fieldName + ")",
                    params);
        }
    }
    /**
     * @param id
     */
    protected void clearManyToManyRefs(final Long id) {
        cleanRefs(ALERTNOTIFSCHEDULES_TABLE, id);
        cleanRefs(ARRIVALNOTIFSCHEDULES_TABLE, id);
    }
    /**
     * @param table
     * @param s
     */
    protected void cleanRefs(final String table, final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        //delete personal schedule
        jdbc.update("delete from " + table + " where shipment = :id", paramMap);
    }
    protected Map<String, Object> createParameterMap(final E s) {
        final Map<String, Object> map= new HashMap<String, Object>();
        map.put(ISTEMPLATE_FIELD, isTemplate());
        map.put(DESCRIPTION_FIELD, s.getShipmentDescription());
        map.put(ALERT_FIELD, s.getAlertProfile() == null ? null: s.getAlertProfile().getId());
        map.put(NOALERTIFCOODOWN_FIELD, s.getAlertSuppressionMinutes());
        map.put(ARRIVALNOTIFWITHIN_FIELD, s.getArrivalNotificationWithinKm());
        map.put(NONOTIFSIFNOALERTS_FIELD, s.isExcludeNotificationsIfNoAlerts());
        map.put(SHUTDOWNTIMEOUT_FIELD, s.getShutdownDeviceAfterMinutes());
        map.put(NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD, s.getNoAlertsAfterArrivalMinutes());
        map.put(SHUTDOWN_AFTER_START_TIMOUT_FIELD, s.getShutDownAfterStartMinutes());
        map.put(COMPANY_FIELD, s.getCompany().getId());
        map.put(SHIPPEDFROM_FIELD, s.getShippedFrom() == null ? null : s.getShippedFrom().getId());
        map.put(SHIPPEDTO_FIELD, s.getShippedTo() == null ? null : s.getShippedTo().getId());
        map.put(COMMENTS_FIELD, s.getCommentsForReceiver());
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<E> findAll(final Filter filter, final Sorting sorting, final Page page) {
        final Filter f = new Filter(filter);
        f.addFilter(ISTEMPLATE_FIELD, isTemplate());
        return daoBaseFindAll(f, sorting, page);
    }
    /**
     * @param f
     * @param sorting
     * @param page
     * @return
     */
    protected List<E> daoBaseFindAll(final Filter f, final Sorting sorting,
            final Page page) {
        return super.findAll(f, sorting, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getEntityCount(com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(ISTEMPLATE_FIELD, isTemplate());
        return super.getEntityCount(f);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public E findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final Filter f = new Filter();
        f.addFilter(ID_FIELD, id);
        f.addFilter(ISTEMPLATE_FIELD, isTemplate());

        final List<E> list = findAll(f, null, null);
        return list.size() == 0 ? null : list.get(0);
    }
    /**
     * @param ship
     * @param table
     * @return
     */
    private List<NotificationSchedule> findNotificationSchedules(
            final E ship, final String table) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, ship.getId());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from " + table + " where shipment =:" + ID_FIELD,
                params);

        final List<NotificationSchedule> result = new LinkedList<NotificationSchedule>();
        for (final Map<String,Object> row : list) {
            row.remove("shipment");
            final Long id = ((Number) row.entrySet().iterator().next().getValue()).longValue();
            final NotificationSchedule sched = notificationScheduleDao.findOne(id);
            if (sched != null) {
                result.add(sched);
            }
        }
        return result;
    }
    /**
     * @return
     */
    protected abstract E createEntity();

    /**
     * @return
     */
    protected abstract boolean isTemplate();
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
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
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected E createEntity(final Map<String, Object> map) {
        final E no = createEntity();

        no.setId(((Number) map.get(ID_FIELD)).longValue());
        Number id = (Number) map.get(ALERT_FIELD);
        if (id != null) {
            no.setAlertProfile(alertProfileDao.findOne(id.longValue()));
        }
        no.setAlertSuppressionMinutes(((Number) map.get(NOALERTIFCOODOWN_FIELD)).intValue());
        final Number arrivalNotifWithIn = (Number) map.get(ARRIVALNOTIFWITHIN_FIELD);
        if (arrivalNotifWithIn != null) {
            no.setArrivalNotificationWithinKm(arrivalNotifWithIn.intValue());
        }
        no.setExcludeNotificationsIfNoAlerts((Boolean) map.get(NONOTIFSIFNOALERTS_FIELD));
        no.setShipmentDescription((String) map.get(DESCRIPTION_FIELD));
        no.setCommentsForReceiver((String) map.get(COMMENTS_FIELD));
        id = ((Number) map.get(SHIPPEDFROM_FIELD));
        if (id != null) {
            no.setShippedFrom(locationProfileDao.findOne(id.longValue()));
        }
        id = ((Number) map.get(SHIPPEDTO_FIELD));
        if (id != null) {
            no.setShippedTo(locationProfileDao.findOne(id.longValue()));
        }
        final Number shutdownAfterMinutes = (Number) map.get(SHUTDOWNTIMEOUT_FIELD);
        if (shutdownAfterMinutes != null) {
            no.setShutdownDeviceAfterMinutes(shutdownAfterMinutes.intValue());
        }
        final Number noalertAfterArrivalTimeOut = (Number) map.get(NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD);
        if (noalertAfterArrivalTimeOut != null) {
            no.setNoAlertsAfterArrivalMinutes(noalertAfterArrivalTimeOut.intValue());
        }
        final Number shutDownAfterStartMinutes = (Number) map.get(SHUTDOWN_AFTER_START_TIMOUT_FIELD);
        if (noalertAfterArrivalTimeOut != null) {
            no.setShutDownAfterStartMinutes(shutDownAfterStartMinutes.intValue());
        }

        no.getAlertsNotificationSchedules().addAll(findNotificationSchedules(no, ALERTNOTIFSCHEDULES_TABLE));
        no.getArrivalNotificationSchedules().addAll(findNotificationSchedules(no, ARRIVALNOTIFSCHEDULES_TABLE));

        return no;
    }
}
