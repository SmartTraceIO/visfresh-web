/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ActionTakenDao;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ActionTakenDaoImpl extends DaoImplBase<ActionTakenView, ActionTaken, Long> implements ActionTakenDao {
    public static final String TABLE = "actiontakens";

    private static final String ID = "id";
    private static final String ALERT = "alert";
    private static final String CONFIRMED_BY = "confirmedby";
    private static final String VERIFIED_BY = "verifiedby";
    private static final String ACTION = "action";
    private static final String COMMENTS = "comments";
    private static final String TIME = "time";
    private static final String SHIPMENT = "shipment";

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
        paramMap.put(SHIPMENT, t.getShipment());

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
        fields.add(SHIPMENT);
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
        t.setShipment(asLong(map.get(SHIPMENT)));
        t.setTime((Date) map.get(TIME));
        t.setVerifiedBy(asLong(map.get(VERIFIED_BY)));
        return t;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    protected SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName());
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
