/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.visfresh.constants.ShipmentAuditConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.dao.ShipmentAuditDao;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentAuditDaoImpl extends DaoImplBase<ShipmentAuditItem, Long>
        implements ShipmentAuditDao {
    private static final String TABLE = "shipmentaudits";

    private static final String ID = "id";
    private static final String TIME = "time";
    private static final String USER = "user";
    private static final String SHIPMENT = "shipment";
    private static final String ACTION = "action";
    private static final String INFO = "info";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public ShipmentAuditDaoImpl() {
        super();
        propertyToDbFields.put(ShipmentAuditConstants.ID, ID);
        propertyToDbFields.put(ShipmentAuditConstants.TIME, TIME);
        propertyToDbFields.put(ShipmentAuditConstants.USER_ID, USER);
        propertyToDbFields.put(ShipmentAuditConstants.SHIPMENT_ID, SHIPMENT);
        propertyToDbFields.put(ShipmentAuditConstants.ACTION, ACTION);
        propertyToDbFields.put(ShipmentAuditConstants.ADDITIONAL_INFO, INFO);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<Long>("ShipmentAuditDao",
                1000, defaultCacheTimeSeconds, 5 * defaultCacheTimeSeconds);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends ShipmentAuditItem> S saveImpl(final S item) {
        final Map<String, Object> paramMap = createParameterMap(item);
        final LinkedList<String> fields = new LinkedList<String>(paramMap.keySet());

        String sql;
        if (item.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID);
        }

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            item.setId(keyHolder.getKey().longValue());
        }

        return item;
    }
    private Map<String, Object> createParameterMap(final ShipmentAuditItem item) {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ID, item.getId());
        paramMap.put(ACTION, item.getAction().toString());
        paramMap.put(INFO, SerializerUtils.toJson(item.getAdditionalInfo()).toString());
        paramMap.put(SHIPMENT, item.getShipmentId());
        paramMap.put(TIME, item.getTime());
        paramMap.put(USER, item.getUserId());
        return paramMap;
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
    protected ShipmentAuditItem createEntity(final Map<String, Object> map) {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setId(((Number) map.get(ID)).longValue());
        item.setAction(ShipmentAuditAction.valueOf((String) map.get(ACTION)));
        item.setShipmentId(((Number) map.get(SHIPMENT)).longValue());
        item.setTime((Date) map.get(TIME));

        final Number userId = (Number) map.get(USER);
        if (userId != null) {
            item.setUserId(userId.longValue());
        }

        final String additional = (String) map.get(INFO);
        if (additional != null) {
            final JsonElement json = SerializerUtils.parseJson(additional);
            item.getAdditionalInfo().putAll(SerializerUtils.parseStringMap(json));
        }

        return item;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final ShipmentAuditItem t, final Map<String, Object> map,
            final Map<String, Object> cache) {
        // nothing to resolve
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
}
