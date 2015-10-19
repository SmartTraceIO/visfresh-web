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

import com.visfresh.dao.AlertDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDaoImpl extends DaoImplBase<Alert, Long> implements AlertDao {
    public static final String TABLE = "alerts";

    /**
     * Type
     */
    private static final String TYPE_FIELD = "type";
    /**
     * Alert name.
     */
    private static final String NAME_FIELD = "name";
    /**
     * Description.
     */
    private static final String DESCRIPTION_FIELD = "description";
    /**
     * Device.
     */
    private static final String DEVICE_FIELD = "device";
    /**
     * Temperature.
     */
    private static final String TEMPERATURE_FIELD = "temperature";
    /**
     * The time interval for given temperature
     */
    private static final String MINUTES_FIELD = "minutes";
    /**
     * Entity ID.
     */
    private static final String ID_FIELD = "id";
    /**
     * Date of occurrence.
     */
    private static final String DATE_FIELD = "date";

    /**
     * Default constructor.
     */
    public AlertDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <A extends Alert> A save(final A alert) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (alert.getId() == null) {
            //insert
            paramMap.put("id", alert.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD
                    , TYPE_FIELD
                    , DESCRIPTION_FIELD
                    , DEVICE_FIELD
                    , TEMPERATURE_FIELD
                    , MINUTES_FIELD
                    , DATE_FIELD
                ) + ")" + " values("
                    + ":"+ NAME_FIELD
                    + ", :" + TYPE_FIELD
                    + ", :" + DESCRIPTION_FIELD
                    + ", :" + DEVICE_FIELD
                    + ", :" + TEMPERATURE_FIELD
                    + ", :" + MINUTES_FIELD
                    + ", :" + DATE_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + NAME_FIELD + ","
                + TYPE_FIELD + "=:" + TYPE_FIELD + ","
                + DESCRIPTION_FIELD + "=:" + DESCRIPTION_FIELD + ","
                + DEVICE_FIELD + "=:" + DEVICE_FIELD + ","
                + TEMPERATURE_FIELD + "=:" + TEMPERATURE_FIELD + ","
                + MINUTES_FIELD + "=:" + MINUTES_FIELD + ","
                + DATE_FIELD + "=:" + DATE_FIELD
                + " where id = :" + ID_FIELD
            ;
        }

        final boolean isTemperature = alert instanceof TemperatureAlert;

        paramMap.put(ID_FIELD, alert.getId());
        paramMap.put(NAME_FIELD, alert.getName());
        paramMap.put(TYPE_FIELD, alert.getType().name());
        paramMap.put(DESCRIPTION_FIELD, alert.getDescription());
        paramMap.put(DEVICE_FIELD, alert.getDevice().getId());
        paramMap.put(TEMPERATURE_FIELD, isTemperature ? ((TemperatureAlert) alert).getTemperature() : -1);
        paramMap.put(MINUTES_FIELD, isTemperature ? ((TemperatureAlert) alert).getMinutes() : -1);
        paramMap.put(DATE_FIELD, alert.getDate());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            alert.setId(keyHolder.getKey().longValue());
        }

        return alert;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public Alert findOne(final Long id) {
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
        return list.size() == 0 ? null : createAlert(resultPrefix, deviceResultPrefix, companyResultPrefix, list.get(0));
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
     * @param resultPrefix
     * @param deviceResultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    private Alert createAlert(final String resultPrefix, final String deviceResultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        Alert a;
        final AlertType type = AlertType.valueOf((String) map.get(resultPrefix + TYPE_FIELD));

        switch (type) {
            case CriticalHighTemperature:
            case CriticalLowTemperature:
            case HighTemperature:
            case LowTemperature:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(((Number) map.get(resultPrefix + TEMPERATURE_FIELD)).doubleValue());
                ta.setMinutes(((Number) map.get(resultPrefix + MINUTES_FIELD)).intValue());
                a = ta;
                break;
            default:
                a = new Alert();
        }

        a.setName((String) map.get(resultPrefix + NAME_FIELD));
        a.setId(((Number) map.get(resultPrefix + ID_FIELD)).longValue());
        a.setType(type);
        a.setDescription((String) map.get(resultPrefix + DESCRIPTION_FIELD));
        a.setDevice(DeviceDaoImpl.createDevice(deviceResultPrefix, companyResultPrefix, map));
        a.setDate((Date) map.get(resultPrefix + DATE_FIELD));

        return a;
    }

    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + NAME_FIELD, resultPrefix + NAME_FIELD);
        map.put(entityName + "." + ID_FIELD, resultPrefix + ID_FIELD);
        map.put(entityName + "." + TYPE_FIELD, resultPrefix + TYPE_FIELD);
        map.put(entityName + "." + DESCRIPTION_FIELD, resultPrefix + DESCRIPTION_FIELD);
        map.put(entityName + "." + TEMPERATURE_FIELD, resultPrefix + TEMPERATURE_FIELD);
        map.put(entityName + "." + MINUTES_FIELD, resultPrefix + MINUTES_FIELD);
        map.put(entityName + "." + DATE_FIELD, resultPrefix + DATE_FIELD);
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<Alert> findAll() {
        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(null, entityName, companyEntityName, resultPrefix,
                companyResultPrefix, deviceEntityName, deviceResultPrefix);

        final List<Alert> result = new LinkedList<Alert>();
        for (final Map<String,Object> map : list) {
            result.add(createAlert(resultPrefix, deviceResultPrefix, companyResultPrefix, map));
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
