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
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class ShipmentBaseDao<E extends ShipmentBase> extends DaoImplBase<E, Long>
    implements DaoBase<E, Long> {

    public static final String TABLE = "shipments";
    private static final String ARRIVALNOTIFSCHEDULES_TABLE = "arrivalnotifschedules";
    private static final String ALERTNOTIFSCHEDULES_TABLE = "alertnotifschedules";

    protected static final String ID_FIELD = "id";
    protected static final String ISTEMPLATE_FIELD = "istemplate";
    protected static final String NAME_FIELD = "name";
    protected static final String DESCRIPTION_FIELD = "description";
    protected static final String ALERT_FIELD = "alert";
    protected static final String NOALERTIFCOODOWN_FIELD = "noalertsifcooldown";
    protected static final String ARRIVALNOTIFWITHIN_FIELD = "arrivalnotifwithIn";
    protected static final String NONOTIFSIFNOALERTS_FIELD = "nonotifsifnoalerts";
    protected static final String SHUTDOWNTIMEOUT_FIELD = "shutdowntimeout";
    protected static final String COMPANY_FIELD = "company";
    private static final String SHIPPEDTO_FIELD = "shippedto";
    private static final String SHIPPEDFROM_FIELD = "shippedfrom";

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
        map.put(NAME_FIELD, s.getName());
        map.put(DESCRIPTION_FIELD, s.getShipmentDescription());
        map.put(ALERT_FIELD, s.getAlertProfile().getId());
        map.put(NOALERTIFCOODOWN_FIELD, s.getAlertSuppressionDuringCoolDown());
        map.put(ARRIVALNOTIFWITHIN_FIELD, s.getArrivalNotificationWithIn());
        map.put(NONOTIFSIFNOALERTS_FIELD, s.isExcludeNotificationsIfNoAlertsFired());
        map.put(SHUTDOWNTIMEOUT_FIELD, s.getShutdownDeviceTimeOut());
        map.put(COMPANY_FIELD, s.getCompany().getId());
        map.put(SHIPPEDFROM_FIELD, s.getShippedFrom().getId());
        map.put(SHIPPEDTO_FIELD, s.getShippedTo().getId());
        return map;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final Long id) {
        clearManyToManyRefs(id);
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);

        //delete notification schedule
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<E> findAll() {
        final List<Map<String, Object>> list = runSelectScript(null);

        final Map<Long, Company> companyCache = new HashMap<Long, Company>();
        final List<E> result = new LinkedList<E>();
        for (final Map<String,Object> map : list) {
            result.add(createEntity(map, companyCache));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public E findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final List<Map<String, Object>> list = runSelectScript(id);
        return list.size() == 0 ? null : createEntity(list.get(0), new HashMap<Long, Company>());
    }
    /**
     * @param map
     * @return
     */
    protected E createEntity(final Map<String, Object> map, final Map<Long, Company> cache) {
        final E no = createEntity();

        no.setId(((Number) map.get(ID_FIELD)).longValue());
        no.setName((String) map.get(NAME_FIELD));
        no.setAlertProfile(alertProfileDao.findOne(((Number) map.get(ALERT_FIELD)).longValue()));
        no.setAlertSuppressionDuringCoolDown(((Number) map.get(NOALERTIFCOODOWN_FIELD)).intValue());
        no.setArrivalNotificationWithIn(((Number) map.get(ARRIVALNOTIFWITHIN_FIELD)).intValue());
        no.setExcludeNotificationsIfNoAlertsFired((Boolean) map.get(NONOTIFSIFNOALERTS_FIELD));
        no.setName((String) map.get(NAME_FIELD));
        no.setShipmentDescription((String) map.get(DESCRIPTION_FIELD));
        no.setShippedFrom(locationProfileDao.findOne(((Number) map.get(SHIPPEDFROM_FIELD)).longValue()));
        no.setShippedTo(locationProfileDao.findOne(((Number) map.get(SHIPPEDTO_FIELD)).longValue()));
        no.setShutdownDeviceTimeOut(((Number) map.get(SHUTDOWNTIMEOUT_FIELD)).intValue());

        final long companyId = ((Number) map.get(COMPANY_FIELD)).longValue();
        Company company = cache.get(companyId);
        if (company == null) {
            company = companyDao.findOne(companyId);
            cache.put(companyId, company);
        }
        no.setCompany(company);

        no.getAlertsNotificationSchedules().addAll(findNotificationSchedules(no, ALERTNOTIFSCHEDULES_TABLE));
        no.getArrivalNotificationSchedules().addAll(findNotificationSchedules(no, ARRIVALNOTIFSCHEDULES_TABLE));

        return no;
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
     * @param id
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);
        params.put(ISTEMPLATE_FIELD, isTemplate());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from "
                + TABLE
                + " where " + ISTEMPLATE_FIELD + "=:" + ISTEMPLATE_FIELD
                + (id == null ? "" : " and " + ID_FIELD + " = :" + ID_FIELD),
                params);
        return list;
    }
    /**
     * @return
     */
    protected abstract boolean isTemplate();
}
