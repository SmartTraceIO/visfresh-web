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
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDaoImpl extends DaoImplBase<Alert, Long> implements AlertDao {
    public static final String TABLE = "alerts";

    private static final String TYPE_FIELD = "type";
    protected static final String DEVICE_FIELD = "device";
    protected static final String SHIPMENT_FIELD = "shipment";
    protected static final String TEMPERATURE_FIELD = "temperature";
    protected static final String MINUTES_FIELD = "minutes";
    protected static final String ID_FIELD = "id";
    protected static final String DATE_FIELD = "date";
    protected static final String CUMULATIVE_FIELD = "cumulative";
    protected static final String TRACKER_EVENT_FIELD = "event";
    protected static final String RULE_FIELD = "rule";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceDao deviceDao;

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
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        final boolean isTemperature = alert instanceof TemperatureAlert;

        paramMap.put(ID_FIELD, alert.getId());
        paramMap.put(TYPE_FIELD, alert.getType().name());
        paramMap.put(DEVICE_FIELD, alert.getDevice().getId());
        paramMap.put(SHIPMENT_FIELD, alert.getShipment().getId());
        paramMap.put(DATE_FIELD, alert.getDate());
        paramMap.put(TRACKER_EVENT_FIELD, alert.getTrackerEventId());
        paramMap.put(TEMPERATURE_FIELD, isTemperature ? ((TemperatureAlert) alert).getTemperature() : -1);
        paramMap.put(MINUTES_FIELD, isTemperature ? ((TemperatureAlert) alert).getMinutes() : -1);
        paramMap.put(CUMULATIVE_FIELD, isTemperature ? ((TemperatureAlert) alert).isCumulative() : false);
        paramMap.put(RULE_FIELD, isTemperature ? ((TemperatureAlert) alert).getRuleId() : null);

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            alert.setId(keyHolder.getKey().longValue());
        }

        return alert;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ArrivalDao#moveToNewDevice(com.visfresh.entities.Device, com.visfresh.entities.Device)
     */
    @Override
    public void moveToNewDevice(final Device oldDevice, final Device newDevice) {
        final String sql = "update " + TABLE + " set device = :new where device = :old";
        final Map<String, Object> params = new HashMap<>();
        params.put("old", oldDevice.getImei());
        params.put("new", newDevice.getImei());

        jdbc.update(sql, params);
    }

    public static List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(TYPE_FIELD);
        fields.add(DEVICE_FIELD);
        fields.add(SHIPMENT_FIELD);
        fields.add(TRACKER_EVENT_FIELD);
        fields.add(TEMPERATURE_FIELD);
        fields.add(MINUTES_FIELD);
        fields.add(DATE_FIELD);
        fields.add(CUMULATIVE_FIELD);
        fields.add(RULE_FIELD);
        if (includeId) {
            fields.add(ID_FIELD);
        }
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
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Alert createEntity(final Map<String, Object> map) {
        return createAlert(map);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.AlertDaoImpl#resolveReferences(com.visfresh.entities.Alert, java.util.Map, java.util.Map)
     */
    @Override
    public void resolveReferences(final Alert a, final Map<String, Object> row,
            final Map<String, Object> cache) {
        //resolve shipment
        final Number shipmentId = (Number) row.get(SHIPMENT_FIELD);
        if (shipmentId != null) {
            final String shipmentKey = "ship_" + shipmentId;

            Shipment shipment = (Shipment) cache.get(shipmentKey);
            if (shipment == null) {
                shipment = shipmentDao.findOne(shipmentId.longValue());
                cache.put(shipmentKey, shipment);
            }
            a.setShipment(shipment);
        }

        //resolve device
        final String imei = (String) row.get(DEVICE_FIELD);
        final String deviceKey = "dev_" + imei;

        Device device = (Device) cache.get(deviceKey);
        if (device == null) {
            device = deviceDao.findOne(imei);
            cache.put(deviceKey, device);
        }
        a.setDevice(device);
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
     * @see com.visfresh.dao.AlertDao#getAlerts(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Alert> getAlerts(final Shipment shipment) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", shipment.getId());
        final Map<String, String> fields = createSelectAsMapping();

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE + " a"
                + " where "
                + "a." + SHIPMENT_FIELD + " =:shipment order by date, id",
                params);
        final List<Alert> alerts = new LinkedList<Alert>();
        for (final Map<String,Object> row : list) {
            final Alert a = createAlert(row);
            a.setShipment(shipment);
            a.setDevice(shipment.getDevice());
            alerts.add(a);
        }
        return alerts;
    }
    /**
     * @param fields
     * @return
     */
    private String buildSelectAs(final Map<String, String> fields) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> e : fields.entrySet()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e.getKey() + " as " + e.getValue());
        }
        return sb.toString();
    }
    /**
     * Create alert with unresolved references.
     * @param map parameter map.
     * @return
     */
    public static Alert createAlert(final Map<String, Object> map) {
        Alert a;
        final AlertType type = AlertType.valueOf((String) map.get(TYPE_FIELD));

        switch (type) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(((Number) map.get(TEMPERATURE_FIELD)).doubleValue());
                ta.setMinutes(((Number) map.get(MINUTES_FIELD)).intValue());
                ta.setCumulative((Boolean) map.get(CUMULATIVE_FIELD));
                if (map.get(RULE_FIELD) != null) {
                    ta.setRuleId(((Number) map.get(RULE_FIELD)).longValue());
                }
                a = ta;
                break;
            default:
                a = new Alert();
        }

        a.setId(((Number) map.get(ID_FIELD)).longValue());
        a.setType(type);
        a.setDate((Date) map.get(DATE_FIELD));
        final Number teid = (Number) map.get(TRACKER_EVENT_FIELD);
        a.setTrackerEventId(teid == null ? null : teid.longValue());
        return a;
    }

    /**
     * @return
     */
    private Map<String, String> createSelectAsMapping() {
        final Map<String, String> map = new HashMap<String, String>();
        for (final String field : getFields(true)) {
            map.put(field, field);
        }
        return map;
    }
}
