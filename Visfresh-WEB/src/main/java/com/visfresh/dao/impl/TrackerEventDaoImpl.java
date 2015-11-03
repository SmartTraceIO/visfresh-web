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

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

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
    /**
     * ID field.
     */
    protected static final String ID_FIELD = "id";
    /**
     * Event type.
     */
    protected static final String TYPE_FIELD = "type";
    /**
     * Event time.
     */
    protected static final String TIME_FIELD = "time";
    /**
     * Battery level.
     */
    protected static final String BATTERY_FIELD = "battery";
    /**
     * Temperature.
     */
    protected static final String TEMPERATURE_FIELD = "temperature";
    /**
     * latitude.
     */
    protected static final String LATITUDE_FIELD = "latitude";
    /**
     * Longitude.
     */
    protected static final String LONGITUDE_FIELD = "longitude";
    /**
     * Device ID.
     */
    protected static final String DEVICE_FIELD = "device";
    private static final String SHIPMENT_FIELD = "shipment";

    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public TrackerEventDaoImpl() {
        super();
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
        paramMap.put(TYPE_FIELD, event.getType());
        paramMap.put(TIME_FIELD, event.getTime());
        paramMap.put(BATTERY_FIELD, event.getBattery());
        paramMap.put(TEMPERATURE_FIELD, event.getTemperature());
        paramMap.put(LATITUDE_FIELD, event.getLatitude());
        paramMap.put(LONGITUDE_FIELD, event.getLongitude());
        paramMap.put(DEVICE_FIELD, event.getDevice().getId());
        paramMap.put(SHIPMENT_FIELD, event.getShipment().getId());

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
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public TrackerEvent findOne(final Long id) {
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
        return list.size() == 0 ? null : createTrackerEvent(resultPrefix, deviceResultPrefix, companyResultPrefix, list.get(0));
    }
    /**
     * @param resultPrefix
     * @param deviceResultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    private TrackerEvent createTrackerEvent(final String resultPrefix, final String deviceResultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        final TrackerEvent a = createTrackerEvent(map, resultPrefix);
        a.setDevice(DeviceDaoImpl.createDevice(deviceResultPrefix, companyResultPrefix, map));
        a.setShipment(shipmentDao.findOne(((Number) map.get(resultPrefix + SHIPMENT_FIELD)).longValue()));
        return a;
    }
    /**
     * @param map
     * @param resultPrefix
     * @return
     */
    public static TrackerEvent createTrackerEvent(final Map<String, Object> map,
            final String resultPrefix) {
        final TrackerEvent a = new TrackerEvent();
        a.setId(((Number) map.get(resultPrefix + ID_FIELD)).longValue());
        a.setBattery(((Number) map.get(resultPrefix + BATTERY_FIELD)).intValue());
        a.setTemperature(((Number) map.get(resultPrefix + TEMPERATURE_FIELD)).doubleValue());
        a.setLatitude(((Number) map.get(resultPrefix + LATITUDE_FIELD)).doubleValue());
        a.setLongitude(((Number) map.get(resultPrefix + LONGITUDE_FIELD)).doubleValue());
        a.setTime((Date) map.get(resultPrefix + TIME_FIELD));
        a.setType((String) map.get(resultPrefix + TYPE_FIELD));
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
                + deviceEntityName + "." + DeviceDaoImpl.IMEI_FIELD
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
        for (final String field : getFields(true)) {
            map.put(entityName + "." + field, resultPrefix + field);
        }
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<TrackerEvent> findAll() {
        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(null, entityName, companyEntityName, resultPrefix,
                companyResultPrefix, deviceEntityName, deviceResultPrefix);

        final List<TrackerEvent> result = new LinkedList<TrackerEvent>();
        for (final Map<String,Object> map : list) {
            result.add(createTrackerEvent(resultPrefix, deviceResultPrefix, companyResultPrefix, map));
        }
        return result;
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
        final Map<String, String> fields = createSelectAsMapping("a", "res");

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE + " a"
                + " where "
                + "a." + SHIPMENT_FIELD + " =:shipment"
                + " and time >= :fromDate and time <= :toDate order by time, id",
                params);
        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        for (final Map<String,Object> row : list) {
            final TrackerEvent e = createTrackerEvent(row, "res");
            e.setShipment(shipment);
            e.setDevice(shipment.getDevice());
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
}
