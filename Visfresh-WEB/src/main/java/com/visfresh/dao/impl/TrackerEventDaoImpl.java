/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.TrackerEventConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerEventDaoImpl extends DaoImplBase<TrackerEvent, TrackerEvent, Long>
    implements TrackerEventDao {

    /**
     * Table name.
     */
    public static final String TABLE = "trackerevents";

    protected static final String ID_FIELD = "id";
    protected static final String TYPE_FIELD = "type";
    protected static final String TIME_FIELD = "time";
    protected static final String CREATED_ON_FIELD = "createdon";
    protected static final String BATTERY_FIELD = "battery";
    protected static final String TEMPERATURE_FIELD = "temperature";
    protected static final String LATITUDE_FIELD = "latitude";
    protected static final String LONGITUDE_FIELD = "longitude";
    protected static final String DEVICE_FIELD = "device";
    protected static final String SHIPMENT_FIELD = "shipment";
    private static final String GATEWAY = "gateway";

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
        propertyToDbMap.put(TrackerEventConstants.PROPERTY_SHIPMENT, SHIPMENT_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends TrackerEvent> S save(final S event) {
        return save(event, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#save(com.visfresh.entities.TrackerEvent, java.lang.String)
     */
    @Override
    public <S extends TrackerEvent> S save(final S event, final String gateway) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);
        fields.add(GATEWAY);

        if (event.getId() == null) {
            //create on field is not updateable.
            event.setCreatedOn(new Date());
            fields.add(CREATED_ON_FIELD);
            paramMap.put(CREATED_ON_FIELD, event.getCreatedOn());

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
        paramMap.put(GATEWAY, gateway);
        paramMap.put(SHIPMENT_FIELD, event.getShipment() == null ? null : event.getShipment().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            event.setId(keyHolder.getKey().longValue());
        }

        return event;
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
    public List<TrackerEvent> getEvents(final Shipment shipment) {
        //filtering
        final Filter filter = new Filter();
        filter.addFilter(SHIPMENT_FIELD, shipment.getId());
        //sorting
        final Sorting sort = new Sorting(true, TIME_FIELD, ID_FIELD);
        return findAll(filter, sort, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getLastEvent(com.visfresh.entities.Shipment)
     */
    @Override
    public TrackerEvent getLastEvent(final Shipment s) {
        //filtering
        final Filter filter = new Filter();
        filter.addFilter(TrackerEventConstants.PROPERTY_SHIPMENT, s);
        //sorting
        final Sorting sort = new Sorting(false, TIME_FIELD, ID_FIELD);
        final List<TrackerEvent> events = findAll(filter, sort, new Page(1, 1));

        return events.isEmpty() ? null : events.get(0);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getFirstEvent(com.visfresh.entities.Shipment)
     */
    @Override
    public TrackerEvent getFirstEvent(final Shipment s) {
        //filtering
        final Filter filter = new Filter();
        filter.addFilter(TrackerEventConstants.PROPERTY_SHIPMENT, s);
        //sorting
        final Sorting sort = new Sorting(true, TIME_FIELD, ID_FIELD);
        final List<TrackerEvent> events = findAll(filter, sort, new Page(1, 1));

        return events.isEmpty() ? null : events.get(0);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getPreviousEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public TrackerEvent getPreviousEvent(final TrackerEvent e) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", e.getShipment().getId());
        params.put("id", e.getId());

        //find first previous normal temperature.
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from " + TABLE + " where "
                + SHIPMENT_FIELD + " =:shipment and id < :id order by id desc limit 1",
                params);
        if (rows.size() == 0) {
            return null;
        }

        //create event.
        final TrackerEvent prev = createEntity(rows.get(0));
        prev.setShipment(e.getShipment());
        prev.setDevice(e.getShipment().getDevice());
        return prev;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getEventsForShipmentIds(java.util.Collection)
     */
    @Override
    public Map<Long, List<TrackerEventDto>> getEventsForShipmentIds(
            final Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }

        final Map<Long, List<TrackerEventDto>> events = new HashMap<>();
        for (final Long id : ids) {
            events.put(id, new LinkedList<TrackerEventDto>());
        }

        //find first previous normal temperature.
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from " + TABLE + " where "
                + SHIPMENT_FIELD + " in (" + StringUtils.combine(ids, ",") + ") order by id",
                new HashMap<String, Object>());

        for (final Map<String, Object> row : rows) {
            final TrackerEventDto dto = createTrackerEventDto(row);
            events.get(dto.getShipmentId()).add(dto);
        }

        return events;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getOrderedByTimeNotNullLocations(java.lang.Long, int, int)
     */
    @Override
    public List<TrackerEventDto> getOrderedByTimeNotNullLocations(
            final Long shipment, final Page page, final boolean isOrderAscent) {
        final String sql = "select * from " + TABLE + " where "
                + SHIPMENT_FIELD + " = " + shipment + " order by time"
                + (isOrderAscent ? "" : " desc") + " limit "
                + ((page.getPageNumber() - 1) * page.getPageSize()) + ", " + page.getPageSize();
        final List<Map<String, Object>> rows = jdbc.queryForList(
                sql, new HashMap<String, Object>());

        final List<TrackerEventDto> events = new LinkedList<>();
        for (final Map<String, Object> row : rows) {
            events.add(createTrackerEventDto(row));
        }

        return events;
    }

    /**
     * @param row
     * @return
     */
    protected TrackerEventDto createTrackerEventDto(final Map<String, Object> row) {
        final TrackerEvent e = createEntity(row);

        final TrackerEventDto dto = new TrackerEventDto();
        dto.setBattery(e.getBattery());
        dto.setCreatedOn(e.getCreatedOn());
        dto.setId(e.getId());
        dto.setLatitude(e.getLatitude());
        dto.setLongitude(e.getLongitude());
        dto.setTemperature(e.getTemperature());
        dto.setTime(e.getTime());
        dto.setType(e.getType());

        dto.setShipmentId(((Number) row.get(SHIPMENT_FIELD)).longValue());
        dto.setDeviceImei((String) row.get(DEVICE_FIELD));
        dto.setGateway((String) row.get(GATEWAY));
        return dto;
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
        //resolve shipment
        final Number shipmentId = (Number) row.get(SHIPMENT_FIELD);
        if (shipmentId != null) {
            final String shipmentKey = "ship_" + shipmentId;

            Shipment shipment = (Shipment) cache.get(shipmentKey);
            if (shipment == null) {
                shipment = shipmentDao.findOne(shipmentId.longValue());
                cache.put(shipmentKey, shipment);
            }
            e.setShipment(shipment);
        }

        //resolve device
        final String imei = (String) row.get(DEVICE_FIELD);
        final String deviceKey = "dev_" + imei;

        Device device = (Device) cache.get(deviceKey);
        if (device == null) {
            device = deviceDao.findOne(imei);
            cache.put(deviceKey, device);
        }
        e.setDevice(device);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    public SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()){
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.Object, java.util.Map, java.util.List)
             */
            @Override
            protected void addFilterValue(final String property, final Object value,
                    final Map<String, Object> params, final List<String> filters) {
                if (value instanceof Shipment) {
                    super.addFilterValue(property, ((Shipment) value).getId(), params, filters);
                } else {
                    super.addFilterValue(property, value, params, filters);
                }
            }
        };
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected TrackerEvent createEntity(final Map<String, Object> map) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(((Number) map.get(ID_FIELD)).longValue());
        e.setBattery(((Number) map.get(BATTERY_FIELD)).intValue());
        e.setTemperature(((Number) map.get(TEMPERATURE_FIELD)).doubleValue());
        final Number lat = (Number) map.get(LATITUDE_FIELD);
        if (lat != null) {
            e.setLatitude(lat.doubleValue());
        }
        final Number lon = (Number) map.get(LONGITUDE_FIELD);
        if (lon != null) {
            e.setLongitude(lon.doubleValue());
        }
        e.setTime((Date) map.get(TIME_FIELD));
        e.setCreatedOn((Date) map.get(CREATED_ON_FIELD));
        e.setType(TrackerEventType.valueOf((String) map.get(TYPE_FIELD)));
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getLastEvents(java.util.List)
     */
    @Override
    public List<ShortTrackerEvent> getLastEvents(final List<Device> devices) {
        if (devices.isEmpty()) {
            return new LinkedList<>();
        }

        //create set of device IMEI for avoid of duplicates.
        final Set<String> imeis = new HashSet<>();
        for (final Device d : devices) {
            imeis.add(d.getImei());
        }

        final Map<String, Object> params = new HashMap<>();
        final List<String> sqls = new LinkedList<>();

        int index = 0;
        for (final String imei : imeis) {
            final String key = "imei_" + index;

            final String sql = "(select * from " + getTableName()
                    + " where " + DEVICE_FIELD + "=:" + key
                    + " order by " + ID_FIELD + " desc limit 1"
                    + ")";
            params.put(key, imei);
            index++;
            sqls.add(sql);
        }

        final List<ShortTrackerEvent> result = new LinkedList<>();
        final List<Map<String, Object>> rows = jdbc.queryForList(StringUtils.combine(sqls, " UNION "), params);

        for (final Map<String, Object> row : rows) {
            final ShortTrackerEvent ue = createShortTrackerEvent(row);

            result.add(ue);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getLastEvent(com.visfresh.entities.Device)
     */
    @Override
    public ShortTrackerEvent getLastEvent(final Device device) {
        if (device == null) {
            return null;
        }

        final List<Device> devices = new LinkedList<>();
        devices.add(device);

        final List<ShortTrackerEvent> lastEvents = getLastEvents(devices);
        if (!lastEvents.isEmpty()) {
            return lastEvents.get(0);
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#findBy(java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public List<ShortTrackerEvent> findBy(final String device, final Date startDate,
            final Date endDate) {
        final List<Map<String, Object>> rows = findByDeviceAndDateRanges(device, startDate, endDate);

        final List<ShortTrackerEvent> result = new LinkedList<>();
        for (final Map<String, Object> row : rows) {
            final ShortTrackerEvent ue = createShortTrackerEvent(row);
            result.add(ue);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getEventsAfterDate(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    public List<TrackerEvent> getEventsAfterDate(final Shipment s, final Date date) {
        final List<TrackerEvent> result = new LinkedList<>();

        if (s != null) {
            final List<Map<String, Object>> rows = findByDeviceAndDateRanges(
                    s.getDevice().getImei(), date, null);

            for (final Map<String, Object> row : rows) {
                if (row.get(SHIPMENT_FIELD) != null) {
                    final TrackerEvent ue = createEntity(row);
                    ue.setShipment(s);
                    ue.setDevice(s.getDevice());
                    result.add(ue);
                }
            }
        }

        return result;
    }
    /**
     * @param device
     * @param startDate
     * @param endDate
     * @return
     */
    private List<Map<String, Object>> findByDeviceAndDateRanges(final String device,
            final Date startDate, final Date endDate) {
        //create set of device IMEI for avoid of duplicates.
        final Map<String, Object> params = new HashMap<>();
        params.put("device", device);
        if (startDate != null) {
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            params.put("endDate", endDate);
        }

        final StringBuilder sql = new StringBuilder("select * from " + getTableName());
        sql.append(" where " + DEVICE_FIELD + "=:device");
        if (startDate != null) {
            sql.append(" and " + TIME_FIELD + " >= :startDate");
        }
        if (endDate != null) {
            sql.append(" and " + TIME_FIELD + " <= :endDate");
        }
        sql.append(" order by " + ID_FIELD);

        return jdbc.queryForList(sql.toString(), params);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getEventsByCompanyDateRanges(com.visfresh.entities.Company, java.util.Date, java.util.Date, com.visfresh.dao.Page)
     */
    @Override
    public List<TrackerEvent> findForArrivedShipmentsInDateRanges(final Company c,
            final Date startDate, final Date endDate, final LocationProfile location, final Page page) {
        //create set of device IMEI for avoid of duplicates.
        final Map<String, Object> params = new HashMap<>();
        params.put("company", c.getId());
        params.put("status", ShipmentStatus.Arrived.name());
        if (startDate != null) {
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            params.put("endDate", endDate);
        }
        if (location != null) {
            params.put("location", location.getId());
        }

        final StringBuilder sql = new StringBuilder("select * from " + getTableName());
        sql.append(" join devices on devices.imei = trackerevents.device and devices.company = :company");
        sql.append(" join shipments on shipments.id = trackerevents.shipment and shipments.status = :status");
        if (location != null) {
            sql.append(" and shipments.shippedto = :location");
        }
        if (startDate != null) {
            sql.append(" and trackerevents." + TIME_FIELD + " >= :startDate");
        }
        if (endDate != null) {
            sql.append(" and trackerevents." + TIME_FIELD + " <= :endDate");
        }

        sql.append(" order by trackerevents.time, trackerevents.id");
        if (page != null) {
            sql.append(" limit " + ((page.getPageNumber() - 1) * page.getPageSize())
                    + "," + page.getPageSize());
        }

        final List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<TrackerEvent> result = new LinkedList<>();
        for (final Map<String,Object> map : rows) {
            final TrackerEvent e = createEntity(map);
            resolveReferences(e, map, cache);
            result.add(e);
        }

        return result;
    }
    /**
     * @param row
     * @return
     */
    private ShortTrackerEvent createShortTrackerEvent(
            final Map<String, Object> row) {
        final TrackerEvent e = createEntity(row);

        final ShortTrackerEvent ue = new ShortTrackerEvent();
        ue.setBattery(e.getBattery());
        ue.setId(e.getId());
        ue.setLatitude(e.getLatitude());
        ue.setLongitude(e.getLongitude());
        ue.setTemperature(e.getTemperature());
        ue.setTime(e.getTime());
        ue.setType(e.getType());
        ue.setCreatedOn(e.getCreatedOn());

        final Object shipmentField = row.get(SHIPMENT_FIELD);
        //possible null if not shipment assigned for given event. It is possible
        if (shipmentField != null) {
            ue.setShipmentId(((Number) shipmentField).longValue());
        }
        ue.setDeviceImei((String) row.get(DEVICE_FIELD));
        return ue;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#assignShipment(java.lang.Long, com.visfresh.entities.Shipment)
     */
    @Override
    public void assignShipment(final Long id, final Shipment s) {
        final String sql = "update " + TABLE + " set " + SHIPMENT_FIELD + "=:shipment where " + ID_FIELD + "=:id";

        final Map<String, Object> map = new HashMap<>();
        map.put("shipment", s.getId());
        map.put("id", id);

        jdbc.update(sql, map);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getTrackerEventPart(java.util.Set, int, int)
     */
    @Override
    public List<TrackerEventDto> getEventPart(final Set<Long> shipments, final int page, final int size) {
        final List<TrackerEventDto> events = new LinkedList<>();

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from trackerevents e where e.shipment in ("
                        + StringUtils.combine(shipments, ",") + ") order by e.time, e.id limit "
                        + (page * size) + "," + size,
                new HashMap<String, Object>());

        for (final Map<String, Object> row : rows) {
            final TrackerEventDto e = new TrackerEventDto();

            e.setBattery(DaoImplBase.dbInteger(row.get("battery")));
            e.setCreatedOn((Date) row.get("createdon"));
            e.setId(DaoImplBase.dbLong(row.get("id")));
            e.setLatitude(DaoImplBase.dbDouble(row.get("latitude")));
            e.setLongitude(DaoImplBase.dbDouble(row.get("longitude")));
            e.setTemperature(DaoImplBase.dbDouble(row.get("temperature")));
            e.setTime((Date) row.get("time"));
            e.setType(TrackerEventType.valueOf((String) row.get("type")));
            e.setShipmentId(DaoImplBase.dbLong(row.get("shipment")));
            e.setDeviceImei((String) row.get("device"));
            e.setGateway((String) row.get(GATEWAY));

            events.add(e);
        }
        return events;
    }
}
