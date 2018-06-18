/**
 *
 */
package com.visfresh.dao.impl.shipment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DaoBase;
import com.visfresh.dao.Filter;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.UserDao;
import com.visfresh.dao.impl.EntityWithCompanyDaoImplBase;
import com.visfresh.dao.impl.SelectAllSupport;
import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.User;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class ShipmentBaseDao<V extends E, E extends ShipmentBase> extends EntityWithCompanyDaoImplBase<V, E, Long>
    implements DaoBase<V, E, Long> {

    /**
     *
     */
    private static final String EXTERNALCOMPANIES_TABLE = "externalcompanies";
    /**
     *
     */
    private static final String EXTERNALUSERS_TABLE = "externalusers";
    public static final String TABLE = "shipments";
    public static final String ARRIVALNOTIFSCHEDULES_TABLE = "arrivalnotifschedules";
    public static final String ALERTNOTIFSCHEDULES_TABLE = "alertnotifschedules";

    public static final String ID_FIELD = "id";
    protected static final String ISTEMPLATE_FIELD = "istemplate";
    public static final String NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String ALERT_PROFILE_FIELD = "alert";
    protected static final String NOALERTIFCOODOWN_FIELD = "noalertsifcooldown";
    protected static final String ARRIVALNOTIFWITHIN_FIELD = "arrivalnotifwithIn";
    protected static final String NONOTIFSIFNOALERTS_FIELD = "nonotifsifnoalerts";
    protected static final String SEND_ARRIVAL_REPORT_FIELD = "arrivalreport";
    protected static final String ARRIVAL_REPORT_ONLYIFALERTS_FIELD = "arrivalreportonlyifalerts";

    protected static final String SHUTDOWNTIMEOUT_FIELD = "shutdownafterarrivalminutes";
    protected static final String NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD = "noalertsafterarrivalminutes";
    protected static final String NOALERT_AFTER_START_TIMOUT_FIELD = "noalertsafterstartminutes";
    protected static final String SHUTDOWN_AFTER_START_TIMOUT_FIELD = "shutdownafterstartminutes";

    protected static final String COMPANY_FIELD = "company";
    protected static final String SHIPPEDTO_FIELD = "shippedto";
    protected static final String SHIPPEDFROM_FIELD = "shippedfrom";
    protected static final String COMMENTS_FIELD = "comments";
    public static final String AUTOSTART_FIELD = "isautostart";

    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CompanyDao companyDao;

    private static class RefUpdate <ID extends Serializable & Comparable<ID>> {
        Long shipmentId;
        String tableName;
        String shipmentRefField = "shipment";
        String idRefField;
        List<ID> updated;

        /**
         * @param entityId
         * @param tableName
         * @param idRefField
         * @param shipmentRefField
         * @param updated
         */
        public RefUpdate(final Long entityId, final String tableName, final String idRefField,
                final String shipmentRefField, final List<ID> updated) {
            super();
            this.shipmentId = entityId;
            this.tableName = tableName;
            this.idRefField = idRefField;
            this.shipmentRefField = shipmentRefField;
            this.updated = updated;
        }
    }
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
        final List<RefUpdate<Long>> updates = new LinkedList<>();
        updates.add(new RefUpdate<Long>(s.getId(), ALERTNOTIFSCHEDULES_TABLE, "notification",
                "shipment", EntityUtils.getIdList(s.getAlertsNotificationSchedules())));
        updates.add(new RefUpdate<Long>(s.getId(), ARRIVALNOTIFSCHEDULES_TABLE, "notification",
                "shipment", EntityUtils.getIdList(s.getArrivalNotificationSchedules())));

        updates.add(new RefUpdate<Long>(s.getId(), EXTERNALUSERS_TABLE, "user",
                "shipment", EntityUtils.getIdList(s.getUserAccess())));
        updates.add(new RefUpdate<Long>(s.getId(), EXTERNALCOMPANIES_TABLE, "company",
                "shipment", EntityUtils.getIdList(s.getCompanyAccess())));

        for (final RefUpdate<Long> refUpdate : updates) {
            updateRefs(refUpdate);
        }
    }
    /**
     * @param refUpdate
     */
    private void updateRefs(final RefUpdate<Long> refUpdate) {
        //remove redundant references
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("shipment", refUpdate.shipmentId);

        //delete personal schedule
        String sql = "delete from " + refUpdate.tableName + " where "
                + refUpdate.shipmentRefField + " = :shipment";

        if (refUpdate.updated.size() > 0) {
            sql += " and not " + refUpdate.idRefField
                    + " in (" + StringUtils.combine(refUpdate.updated, ",") + ")";
        }

        jdbc.update(sql, paramMap);

        //add new references
        if (refUpdate.updated.size() > 0) {
            sql = "insert ignore into " + refUpdate.tableName
                    + " (" + refUpdate.shipmentRefField + ", " + refUpdate.idRefField + ") values ";

            final List<String> values = new LinkedList<>();
            int i = 0;
            for (final Long m : refUpdate.updated) {
                final String fName = "ref_" + i;
                paramMap.put(fName, m);
                values.add("(:shipment,:" + fName + ")");
                i++;
            }

            jdbc.update(sql + StringUtils.combine(values, ","), paramMap);
        }
    }
    protected Map<String, Object> createParameterMap(final E s) {
        final Map<String, Object> map= new HashMap<String, Object>();
        map.put(ISTEMPLATE_FIELD, isTemplate());
        map.put(DESCRIPTION_FIELD, s.getShipmentDescription());
        map.put(ALERT_PROFILE_FIELD, s.getAlertProfile() == null ? null: s.getAlertProfile().getId());
        map.put(NOALERTIFCOODOWN_FIELD, s.getAlertSuppressionMinutes());
        map.put(ARRIVALNOTIFWITHIN_FIELD, s.getArrivalNotificationWithinKm());
        map.put(NONOTIFSIFNOALERTS_FIELD, s.isExcludeNotificationsIfNoAlerts());
        map.put(SEND_ARRIVAL_REPORT_FIELD, s.isSendArrivalReport());
        map.put(ARRIVAL_REPORT_ONLYIFALERTS_FIELD, s.isSendArrivalReportOnlyIfAlerts());
        map.put(SHUTDOWNTIMEOUT_FIELD, s.getShutdownDeviceAfterMinutes());
        map.put(NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD, s.getNoAlertsAfterArrivalMinutes());
        map.put(NOALERT_AFTER_START_TIMOUT_FIELD, s.getNoAlertsAfterStartMinutes());
        map.put(SHUTDOWN_AFTER_START_TIMOUT_FIELD, s.getShutDownAfterStartMinutes());
        map.put(COMPANY_FIELD, s.getCompanyId());
        map.put(SHIPPEDFROM_FIELD, s.getShippedFrom() == null ? null : s.getShippedFrom().getId());
        map.put(SHIPPEDTO_FIELD, s.getShippedTo() == null ? null : s.getShippedTo().getId());
        map.put(COMMENTS_FIELD, s.getCommentsForReceiver());
        return map;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#customizeSupport(com.visfresh.dao.impl.SelectAllSupport)
     */
    @Override
    protected void customizeSupport(final SelectAllSupport support) {
        super.customizeSupport(support);

        final Filter f = new Filter();
        f.addFilter(ISTEMPLATE_FIELD, isTemplate());
        support.addFilter(f);
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
     * @param s
     * @return
     */
    private List<User> findExternalUsers(final E s) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select user from " + EXTERNALUSERS_TABLE + " where shipment =:shipment",
                params);

        final List<User> result = new LinkedList<>();
        for (final Map<String,Object> row : list) {
            final Long id = ((Number) row.get("user")).longValue();
            final User user = userDao.findOne(id);
            if (user != null) {
                result.add(user);
            }
        }
        return result;
    }
    /**
     * @param s
     * @return
     */
    private List<Company> findExternalCompanies(final E s) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select company from " + EXTERNALCOMPANIES_TABLE + " where shipment =:shipment",
                params);

        final List<Company> result = new LinkedList<>();
        for (final Map<String,Object> row : list) {
            final Long id = ((Number) row.get("company")).longValue();
            final Company company = companyDao.findOne(id);
            if (company != null) {
                result.add(company);
            }
        }
        return result;
    }
    /**
     * @return
     */
    protected abstract V createEntity();

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
    protected V createEntity(final Map<String, Object> map) {
        final V s = createEntity();

        s.setId(((Number) map.get(ID_FIELD)).longValue());
        s.setCompany(((Number) map.get(COMPANY_FIELD)).longValue());
        Number id = (Number) map.get(ALERT_PROFILE_FIELD);
        if (id != null) {
            s.setAlertProfile(alertProfileDao.findOne(id.longValue()));
        }
        s.setAlertSuppressionMinutes(((Number) map.get(NOALERTIFCOODOWN_FIELD)).intValue());
        final Number arrivalNotifWithIn = (Number) map.get(ARRIVALNOTIFWITHIN_FIELD);
        if (arrivalNotifWithIn != null) {
            s.setArrivalNotificationWithinKm(arrivalNotifWithIn.intValue());
        }
        s.setExcludeNotificationsIfNoAlerts((Boolean) map.get(NONOTIFSIFNOALERTS_FIELD));
        s.setSendArrivalReport((Boolean) map.get(SEND_ARRIVAL_REPORT_FIELD));
        s.setSendArrivalReportOnlyIfAlerts((Boolean) map.get(ARRIVAL_REPORT_ONLYIFALERTS_FIELD));

        s.setShipmentDescription((String) map.get(DESCRIPTION_FIELD));
        s.setCommentsForReceiver((String) map.get(COMMENTS_FIELD));
        id = ((Number) map.get(SHIPPEDFROM_FIELD));
        if (id != null) {
            s.setShippedFrom(locationProfileDao.findOne(id.longValue()));
        }
        id = ((Number) map.get(SHIPPEDTO_FIELD));
        if (id != null) {
            s.setShippedTo(locationProfileDao.findOne(id.longValue()));
        }
        final Number shutdownAfterMinutes = (Number) map.get(SHUTDOWNTIMEOUT_FIELD);
        if (shutdownAfterMinutes != null) {
            s.setShutdownDeviceAfterMinutes(shutdownAfterMinutes.intValue());
        }
        final Number noalertAfterArrivalTimeOut = (Number) map.get(NOALERT_AFTER_ARRIVAL_TIMOUT_FIELD);
        if (noalertAfterArrivalTimeOut != null) {
            s.setNoAlertsAfterArrivalMinutes(noalertAfterArrivalTimeOut.intValue());
        }
        final Number noalertAfterStartTimeOut = (Number) map.get(NOALERT_AFTER_START_TIMOUT_FIELD);
        if (noalertAfterStartTimeOut != null) {
            s.setNoAlertsAfterStartMinutes(noalertAfterStartTimeOut.intValue());
        }
        final Number shutDownAfterStartMinutes = (Number) map.get(SHUTDOWN_AFTER_START_TIMOUT_FIELD);
        if (shutDownAfterStartMinutes != null) {
            s.setShutDownAfterStartMinutes(shutDownAfterStartMinutes.intValue());
        }

        s.getAlertsNotificationSchedules().addAll(findNotificationSchedules(s, ALERTNOTIFSCHEDULES_TABLE));
        s.getArrivalNotificationSchedules().addAll(findNotificationSchedules(s, ARRIVALNOTIFSCHEDULES_TABLE));
        s.getUserAccess().addAll(findExternalUsers(s));
        s.getCompanyAccess().addAll(findExternalCompanies(s));
        s.setAutostart(Boolean.TRUE.equals(map.get(AUTOSTART_FIELD)));

        return s;
    }
}
