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
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends ShipmentBaseDao<Shipment> implements ShipmentDao {
    private static final String PONUM_FIELD = "ponum";
    private static final String TRIPCOUNT_FIELD = "tripcount";
    private static final String PALETTID_FIELD = "palletid";
    private static final String ASSETNUM_FIELD = "assetnum";
    private static final String SHIPMENTDATE_FIELD = "shipmentdate";
    private static final String CUSTOMFIELDS_FIELD = "customfiels";
    private static final String STATUS_FIELD = "status";
    private static final String DEVICE_FIELD = "device";
    private static final String ASSETTYPE_FIELD = "assettype";

    @Autowired
    private DeviceDao deviceDao;

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super();
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID, ALERT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES, NOALERTIFCOODOWN_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM,
                ARRIVALNOTIFWITHIN_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS,
                NONOTIFSIFNOALERTS_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_FROM, SHIPPEDFROM_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_TO, SHIPPEDTO_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES,
                SHUTDOWNTIMEOUT_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES,
//                );
        propertyToDbFields.put(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER, COMMENTS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_PROFILE, ALERT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_DEVICE_IMEI, DEVICE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_STATUS, STATUS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_CUSTOM_FIELDS, CUSTOMFIELDS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_DATE, SHIPMENTDATE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_PO_NUM, PONUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_TRIP_COUNT, TRIPCOUNT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ASSET_NUM, ASSETNUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_PALLET_ID, PALETTID_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_ID, ID_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ASSET_TYPE, ASSETTYPE_FIELD);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity()
     */
    @Override
    protected Shipment createEntity() {
        return new Shipment();
    }
    /**
     * @param params
     * @return
     */
    protected Map<String, List<Alert>> getDeviceAlertMap(
            final Map<String, Object> params) {
        final String resultPrefix = "result_";

        //select alerts.
        final String selectAs = buildSelectAs(AlertDaoImpl.getFields(true), "a", resultPrefix);
        final String sql = "select " + selectAs + " from " + AlertDaoImpl.TABLE + " a, "
                + DeviceDaoImpl.TABLE + " d"
                + " where a." + AlertDaoImpl.DEVICE_FIELD + " = d." + DeviceDaoImpl.IMEI_FIELD
                + " and d." + DeviceDaoImpl.COMPANY_FIELD + " = :companyId"
                + " and a." + AlertDaoImpl.DATE_FIELD + " >= :startDate and a." + AlertDaoImpl.DATE_FIELD + " <= :endDate"
                + " order by a." + AlertDaoImpl.DATE_FIELD;

        //map of device alerts. The key is device ID.
        final Map<String, List<Alert>> deviceAlerts = new HashMap<String, List<Alert>>();

        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        for (final Map<String, Object> row : list) {
            final String deviceId = (String) row.get(resultPrefix + AlertDaoImpl.DEVICE_FIELD);
            //add alert to map
            List<Alert> alerts = deviceAlerts.get(deviceId);
            if (alerts == null) {
                alerts = new LinkedList<Alert>();
                deviceAlerts.put(deviceId, alerts);
            }

            alerts.add(AlertDaoImpl.createAlert(row));
        }

        return deviceAlerts;
    }

    /**
     * @param fields
     * @param tableAlias
     * @param resultPrefix
     * @return
     */
    private String buildSelectAs(final List<String> fields, final String tableAlias,
            final String resultPrefix) {
        final StringBuilder sb = new StringBuilder();
        for (final String f : fields) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(tableAlias + "." + f);
            sb.append(" as ");
            sb.append(resultPrefix + f);
        }
        return sb.toString();
    }

    /**
     * @param map
     * @param companyCache
     * @param deviceCache
     * @return
     */
    @Override
    protected Shipment createEntity(final Map<String, Object> map) {
        final Shipment e = super.createEntity(map);
        e.setPalletId((String) map.get(PALETTID_FIELD));
        e.setAssetNum((String) map.get(ASSETNUM_FIELD));
        e.setShipmentDate((Date) map.get(SHIPMENTDATE_FIELD));
        e.getCustomFields().putAll(parseJsonMap((String) map.get(CUSTOMFIELDS_FIELD)));
        e.setStatus(ShipmentStatus.valueOf((String) map.get(STATUS_FIELD)));
        e.setDevice(deviceDao.findOne((String) map.get(DEVICE_FIELD)));
        e.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        e.setPoNum(((Number) map.get(PONUM_FIELD)).intValue());
        e.setAssetType((String) map.get(ASSETTYPE_FIELD));
        return e;
    }
    /**
     * @param str
     * @return
     */
    private Map<String, String> parseJsonMap(final String str) {
        return AbstractJsonSerializer.parseStringMap(SerializerUtils.parseJson(str));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final Shipment s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ISTEMPLATE_FIELD, false);
        params.put(PALETTID_FIELD, s.getPalletId());
        params.put(ASSETNUM_FIELD, s.getAssetNum());
        params.put(SHIPMENTDATE_FIELD, s.getShipmentDate());
        params.put(CUSTOMFIELDS_FIELD, AbstractJsonSerializer.toJson(s.getCustomFields()).toString());
        params.put(STATUS_FIELD, s.getStatus().name());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(TRIPCOUNT_FIELD, s.getTripCount());
        params.put(DEVICE_FIELD, s.getDevice().getId());
        params.put(ASSETTYPE_FIELD, s.getAssetType());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#save(com.visfresh.entities.ShipmentBase)
     */
    @Override
    public <S extends Shipment> S save(final S s) {
        final boolean insert = s.getId() == null;
        if (insert) {
            s.setTripCount(s.getDevice().getTripCount() + 1);
            s.getDevice().setTripCount(s.getTripCount());
        }
        final S result = super.save(s);
        if (insert) {
            deviceDao.save(s.getDevice());
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipment(java.lang.String)
     */
    @Override
    public Shipment findActiveShipment(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);
        params.put("status", ShipmentStatus.Complete.name());

        final String sql = "select s." + ID_FIELD
                + " from " + TABLE + " s"
                + " join " + DeviceDaoImpl.TABLE + " d on d." + DeviceDaoImpl.IMEI_FIELD + "= s.device"
                + " and d." + DeviceDaoImpl.IMEI_FIELD + "= :imei"
                + " where s." + STATUS_FIELD + "<> :status";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        if (rows.size() > 0) {
            final Long shipmentId = ((Number) rows.get(0).get(ID_FIELD)).longValue();
            return findOne(shipmentId);
        } else {
            return null;
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Shipment s, final Map<String, Object> row,
            final Map<String, Object> cache) {
        super.resolveReferences(s, row, cache);

        final String imei = (String) row.get(DEVICE_FIELD);
        final String key = "device_" + imei;
        Device device = (Device) cache.get(key);
        if (device == null) {
            device = deviceDao.findByImei(imei);
            cache.put(key, device);
        }

        s.setDevice(device);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.String, java.lang.Object, java.util.Map, java.util.List)
     */
    @Override
    protected void addFilterValue(final String key, final String fieldName, final Object value,
            final Map<String, Object> params, final List<String> filters) {
        if (STATUS_FIELD.equals(fieldName)) {
            super.addFilterValue(key, fieldName, value == null ? null : value.toString(), params, filters);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_TO.equals(fieldName)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String k = key + "_" + num;
                params.put(k, obj);
                in.add(":" + k);
                num++;
            }

            filters.add(SHIPPEDTO_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.PROPERTY_SHIPPED_FROM.equals(fieldName)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String k = key + "_" + num;
                params.put(k, obj);
                in.add(":" + k);
                num++;
            }

            filters.add(SHIPPEDFROM_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.PROPERTY_SHIPPED_TO_DATE.equals(fieldName)){
            //shipped to date
            params.put(key, value);
            filters.add(SHIPMENTDATE_FIELD + " <= :" + key);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_FROM_DATE.equals(fieldName)){
            //shipped from date
            params.put(key, value);
            filters.add(SHIPMENTDATE_FIELD + " >= :" + key);
        } else if (DESCRIPTION_FIELD.equals(fieldName)){
            params.put(key, "%" + value + "%");
            filters.add(fieldName + " like :" + key);
        } else {
            super.addFilterValue(key, fieldName, value, params, filters);
        }
    }
}
