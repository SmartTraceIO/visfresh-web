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

import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalDaoImpl extends DaoImplBase<Arrival, Long> implements ArrivalDao {
    /**
     * Table name.
     */
    public static final String TABLE = "arrivals";

    private static final String ID_FIELD = "id";
    private static final String NUMMETERS_FIELD = "nummeters";
    private static final String DATE_FIELD = "date";
    private static final String DEVICE_FIELD = "device";
    private static final String SHIPMENT_FIELD = "shipment";

    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public ArrivalDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Arrival> S save(final S arrival) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);

        if (arrival.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, arrival.getId());
        paramMap.put(DATE_FIELD, arrival.getDate());
        paramMap.put(NUMMETERS_FIELD, arrival.getNumberOfMettersOfArrival());
        paramMap.put(DEVICE_FIELD, arrival.getDevice().getId());
        paramMap.put(SHIPMENT_FIELD, arrival.getShipment().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            arrival.setId(keyHolder.getKey().longValue());
        }

        return arrival;
    }

    public List<String> getFields(final boolean addId) {
        final LinkedList<String> fields = new LinkedList<String>();
        fields.add(DATE_FIELD);
        fields.add(NUMMETERS_FIELD);
        fields.add(DEVICE_FIELD);
        fields.add(SHIPMENT_FIELD);
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
    /**
     * @return
     */
    private Map<String, String> createSelectAsMapping() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(ID_FIELD, ID_FIELD);
        map.put(DATE_FIELD, DATE_FIELD);
        map.put(NUMMETERS_FIELD, NUMMETERS_FIELD);
        map.put(SHIPMENT_FIELD, SHIPMENT_FIELD);
        return map;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ArrivalDao#getArrivals(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Arrival> getArrivals(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", shipment.getId());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        final Map<String, String> fields = createSelectAsMapping();

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE + " a"
                + " where "
                + "a." + SHIPMENT_FIELD + " =:shipment"
                + " and date >= :fromDate and date <= :toDate order by date, id",
                params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<Arrival> alerts = new LinkedList<Arrival>();
        for (final Map<String,Object> row : list) {
            final Arrival a = createEntity(row);
            resolveReferences(a, row, cache);
            alerts.add(a);
        }
        return alerts;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Arrival a, final Map<String, Object> map,
            final Map<String, Object> cache) {
        final String shipmentId = map.get(SHIPMENT_FIELD).toString();
        Shipment shipment = (Shipment) cache.get(shipmentId);
        if (shipment == null) {
            shipment = shipmentDao.findOne(Long.valueOf(shipmentId));
            cache.put(shipmentId, shipment);
        }

        a.setShipment(shipment);
        a.setDevice(shipment.getDevice());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Arrival createEntity(final Map<String, Object> map) {
        final Arrival a = new Arrival();
        a.setId(((Number) map.get(ID_FIELD)).longValue());
        a.setDate((Date) map.get(DATE_FIELD));
        a.setNumberOfMettersOfArrival(((Number) map.get(NUMMETERS_FIELD)).intValue());
        return a;
    }
}
