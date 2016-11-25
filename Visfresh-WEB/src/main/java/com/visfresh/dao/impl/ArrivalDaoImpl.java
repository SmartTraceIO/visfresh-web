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
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
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

    private static final String ID_FIELD = "id";
    private static final String NUMMETERS_FIELD = "nummeters";
    private static final String DATE_FIELD = "date";
    private static final String DEVICE_FIELD = "device";
    private static final String SHIPMENT_FIELD = "shipment";
    private static final String TRACKER_EVENT_FIELD = "event";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public ArrivalDaoImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<Long>("ArrivalDao", 1000, defaultCacheTimeSeconds, 2 * defaultCacheTimeSeconds);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Arrival> S saveImpl(final S arrival) {
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
        paramMap.put(TRACKER_EVENT_FIELD, arrival.getTrackerEventId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            arrival.setId(keyHolder.getKey().longValue());
        }

        return arrival;
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

    public List<String> getFields(final boolean addId) {
        final LinkedList<String> fields = new LinkedList<String>();
        fields.add(DATE_FIELD);
        fields.add(NUMMETERS_FIELD);
        fields.add(DEVICE_FIELD);
        fields.add(SHIPMENT_FIELD);
        fields.add(TRACKER_EVENT_FIELD);
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
     * @see com.visfresh.dao.ArrivalDao#getArrivals(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Arrival> getArrivals(final Shipment shipment) {
        final Filter filter = new Filter();
        filter.addFilter(SHIPMENT_FIELD, shipment.getId());

        final Sorting sorting = new Sorting(DATE_FIELD, ID_FIELD);
        return findAll(filter, sorting, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ArrivalDao#getArrival(com.visfresh.entities.Shipment)
     */
    @Override
    public Arrival getArrival(final Shipment shipment) {
        final Filter filter = new Filter();
        filter.addFilter(SHIPMENT_FIELD, shipment.getId());

        final Sorting sorting = new Sorting(DATE_FIELD, ID_FIELD);
        final List<Arrival> arrivals = findAll(filter, sorting, new Page(1, 1));
        return arrivals.isEmpty() ? null : arrivals.get(0);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Arrival a, final Map<String, Object> row,
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
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Arrival createEntity(final Map<String, Object> map) {
        final Arrival a = new Arrival();
        a.setId(((Number) map.get(ID_FIELD)).longValue());
        a.setDate((Date) map.get(DATE_FIELD));
        a.setNumberOfMettersOfArrival(((Number) map.get(NUMMETERS_FIELD)).intValue());
        final Number trackerEventId = (Number) map.get(TRACKER_EVENT_FIELD);
        if (trackerEventId != null) {
            a.setTrackerEventId(trackerEventId.longValue());
        }
        return a;
    }
}
