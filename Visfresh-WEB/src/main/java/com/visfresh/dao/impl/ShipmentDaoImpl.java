/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends ShipmentBaseDao<Shipment> implements ShipmentDao {
    /**
     *
     */
    private static final String SHIPMENTDEVICES_TABLE = "shipmentdevices";
    private static final String PALETTID_FIELD = "palletid";
    private static final String PONUM_FIELD = "ponum";
    private static final String DESCRIPTIONDATE_FIELD = "descriptiondate";
    private static final String CUSTOMFIELDS_FIELD = "customfiels";
    private static final String STATUS_FIELD = "status";

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
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Company company, final Date startDate,
            final Date endDate, final boolean onlyWithAlerts) {
        //TODO load the alerts and tracker events differently, but optimization should be done
        //as union
        //add request parameters.
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("companyId", company.getId());
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        final Map<String, List<Alert>> deviceAlertMap = getDeviceAlertMap(params);
        final Map<String, List<TrackerEvent>> deviceEventMap = getDeviceEventMap(params);

        //Create set of ID of all used devices
        final Set<String> deviceIds = new HashSet<String>();
        deviceIds.addAll(deviceAlertMap.keySet());
        deviceIds.addAll(deviceEventMap.keySet());

        final List<Shipment> deviceShipments = getShipmentsForDevices(deviceIds);

        final List<ShipmentData> data = new LinkedList<ShipmentData>();
        final Map<String, List<DeviceData>> deviceDataMap = new HashMap<String, List<DeviceData>>();

        for (final Shipment shipment : deviceShipments) {
            ShipmentData shipmentData = null;
            for (final Device device : shipment.getDevices()) {
                if (deviceIds.contains(device.getId())) {
                    if (shipmentData == null) {
                        shipmentData = new ShipmentData();
                        shipmentData.setShipment(shipment);

                        //add shipment data to result
                        data.add(shipmentData);
                    }

                    //create device data
                    final DeviceData dd = new DeviceData();
                    dd.setDevice(device);
                    shipmentData.getDeviceData().add(dd);

                    //register device data at the device data to device map
                    List<DeviceData> ddList = deviceDataMap.get(device.getId());
                    if (ddList == null) {
                        ddList = new LinkedList<DeviceData>();
                        deviceDataMap.put(device.getId(), ddList);
                    }

                    ddList.add(dd);
                }
            }
        }

        addAlerts(deviceDataMap, deviceAlertMap);
        addEvents(deviceDataMap, deviceEventMap);

        if (onlyWithAlerts) {
            applyOnlyWithAlertsFilter(data);
        }

        return data;
    }
    /**
     * @param deviceIds
     * @return
     */
    private List<Shipment> getShipmentsForDevices(final Set<String> deviceIds) {
        if (deviceIds.isEmpty()) {
            return new LinkedList<Shipment>();
        }

        //Get shipment ID for given devices
        final String sql = "select shipment as id from " + SHIPMENTDEVICES_TABLE
                + " where device in (" + StringUtils.combine(deviceIds, ",") + ")";
        final List<Map<String, Object>> l = jdbc.queryForList(sql, new HashMap<String, Object>());

        final List<Shipment> result = new LinkedList<Shipment>();
        for (final Map<String,Object> map : l) {
            final Long id = ((Number) map.get("id")).longValue();
            result.add(findOne(id));
        }
        return result;
    }
    /**
     * @param deviceDataMap
     * @param deviceEventMap
     */
    private void addEvents(final Map<String, List<DeviceData>> deviceDataMap,
            final Map<String, List<TrackerEvent>> deviceEventMap) {
        for (final Map.Entry<String, List<TrackerEvent>> e : deviceEventMap.entrySet()) {
            final List<DeviceData> ddList = deviceDataMap.get(e.getKey());
            if (ddList != null) {
                for (final DeviceData dd : ddList) {
                    dd.getEvents().addAll(e.getValue());
                }
            }
        }

        //sort alerts by time
        for (final List<TrackerEvent> alerts : deviceEventMap.values()) {
            Collections.sort(alerts);
        }
    }
    /**
     * @param deviceDataMap
     * @param deviceAlertMap
     */
    private void addAlerts(final Map<String, List<DeviceData>> deviceDataMap,
            final Map<String, List<Alert>> deviceAlertMap) {
        for (final Map.Entry<String, List<Alert>> e : deviceAlertMap.entrySet()) {
            final List<DeviceData> ddList = deviceDataMap.get(e.getKey());
            if (ddList != null) {
                for (final DeviceData dd : ddList) {
                    dd.getAlerts().addAll(e.getValue());
                }
            }
        }

        //sort alerts by time
        for (final List<Alert> alerts : deviceAlertMap.values()) {
            Collections.sort(alerts);
        }
    }
    /**
     * @param data
     * @return
     */
    protected void applyOnlyWithAlertsFilter(final List<ShipmentData> shipmentData) {
        //filter empty
        final Iterator<ShipmentData> sdIter = shipmentData.iterator();
        while(sdIter.hasNext()) {
            final ShipmentData next = sdIter.next();
            boolean hasAlerts = false;

            final Iterator<DeviceData> iter = next.getDeviceData().iterator();
            while (iter.hasNext()) {
                final DeviceData dd = iter.next();
                if(dd.getEvents().isEmpty() && dd.getAlerts().isEmpty()) {
                    //delete fully empty device data
                    iter.remove();
                } else if (!dd.getAlerts().isEmpty()) {
                    hasAlerts = true;
                }
            }

            if (!hasAlerts) {
                sdIter.remove();
            }
        }
    }
    /**
     * @param params
     * @return
     */
    protected Map<String, List<TrackerEvent>> getDeviceEventMap(
            final Map<String, Object> params) {
        final String resultPrefix = "result_";

        // select device events
        final String selectAs = buildSelectAs(TrackerEventDaoImpl.createFieldList(false),
                "e", resultPrefix);
        final String sql = "select " + selectAs + " from " + TrackerEventDaoImpl.TABLE
                + " e, " + DeviceDaoImpl.TABLE + " d" + " where e."
                + TrackerEventDaoImpl.DEVICE_FIELD + " = d."
                + DeviceDaoImpl.ID_FIELD + " and d."
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
        final String selectAs = buildSelectAs(AlertDaoImpl.createFieldList(false), "a", resultPrefix);
        final String sql = "select " + selectAs + " from " + AlertDaoImpl.TABLE + " a, "
                + DeviceDaoImpl.TABLE + " d"
                + " where a." + AlertDaoImpl.DEVICE_FIELD + " = d." + DeviceDaoImpl.ID_FIELD
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
        e.setPoNum((String) map.get(PONUM_FIELD));
        e.setShipmentDescriptionDate((Date) map.get(DESCRIPTIONDATE_FIELD));
        e.setCustomFields((String) map.get(CUSTOMFIELDS_FIELD));
        e.setStatus(ShipmentStatus.valueOf((String) map.get(STATUS_FIELD)));
        e.getDevices().addAll(findDevices(e, deviceCache));
        return e;
    }
    /**
     * @param no
     * @param table
     * @return
     */
    private List<Device> findDevices(final Shipment no, final Map<String, Device> cache) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, no.getId());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select device from shipmentdevices where shipment =:" + ID_FIELD,
                params);

        final List<Device> result = new LinkedList<Device>();
        for (final Map<String,Object> row : list) {
            row.remove("schedule");
            final String id = ((String) row.get("id"));

            Device device = cache.get(id);
            if (device == null) {
                device = deviceDao.findOne(id);
                cache.put(id, device);
            }

            result.add(device);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final Shipment s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ISTEMPLATE_FIELD, false);
        params.put(PALETTID_FIELD, s.getPalletId());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(DESCRIPTIONDATE_FIELD, s.getShipmentDescriptionDate());
        params.put(CUSTOMFIELDS_FIELD, s.getCustomFields());
        params.put(STATUS_FIELD, s.getStatus().name());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createManyToManyRefs(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected void createManyToManyRefs(final Shipment s) {
        super.createManyToManyRefs(s);
        for (final Device m : s.getDevices()) {
            final HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("shipment", s.getId());
            params.put("device", m.getId());
            jdbc.update("insert into " + SHIPMENTDEVICES_TABLE + " (shipment, device)"
                    + " values(:shipment,:device)", params);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#clearManyToManyRefs(java.lang.Long)
     */
    @Override
    protected void clearManyToManyRefs(final Long id) {
        super.clearManyToManyRefs(id);
        cleanRefs(SHIPMENTDEVICES_TABLE, id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return false;
    }
}
