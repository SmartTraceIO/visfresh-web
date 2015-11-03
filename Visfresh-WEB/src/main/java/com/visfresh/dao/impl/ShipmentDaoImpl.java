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

import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.EntityJSonSerializer;

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

    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super();
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
    protected Map<String, List<TrackerEvent>> getDeviceEventMap(
            final Map<String, Object> params) {
        final String resultPrefix = "result_";

        // select device events
        final String selectAs = buildSelectAs(TrackerEventDaoImpl.getFields(true),
                "e", resultPrefix);
        final String sql = "select " + selectAs + " from " + TrackerEventDaoImpl.TABLE
                + " e, " + DeviceDaoImpl.TABLE + " d" + " where e."
                + TrackerEventDaoImpl.DEVICE_FIELD + " = d."
                + DeviceDaoImpl.IMEI_FIELD + " and d."
                + DeviceDaoImpl.COMPANY_FIELD + " = :companyId" + " and e."
                + TrackerEventDaoImpl.TIME_FIELD + " >= :startDate and e."
                + TrackerEventDaoImpl.TIME_FIELD + " <= :endDate"
                + " order by e." + TrackerEventDaoImpl.TIME_FIELD;

        // map of device alerts. The key is device ID.
        final Map<String, List<TrackerEvent>> deviceEvents = new HashMap<String, List<TrackerEvent>>();

        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        for (final Map<String, Object> row : list) {
            final String deviceId = (String) row.get(resultPrefix
                    + TrackerEventDaoImpl.DEVICE_FIELD);
            // add alert to map
            List<TrackerEvent> events = deviceEvents.get(deviceId);
            if (events == null) {
                events = new LinkedList<TrackerEvent>();
                deviceEvents.put(deviceId, events);
            }

            events.add(TrackerEventDaoImpl.createTrackerEvent(row, resultPrefix));
        }

        return deviceEvents;
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

            alerts.add(AlertDaoImpl.createAlert(row, resultPrefix));
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity(java.util.Map, java.util.Map)
     */
    @Override
    protected Shipment createEntity(final Map<String, Object> map,
            final Map<Long, Company> companyCache) {
        return createEntity(map, companyCache, new HashMap<String, Device>());
    }
    /**
     * @param map
     * @param companyCache
     * @param deviceCache
     * @return
     */
    protected Shipment createEntity(final Map<String, Object> map,
            final Map<Long, Company> companyCache,
            final Map<String, Device> deviceCache) {
        final Shipment e = super.createEntity(map, companyCache);
        e.setPalletId((String) map.get(PALETTID_FIELD));
        e.setAssetNum((String) map.get(ASSETNUM_FIELD));
        e.setShipmentDate((Date) map.get(SHIPMENTDATE_FIELD));
        e.getCustomFields().putAll(parseJsonMap((String) map.get(CUSTOMFIELDS_FIELD)));
        e.setStatus(ShipmentStatus.valueOf((String) map.get(STATUS_FIELD)));
        e.setDevice(deviceDao.findOne((String) map.get(DEVICE_FIELD)));
        e.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        e.setPoNum(((Number) map.get(PONUM_FIELD)).intValue());
        return e;
    }
    /**
     * @param str
     * @return
     */
    private Map<String, String> parseJsonMap(final String str) {
        return EntityJSonSerializer.parseStringMap(EntityJSonSerializer.parseJson(str));
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
        params.put(CUSTOMFIELDS_FIELD, EntityJSonSerializer.toJson(s.getCustomFields()).toString());
        params.put(STATUS_FIELD, s.getStatus().name());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(TRIPCOUNT_FIELD, s.getTripCount());
        params.put(DEVICE_FIELD, s.getDevice().getId());
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
}
