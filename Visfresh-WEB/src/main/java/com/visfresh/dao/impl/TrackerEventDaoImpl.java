/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
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

        if (event.getId() == null) {
            //insert
            paramMap.put("id", event.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    TYPE_FIELD
                    , TIME_FIELD
                    , BATTERY_FIELD
                    , TEMPERATURE_FIELD
                    , LATITUDE_FIELD
                    , LONGITUDE_FIELD
                    , DEVICE_FIELD
                ) + ")" + " values("
                    + ":"+ TYPE_FIELD
                    + ", :" + TIME_FIELD
                    + ", :" + BATTERY_FIELD
                    + ", :" + TEMPERATURE_FIELD
                    + ", :" + LATITUDE_FIELD
                    + ", :" + LONGITUDE_FIELD
                    + ", :" + DEVICE_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + TYPE_FIELD + "=:" + TYPE_FIELD + ","
                + TIME_FIELD + "=:" + TIME_FIELD + ","
                + BATTERY_FIELD + "=:" + BATTERY_FIELD + ","
                + TEMPERATURE_FIELD + "=:" + TEMPERATURE_FIELD + ","
                + LATITUDE_FIELD + "=:" + LATITUDE_FIELD + ","
                + LONGITUDE_FIELD + "=:" + LONGITUDE_FIELD + ","
                + DEVICE_FIELD + "=:" + DEVICE_FIELD
                + " where id = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, event.getId());
        paramMap.put(TYPE_FIELD, event.getType());
        paramMap.put(TIME_FIELD, event.getTime());
        paramMap.put(BATTERY_FIELD, event.getBattery());
        paramMap.put(TEMPERATURE_FIELD, event.getTemperature());
        paramMap.put(LATITUDE_FIELD, event.getLatitude());
        paramMap.put(LONGITUDE_FIELD, event.getLongitude());
        paramMap.put(DEVICE_FIELD, event.getDevice().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            event.setId(keyHolder.getKey().longValue());
        }

        return event;
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
        for (final String field : createFieldList(true)) {
            map.put(entityName + "." + field, resultPrefix + field);
        }
        return map;
    }
    public static List<String> createFieldList(final boolean excludeReferences) {
        final List<String> list = new LinkedList<String>();
        list.add(ID_FIELD);
        list.add(TYPE_FIELD);
        list.add(TIME_FIELD);
        list.add(BATTERY_FIELD);
        list.add(TEMPERATURE_FIELD);
        list.add(LATITUDE_FIELD);
        list.add(LONGITUDE_FIELD);
        if (!excludeReferences) {
            list.add(DEVICE_FIELD);
        }
        return list;
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
}
