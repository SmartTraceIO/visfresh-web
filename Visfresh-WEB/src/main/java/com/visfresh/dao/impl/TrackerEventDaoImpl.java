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

import com.visfresh.constants.TrackerEventConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerEventDaoImpl extends DaoImplBase<TrackerEvent, Long>
    implements TrackerEventDao {

    /**
     * Table name.
     */
    public static final String TABLE = "trackerevents";

    protected static final String ID_FIELD = "id";
    protected static final String TYPE_FIELD = "type";
    protected static final String TIME_FIELD = "time";
    protected static final String BATTERY_FIELD = "battery";
    protected static final String TEMPERATURE_FIELD = "temperature";
    protected static final String LATITUDE_FIELD = "latitude";
    protected static final String LONGITUDE_FIELD = "longitude";
    protected static final String DEVICE_FIELD = "device";
    private static final String SHIPMENT_FIELD = "shipment";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceDao deviceDao;

    private final Map<String, String> propertyToDbMap = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public TrackerEventDaoImpl() {
        super();

        propertyToDbMap.put(TrackerEventConstants.PROPERTY_LONGITUDE, LONGITUDE_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_LATITUDE, LATITUDE_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_TEMPERATURE, TEMPERATURE_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_BATTERY, BATTERY_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_ID, ID_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_TIME, TIME_FIELD);
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_TYPE, TYPE_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends TrackerEvent> S save(final S event) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);
        if (event.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, event.getId());
        paramMap.put(TYPE_FIELD, event.getType().toString());
        paramMap.put(TIME_FIELD, event.getTime());
        paramMap.put(BATTERY_FIELD, event.getBattery());
        paramMap.put(TEMPERATURE_FIELD, event.getTemperature());
        paramMap.put(LATITUDE_FIELD, event.getLatitude());
        paramMap.put(LONGITUDE_FIELD, event.getLongitude());
        paramMap.put(DEVICE_FIELD, event.getDevice().getId());
        paramMap.put(SHIPMENT_FIELD, event.getShipment() == null ? null : event.getShipment().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            event.setId(keyHolder.getKey().longValue());
        }

        return event;
    }
    public static List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(TYPE_FIELD);
        fields.add(TIME_FIELD);
        fields.add(BATTERY_FIELD);
        fields.add(TEMPERATURE_FIELD);
        fields.add(LATITUDE_FIELD);
        fields.add(LONGITUDE_FIELD);
        fields.add(DEVICE_FIELD);
        fields.add(SHIPMENT_FIELD);
        if (includeId) {
            fields.add(ID_FIELD);
        }
        return fields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getEvents(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<TrackerEvent> getEvents(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", shipment.getId());
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from "
                + TABLE
                + " where "
                + SHIPMENT_FIELD + " =:shipment"
                + " and time >= :fromDate and time <= :toDate order by time, id",
                params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        for (final Map<String,Object> row : list) {
            final TrackerEvent e = createEntity(row);
            resolveReferences(e, row, cache);
            events.add(e);
        }
        return events;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getFirstHotOccurence(com.visfresh.entities.TrackerEvent, double)
     */
    @Override
    public Date getFirstHotOccurence(final TrackerEvent e, final double minimalTemperature) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("temperature", minimalTemperature);
        params.put("shipment", e.getShipment().getId());
        params.put("id", e.getId());

        //find first previous normal temperature.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id from " + TABLE + " where "
                + SHIPMENT_FIELD + " =:shipment and temperature < :temperature and id < :id order by id desc limit 1",
                params);
        if (rows.size() == 0) {
            return null;
        }

        //select from first normal temperature
        final long startId = ((Number) rows.get(0).get("id")).longValue();
        params.put("startId", startId);
        params.put("endId", e.getId());
        rows = jdbc.queryForList(
                "select time from " + TABLE + " where "
                + SHIPMENT_FIELD + " =:shipment and id > :startId and id < :endId and temperature >= :temperature"
                + " order by id limit 1",
                params);
        if (rows.size() == 0) {
            return null;
        }

        return (Date) rows.get(0).get("time");
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getFirstColdOccurence(com.visfresh.entities.TrackerEvent, double)
     */
    @Override
    public Date getFirstColdOccurence(final TrackerEvent e, final double maximalTemperature) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("temperature", maximalTemperature);
        params.put("shipment", e.getShipment().getId());
        params.put("id", e.getId());

        //find first previous normal temperature.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id from " + TABLE + " where "
                + SHIPMENT_FIELD + " =:shipment and temperature > :temperature and id < :id order by id desc limit 1",
                params);
        if (rows.size() == 0) {
            return null;
        }

        //select from first normal temperature
        final long startId = ((Number) rows.get(0).get("id")).longValue();
        params.put("startId", startId);
        params.put("endId", e.getId());
        rows = jdbc.queryForList(
                "select time from " + TABLE + " where "
                + SHIPMENT_FIELD + " =:shipment and id > :startId and id < :endId and temperature <= :temperature"
                + " order by id limit 1",
                params);
        if (rows.size() == 0) {
            return null;
        }

        return (Date) rows.get(0).get("time");
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbMap;
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
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final TrackerEvent e, final Map<String, Object> row,
            final Map<String, Object> cache) {
        final Object tmp = row.get(SHIPMENT_FIELD);
        if (tmp != null) {
            final String shipmentId = tmp.toString();
            Shipment shipment = (Shipment) cache.get(shipmentId);
            if (shipment == null) {
                shipment = shipmentDao.findOne(Long.valueOf(shipmentId));
                cache.put(shipmentId, shipment);
            }

            e.setShipment(shipment);
            e.setDevice(shipment.getDevice());
        } else {
            final String imei = (String) row.get(DEVICE_FIELD);
            e.setDevice(deviceDao.findByImei(imei));
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected TrackerEvent createEntity(final Map<String, Object> map) {
        final TrackerEvent a = new TrackerEvent();
        a.setId(((Number) map.get(ID_FIELD)).longValue());
        a.setBattery(((Number) map.get(BATTERY_FIELD)).intValue());
        a.setTemperature(((Number) map.get(TEMPERATURE_FIELD)).doubleValue());
        a.setLatitude(((Number) map.get(LATITUDE_FIELD)).doubleValue());
        a.setLongitude(((Number) map.get(LONGITUDE_FIELD)).doubleValue());
        a.setTime((Date) map.get(TIME_FIELD));
        a.setType(TrackerEventType.valueOf((String) map.get(TYPE_FIELD)));
        return a;
    }
}
