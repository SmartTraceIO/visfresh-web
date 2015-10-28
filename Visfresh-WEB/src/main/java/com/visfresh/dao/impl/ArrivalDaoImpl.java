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
    /**
     * ID field.
     */
    private static final String ID_FIELD = "id";
    /**
     * Number of meters for arrival.
     */
    private static final String NUMMETERS_FIELD = "nummeters";
    /**
     * Date.
     */
    private static final String DATE_FIELD = "date";
    /**
     * Reference to device.
     */
    private static final String DEVICE_FIELD = "device";
    /**
     * Reference to shipment.
     */
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
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public Arrival findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(id, entityName,
                companyEntityName, resultPrefix, companyResultPrefix,
                deviceEntityName, deviceResultPrefix);
        return list.size() == 0 ? null : createArrival(resultPrefix, deviceResultPrefix, companyResultPrefix, list.get(0));
    }
    /**
     * @param resultPrefix
     * @param deviceResultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    private Arrival createArrival(final String resultPrefix, final String deviceResultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        final Arrival a = createArrival(map, resultPrefix);
        a.setDevice(DeviceDaoImpl.createDevice(deviceResultPrefix, companyResultPrefix, map));
        a.setShipment(shipmentDao.findOne(((Number) map.get(resultPrefix + SHIPMENT_FIELD)).longValue()));
        return a;
    }

    /**
     * @param map
     * @param resultPrefix
     * @return
     */
    protected Arrival createArrival(final Map<String, Object> map,
            final String resultPrefix) {
        final Arrival a = new Arrival();
        a.setId(((Number) map.get(resultPrefix + ID_FIELD)).longValue());
        a.setDate((Date) map.get(resultPrefix + DATE_FIELD));
        a.setNumberOfMettersOfArrival(((Number) map.get(resultPrefix + NUMMETERS_FIELD)).intValue());
        return a;
    }
    /**
     * @param id
     * @param entityName
     * @param companyEntityName
     * @param resultPrefix
     * @param companyResultPrefix
     * @param deviceEntityName
     * @param deviceResultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id,
            final String entityName, final String companyEntityName,
            final String resultPrefix, final String companyResultPrefix,
            final String deviceEntityName, final String deviceResultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        final Map<String, String> companyFields = CompanyDaoImpl.createSelectAsMapping(
                companyEntityName, companyResultPrefix);
        final Map<String, String> deviceFields = DeviceDaoImpl.createSelectAsMapping(
                deviceEntityName, deviceResultPrefix);

        params.putAll(fields);
        params.putAll(companyFields);
        params.putAll(deviceFields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + ", " + buildSelectAs(deviceFields)
                + ", " + buildSelectAs(companyFields)
                + " from "
                + TABLE + " " + entityName
                + ", " + DeviceDaoImpl.TABLE + " " + deviceEntityName
                + ", " + CompanyDaoImpl.TABLE + " " + companyEntityName
                + " where "
                + entityName + "." + DEVICE_FIELD + " = "
                + deviceEntityName + "." + DeviceDaoImpl.ID_FIELD
                + " and " + deviceEntityName + "." + DeviceDaoImpl.COMPANY_FIELD + " = "
                + companyEntityName + "." + CompanyDaoImpl.ID_FIELD
                + (id == null ? "" : " and " + entityName + "." + ID_FIELD + " = :id"),
                params);
        return list;
    }
    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + ID_FIELD, resultPrefix + ID_FIELD);
        map.put(entityName + "." + DATE_FIELD, resultPrefix + DATE_FIELD);
        map.put(entityName + "." + NUMMETERS_FIELD, resultPrefix + NUMMETERS_FIELD);
        map.put(entityName + "." + SHIPMENT_FIELD, resultPrefix + SHIPMENT_FIELD);
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<Arrival> findAll() {
        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(null, entityName, companyEntityName, resultPrefix,
                companyResultPrefix, deviceEntityName, deviceResultPrefix);

        final List<Arrival> result = new LinkedList<Arrival>();
        for (final Map<String,Object> map : list) {
            result.add(createArrival(resultPrefix, deviceResultPrefix, companyResultPrefix, map));
        }
        return result;
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
        final Map<String, String> fields = createSelectAsMapping("a", "res");

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE + " a"
                + " where "
                + "a." + SHIPMENT_FIELD + " =:shipment"
                + " and date >= :fromDate and date <= :toDate order by date, id",
                params);

        final List<Arrival> alerts = new LinkedList<Arrival>();
        for (final Map<String,Object> row : list) {
            final Arrival a = createArrival(row, "res");
            a.setShipment(shipment);
            a.setDevice(shipment.getDevice());
            alerts.add(a);
        }
        return alerts;
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
