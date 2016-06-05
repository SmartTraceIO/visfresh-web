/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.constants.DeviceConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Color;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.json.DeviceStateSerializer;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceDaoImpl extends EntityWithCompanyDaoImplBase<Device, String> implements DeviceDao {
    private static final Logger log = LoggerFactory.getLogger(DeviceDaoImpl.class);

    /**
     * Table name.
     */
    public static final String TABLE = "devices";
    /**
     * Description field.
     */
    public static final String DESCRIPTION_FIELD = "description";
    /**
     * Name field.
     */
    public static final String NAME_FIELD = "name";
    /**
     * Name field.
     */
    public static final String IMEI_FIELD = "imei";
    /**
     * Company name.
     */
    public static final String COMPANY_FIELD = "company";
    /**
     * Device trip count.
     */
    public static final String TRIPCOUNT_FIELD = "tripcount";
    private static final String PROPERTY_DEVICE_GROUP = "deviceGroup";
    private static final String ACTIVE_FIELD = "active";
    protected static final String AUTOSTART_TEMPLATE_FIELD = "autostart";
    private static final String COLOR_FIELD = "color";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();
    private final DeviceStateSerializer stateSerializer = new DeviceStateSerializer();
    /**
     * Default constructor.
     */
    public DeviceDaoImpl() {
        super();
        propertyToDbFields.put(DeviceConstants.PROPERTY_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_NAME, NAME_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_IMEI, IMEI_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_ACTIVE, ACTIVE_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Device> S save(final S device) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(IMEI_FIELD, device.getId());
        paramMap.put(NAME_FIELD, device.getName());
        paramMap.put(DESCRIPTION_FIELD, device.getDescription());
        paramMap.put(IMEI_FIELD, device.getImei());
        paramMap.put(COMPANY_FIELD, device.getCompany().getId());
        paramMap.put(TRIPCOUNT_FIELD, device.getTripCount());
        paramMap.put(ACTIVE_FIELD, device.isActive());
        paramMap.put(AUTOSTART_TEMPLATE_FIELD, device.getAutostartTemplateId());
        paramMap.put(COLOR_FIELD, device.getColor() == null ? null : device.getColor().name());

        final LinkedList<String> fields = new LinkedList<String>(paramMap.keySet());

        String sql;
        if (findOne(device.getId()) == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, IMEI_FIELD);
        }

        jdbc.update(sql, paramMap);
        return device;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return IMEI_FIELD;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Device createEntity(final Map<String, Object> map) {
        final Device d = new Device();
        d.setName((String) map.get(NAME_FIELD));
        d.setDescription((String) map.get(DESCRIPTION_FIELD));
        d.setImei((String) map.get(IMEI_FIELD));
        d.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        d.setActive(Boolean.TRUE.equals(map.get(ACTIVE_FIELD)));
        d.setColor(parseColor((String) map.get(COLOR_FIELD)));
        if (map.get(AUTOSTART_TEMPLATE_FIELD) != null) {
            d.setAutostartTemplateId(((Number) map.get(AUTOSTART_TEMPLATE_FIELD)).longValue());
        }
        return d;
    }
    /**
     * @param str
     * @return
     */
    private Color parseColor(final String str) {
        if (str == null) {
            return null;
        }
        try {
            return Color.valueOf(str);
        } catch (final Exception e) {
            log.error("Failed to parse color value '" + str + "'", e);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#findAllByImei(java.lang.String)
     */
    @Override
    public Device findByImei(final String imei) {
        return findOne(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#getState(java.lang.String)
     */
    @Override
    public DeviceState getState(final String imei) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("device", imei);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select state as state from devicestates where device = :device", paramMap);
        if (list.size() == 0) {
            return null;
        }

        final String state = (String) list.get(0).get("state");
        return stateSerializer.parseState(state);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#save(java.lang.String, com.visfresh.rules.DeviceState)
     */
    @Override
    public void saveState(final String imei, final DeviceState state) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", stateSerializer.toString(state));
        params.put("device", imei);

        if (getState(imei) == null) {
            jdbc.update("insert into devicestates(device, state) values (:device, :state)", params);
        } else {
            jdbc.update("update devicestates set state = :state where device = :device", params);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#getDevices(java.lang.String)
     */
    @Override
    public List<Device> findByGroup(final DeviceGroup group) {
        final Filter filter = new Filter();
        filter.addFilter(COMPANY_FIELD, group.getCompany().getId());
        filter.addFilter(PROPERTY_DEVICE_GROUP, group);
        final Sorting sorting = new Sorting(NAME_FIELD);

        return findAll(filter, sorting, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#moveToNewCompany(com.visfresh.entities.Device, com.visfresh.entities.Company)
     */
    @Override
    public void moveToNewCompany(final Device device, final Company c) {
        final String sql = "update devices set company=:company, "
                + TRIPCOUNT_FIELD + "= 0,"
                + AUTOSTART_TEMPLATE_FIELD + "= NULL where imei=:device";
        final Map<String, Object> params = new HashMap<>();
        params.put("device", device.getImei());
        params.put("company", c.getId());

        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.Object, java.util.Map, java.util.List)
     */
    @Override
    protected void addFilterValue(final String property, final Object value,
            final Map<String, Object> params, final List<String> filters) {
        if (PROPERTY_DEVICE_GROUP.equals(property)) {
            final Long group = value instanceof DeviceGroup
                    ? ((DeviceGroup) value).getId() : (Long) value;
            final String key = DEFAULT_FILTER_KEY_PREFIX + "group";

            filters.add(IMEI_FIELD + " in (select " + DeviceGroupDaoImpl.RELATIONS_DEVICE
                    + " from " + DeviceGroupDaoImpl.RELATIONS_TABLE
                    + " where `" + DeviceGroupDaoImpl.RELATIONS_GROUP + "` = :" + key + ")");
            params.put(key, group);
        } else {
            super.addFilterValue(property, value, params, filters);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#getListDeviceItems(com.visfresh.entities.Company, com.visfresh.dao.Sorting, com.visfresh.dao.Page)
     */
    @Override
    public List<ListDeviceItem> getDevices(final Company company,
            final Sorting sorting, final Page page) {
        String sql = "select\n"
                + "d." + IMEI_FIELD + " as " + DeviceConstants.PROPERTY_IMEI + ",\n"
                + "d." + NAME_FIELD + " as " + DeviceConstants.PROPERTY_NAME + ",\n"
                + "d." + DESCRIPTION_FIELD + " as " + DeviceConstants.PROPERTY_DESCRIPTION + ",\n"
                + "d." + ACTIVE_FIELD + " as " + DeviceConstants.PROPERTY_ACTIVE + ",\n"
                + "d." + COLOR_FIELD + " as " + DeviceConstants.PROPERTY_COLOR + ",\n"
                + "aut." + AutoStartShipmentDaoImpl.ID_FIELD + " as "
                + DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID + ",\n"
                + "tpl." + ShipmentTemplateDaoImpl.NAME_FIELD + " as "
                + DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME + ",\n"
                + "substring(d." + DeviceDaoImpl.IMEI_FIELD + ", -7, 6) as "
                + DeviceConstants.PROPERTY_SN + ",\n"
                //please attention, the field shipment number is used only for sorting
                + "COALESCE(substring(sp." + ShipmentDaoImpl.DEVICE_FIELD + ", -7, 6), '999999999999999999') as "
                + DeviceConstants.PROPERTY_SHIPMENT_NUMBER + ",\n"
                + "sp.id as " + DeviceConstants.PROPERTY_LAST_SHIPMENT + ",\n"
                + "lr.latitude as " + DeviceConstants.PROPERTY_LAST_READING_LAT + ",\n"
                + "lr.longitude as " + DeviceConstants.PROPERTY_LAST_READING_LONG + ",\n"
                + "lr.battery as " + DeviceConstants.PROPERTY_LAST_READING_BATTERY + ",\n"
                + "lr.temperature as " + DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE + ",\n"
                + "lr.time as " + DeviceConstants.PROPERTY_LAST_READING_TIME_ISO + ",\n"
                + "sp.status as " + DeviceConstants.PROPERTY_SHIPMENT_STATUS + ",\n"
                + "sp.tripcount as deviceTripCount\n"
                + "from devices d\n"

                + "left outer join (select s.* from "
                + ShipmentDaoImpl.TABLE + " s "
                + " join (select max(" + ShipmentDaoImpl.ID_FIELD+ ") as id from " + ShipmentDaoImpl.TABLE
                + " group by " + ShipmentDaoImpl.DEVICE_FIELD + ") s1 on s1.id = s." + ShipmentDaoImpl.ID_FIELD
                + ") sp on sp.device = d." + IMEI_FIELD + "\n"

                + "left outer join (\n"
                + "select\n"
                + "te." + TrackerEventDaoImpl.TIME_FIELD + " as time,\n"
                + "te." + TrackerEventDaoImpl.TEMPERATURE_FIELD + " as temperature,\n"
                + "te." + TrackerEventDaoImpl.BATTERY_FIELD + " as battery,\n"
                + "te." + TrackerEventDaoImpl.LATITUDE_FIELD + " as latitude,\n"
                + "te." + TrackerEventDaoImpl.LONGITUDE_FIELD + " as longitude,\n"
                + "te." + TrackerEventDaoImpl.DEVICE_FIELD + " as device,\n"
                + "te." + TrackerEventDaoImpl.SHIPMENT_FIELD + " as shipment\n"
                + "from " + TrackerEventDaoImpl.TABLE + " te\n"
                + "join (select max(" + TrackerEventDaoImpl.ID_FIELD + ") as id from "
                + TrackerEventDaoImpl.TABLE + " where not shipment is NULL"
                + " group by " + TrackerEventDaoImpl.SHIPMENT_FIELD
                + ") teid\n"
                + "on teid.id = te." + TrackerEventDaoImpl.ID_FIELD + "\n"
                + ") lr on lr.shipment = sp.id\n"

                + "left outer join " + AutoStartShipmentDaoImpl.TABLE + " aut on aut."
                + AutoStartShipmentDaoImpl.ID_FIELD + " = d." + AUTOSTART_TEMPLATE_FIELD + "\n"
                + "left outer join " + ShipmentTemplateDaoImpl.TABLE + " tpl on tpl."
                + ShipmentTemplateDaoImpl.ID_FIELD + " = aut." + AutoStartShipmentDaoImpl.TEMPLATE_FIELD + "\n"
                + "where d." + COMPANY_FIELD + " = :company\n";

        if (sorting != null && sorting.getSortProperties().length > 0) {
            sql += "order by ";
            final String direction = sorting.isAscentDirection() ? " asc" : " desc";
            sql += StringUtils.combine(sorting.getSortProperties(), direction + ",") + direction;
        }
        if (page != null) {
            sql += " limit " + ((page.getPageNumber() - 1) * page.getPageSize()) + "," + page.getPageSize();
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("company", company.getId());

        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        final List<ListDeviceItem> items = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            final ListDeviceItem item = createListDeviceItem(row);
            items.add(item);
        }

        return items;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#updateColor(com.visfresh.entities.Device, com.visfresh.entities.Color)
     */
    @Override
    public void updateColor(final Device d, final Color color) {
        if (d == null) {
            return;
        }


        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", d.getImei());
        params.put("color", color == null ? null : color.name());

        jdbc.update("update " + TABLE + " set " + COLOR_FIELD + "=:color where " + IMEI_FIELD + "=:imei", params);
    }
    /**
     * @param row
     * @return
     */
    private ListDeviceItem createListDeviceItem(final Map<String, Object> row) {
        final ListDeviceItem item = new ListDeviceItem();

        item.setActive(!Boolean.FALSE.equals(row.get(DeviceConstants.PROPERTY_ACTIVE)));
        item.setDescription((String) row.get(DeviceConstants.PROPERTY_DESCRIPTION));
        item.setImei((String) row.get(DeviceConstants.PROPERTY_IMEI));
        item.setName((String) row.get(DeviceConstants.PROPERTY_NAME));
        item.setColor(parseColor((String) row.get(DeviceConstants.PROPERTY_COLOR)));
        final Number deviceTripCount = (Number) row.get("deviceTripCount");
        if (deviceTripCount != null) {
            item.setTripCount(deviceTripCount.intValue());
        }

        if (row.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID) != null) {
            item.setAutostartTemplateId(((Number) row.get(
                    DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID)).longValue());
            item.setAutostartTemplateName((String) row.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME));
        }

        //if shipment not null
        final Number shipmentId = (Number) row.get(DeviceConstants.PROPERTY_LAST_SHIPMENT);
        if (shipmentId != null) {
            item.setShipmentId(shipmentId.longValue());
            item.setShipmentStatus(
                    ShipmentStatus.valueOf((String) row.get(DeviceConstants.PROPERTY_SHIPMENT_STATUS)));

            //if found the tracker event
            final Number temperature = (Number) row.get(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE);
            if (temperature != null) {
                item.setTemperature(temperature.doubleValue());
                item.setBattery(((Number) row.get(DeviceConstants.PROPERTY_LAST_READING_BATTERY)).intValue());
                item.setLastReadingTime((Date) row.get(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO));

                final Number lat = (Number) row.get(DeviceConstants.PROPERTY_LAST_READING_LAT);
                if (lat != null) {
                    item.setLatitude(lat.doubleValue());
                }
                final Number lon = (Number) row.get(DeviceConstants.PROPERTY_LAST_READING_LONG);
                if (lon != null) {
                    item.setLongitude(lon.doubleValue());
                }
            }
        }
        return item;
    }
}
