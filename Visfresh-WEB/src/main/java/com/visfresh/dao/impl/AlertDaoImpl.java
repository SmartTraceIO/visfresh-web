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

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Shipment;
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
     * Device.
     */
    protected static final String DEVICE_FIELD = "device";
    protected static final String SHIPMENT_FIELD = "shipment";
    /**
     * Temperature.
     */
    protected static final String TEMPERATURE_FIELD = "temperature";
    /**
     * The time interval for given temperature
     */
    protected static final String MINUTES_FIELD = "minutes";
    /**
     * Entity ID.
     */
    protected static final String ID_FIELD = "id";
    /**
     * Date of occurrence.
     */
    protected static final String DATE_FIELD = "date";

    @Autowired
    private ShipmentDao shipmentDao;

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

        final List<String> fields = getFields(false);
        if (alert.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            paramMap.put(ID_FIELD, alert.getId());
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        final boolean isTemperature = alert instanceof TemperatureAlert;

        paramMap.put(ID_FIELD, alert.getId());
        paramMap.put(TYPE_FIELD, alert.getType().name());
        paramMap.put(DEVICE_FIELD, alert.getDevice().getId());
        paramMap.put(SHIPMENT_FIELD, alert.getShipment().getId());
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

    public static List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(TYPE_FIELD);
        fields.add(DEVICE_FIELD);
        fields.add(SHIPMENT_FIELD);
        fields.add(TEMPERATURE_FIELD);
        fields.add(MINUTES_FIELD);
        fields.add(DATE_FIELD);
        if (includeId) {
            fields.add(ID_FIELD);
        }
        return fields;
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.AlertDao#getAlerts(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Alert> getAlerts(final Shipment shipment, final Date fromDate, final Date toDate) {
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
        final List<Alert> alerts = new LinkedList<Alert>();
        for (final Map<String,Object> row : list) {
            final Alert a = createAlert(row, "res");
            a.setShipment(shipment);
            a.setDevice(shipment.getDevice());
            alerts.add(a);
        }
        return alerts;
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
        final Alert a = createAlert(map, resultPrefix);
        a.setDevice(DeviceDaoImpl.createDevice(deviceResultPrefix, companyResultPrefix, map));
        a.setShipment(shipmentDao.findOne(((Number) map.get(resultPrefix + SHIPMENT_FIELD)).longValue()));
        return a;
    }

    /**
     * Create alert with unresolved references.
     * @param map parameter map.
     * @param resultPrefix
     * @return
     */
    public static Alert createAlert(final Map<String, Object> map, final String resultPrefix) {
        Alert a;
        final AlertType type = AlertType.valueOf((String) map.get(resultPrefix + TYPE_FIELD));

        switch (type) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(((Number) map.get(resultPrefix + TEMPERATURE_FIELD)).doubleValue());
                ta.setMinutes(((Number) map.get(resultPrefix + MINUTES_FIELD)).intValue());
                a = ta;
                break;
            default:
                a = new Alert();
        }

        a.setId(((Number) map.get(resultPrefix + ID_FIELD)).longValue());
        a.setType(type);
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
        for (final String field : getFields(true)) {
            map.put(entityName + "." + field, resultPrefix + field);
        }
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
