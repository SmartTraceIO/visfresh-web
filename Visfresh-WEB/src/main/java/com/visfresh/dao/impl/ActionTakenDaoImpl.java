/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ActionTakenDao;
import com.visfresh.dao.Filter;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.lists.ShortListUserItem;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ActionTakenDaoImpl extends DaoImplBase<ActionTakenView, ActionTaken, Long> implements ActionTakenDao {
    public static final String TABLE = "actiontakens";

    //action taken fields
    private static final String ID = "id";
    private static final String ALERT = "alert";
    private static final String CONFIRMED_BY = "confirmedby";
    private static final String VERIFIED_BY = "verifiedby";
    private static final String ACTION = "action";
    private static final String COMMENTS = "comments";
    private static final String TIME = "time";

    //action taken view fields
    private static final String SHIPMENT_TRIP_COUNT = "shipmentTripCount";
    private static final String SHIPMENT_SN = "shipmentSn";
    private static final String VERIFIED_BY_FIRSTNAME = "verifiedByFirstName";
    private static final String VERIFIED_BY_LASTTNAME = "verifiedByLastName";
    private static final String VERIFIED_BY_EMAIL = "verifiedByEmail";
    private static final String CONFIRMED_BY_FIRSTNAME = "confirmedByFirstName";
    private static final String CONFIRMED_BY_LASTNAME = "confirmedByLastName";
    private static final String CONFIRMED_BY_EMAIL = "confirmedByEmail";

    //alert
    private static final String ALERT_TIME = "alertTime";
    private static final String ALERT_TYPE = "type";

    //temperature alert fields
    private static final String ALERT_TEMPERATURE = "temperature";
    private static final String ALERT_MINUTES = "minutes";
    private static final String ALERT_CUMULATIVE = "cumulative";

    /**
     * Default constructor.
     */
    public ActionTakenDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<Long>("ActionTakenDao", 1000, defaultCacheTimeSeconds, 2 * defaultCacheTimeSeconds);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <A extends ActionTaken> A saveImpl(final A t) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        final Set<String> fields = getFields();

        final StringBuilder sql = new StringBuilder("insert into " + TABLE + " ("
                + StringUtils.combine(fields, ",")
                + ") values (:"
                + StringUtils.combine(fields, ",:")
                + ") ON DUPLICATE KEY UPDATE ");

        fields.remove(ID);
        final Iterator<String> iter = fields.iterator();
        while (iter.hasNext()) {
            final String field = iter.next();
            sql.append(field + "= :" + field);

            if (iter.hasNext()) {
                sql.append(",");
            }
        }

        paramMap.put(ID, t.getId());
        paramMap.put(ALERT, t.getAlert());
        paramMap.put(CONFIRMED_BY, t.getConfirmedBy());
        paramMap.put(VERIFIED_BY, t.getVerifiedBy());
        paramMap.put(ACTION, t.getAction());
        paramMap.put(COMMENTS, t.getComments());
        paramMap.put(TIME, t.getTime());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql.toString(), new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            t.setId(keyHolder.getKey().longValue());
        }

        return t;
    }

    private Set<String> getFields() {
        final Set<String> fields = new HashSet<>();
        fields.add(ID);
        fields.add(ALERT);
        fields.add(CONFIRMED_BY);
        fields.add(VERIFIED_BY);
        fields.add(ACTION);
        fields.add(COMMENTS);
        fields.add(TIME);
        return fields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected ActionTakenView createEntity(final Map<String, Object> map) {
        final ActionTakenView t = new ActionTakenView();
        t.setAction((String) map.get(ACTION));
        t.setAlert(asLong(map.get(ALERT)));
        t.setComments((String) map.get(COMMENTS));
        t.setConfirmedBy(asLong(map.get(CONFIRMED_BY)));
        t.setId(asLong(map.get(ID)));
        t.setTime((Date) map.get(TIME));
        t.setVerifiedBy(asLong(map.get(VERIFIED_BY)));

        t.setAlertTime((Date) map.get(ALERT_TIME));
        t.setAlertRule(createAlertRule(map));
        t.setConfirmedByEmail((String) map.get(CONFIRMED_BY_EMAIL));
        t.setConfirmedByName(ShortListUserItem.buildFullName(
                (String) map.get(CONFIRMED_BY_FIRSTNAME), (String) map.get(CONFIRMED_BY_LASTNAME)));
        t.setVerifiedByEmail((String) map.get(VERIFIED_BY_EMAIL));
        t.setVerifiedByName(ShortListUserItem.buildFullName(
                (String) map.get(VERIFIED_BY_FIRSTNAME), (String) map.get(VERIFIED_BY_LASTTNAME)));
        t.setShipmentSn((String) map.get(SHIPMENT_SN));
        t.setShipmentTripCount(((Number) map.get(SHIPMENT_TRIP_COUNT)).intValue());
        return t;
    }
    /**
     * @param map
     * @return
     */
    private AlertRule createAlertRule(final Map<String, Object> map) {
        final Number temperature = (Number) map.get(ALERT_TEMPERATURE);
        if (temperature != null) {
            final TemperatureRule r = new TemperatureRule();
            r.setType(AlertType.valueOf((String) map.get(ALERT_TYPE)));
            r.setTemperature(temperature.doubleValue());
            r.setCumulativeFlag((Boolean) map.get(ALERT_CUMULATIVE));
            r.setTimeOutMinutes(((Number) map.get(ALERT_MINUTES)).intValue());
            return r;
        } else {
            return new AlertRule(AlertType.valueOf((String) map.get(ALERT_TYPE)));
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    protected SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()) {
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.SelectAllSupport#buildSelectBlockForFindAll(com.visfresh.dao.Filter)
             */
            @Override
            protected String buildSelectBlockForFindAll(final Filter filter) {
                return "select actiontakens.*,"
                        + "\ns.tripcount as " + SHIPMENT_TRIP_COUNT
                        + "\n, substring(s.device, -7, 6) " + SHIPMENT_SN
                        + "\n, vu.firstname as " + VERIFIED_BY_FIRSTNAME
                        + "\n, vu.lastname as " + VERIFIED_BY_LASTTNAME
                        + "\n, vu.email as " + VERIFIED_BY_EMAIL
                        + "\n, cu.firstname as " + CONFIRMED_BY_FIRSTNAME
                        + "\n, cu.lastname as " + CONFIRMED_BY_LASTNAME
                        + "\n, cu.email as " + CONFIRMED_BY_EMAIL
                        + "\n, a.date as " + ALERT_TIME
                        + "\n, a.type as " + ALERT_TYPE
                        + "\n, r.temp as " + ALERT_TEMPERATURE
                        + "\n, r.cumulative as " + ALERT_CUMULATIVE
                        + "\n, r.timeout as " + ALERT_MINUTES
                        + "\nfrom actiontakens"
                        + "\njoin alerts a on a.id = actiontakens.alert"
                        + "\njoin shipments s on s.id = a.shipment"
                        + "\nleft outer join users vu on vu.id = actiontakens.verifiedby"
                        + "\njoin users cu on cu.id = actiontakens.confirmedby"
                        + "\nleft outer join temperaturerules r on r.id = a.rule";
            }
        };
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ActionTakenDao#findByShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public List<ActionTakenView> findByShipment(final Shipment shipment) {
        final DefaultCustomFilter cf = new DefaultCustomFilter();
        cf.addValue("actionTaken_shipment", shipment.getId());
        cf.setFilter("s.id = :actionTaken_shipment");

        final Filter f = new Filter();
        f.addFilter("actionTaken_shipment", cf);
        return findAll(f, null, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ActionTakenDao#findOne(java.lang.Long, com.visfresh.entities.Company)
     */
    @Override
    public ActionTakenView findOne(final Long id, final Company company) {
        final DefaultCustomFilter cf = new DefaultCustomFilter();
        cf.addValue("actionTaken_company", company.getId());
        cf.setFilter("s.company = :actionTaken_company");

        final Filter f = new Filter();
        f.addFilter("actionTaken_company", cf);
        final List<ActionTakenView> all = findAll(f, null, null);
        return all.size() == 0 ? null : all.get(0);
    }
    /**
     * @param object
     * @return
     */
    private Long asLong(final Object object) {
        final Number n = (Number) object;
        return n == null ? null : n.longValue();
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
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final ActionTaken t,
            final Map<String, Object> map, final Map<String, Object> cache) {
    }
}
