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
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.DeviceModel;
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
public class DeviceDaoImpl extends EntityWithCompanyDaoImplBase<Device, Device, String> implements DeviceDao {
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
    public static final String MODEL_FIELD = "model";
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
    private static final String GET_DEVICES_SQL = StringUtils.loadSql("getDevices");

    /**
     * Default constructor.
     */
    public DeviceDaoImpl() {
        super();
        propertyToDbFields.put(DeviceConstants.PROPERTY_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_NAME, NAME_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_IMEI, IMEI_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_MODEL, MODEL_FIELD);
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
        paramMap.put(MODEL_FIELD, device.getModel().name());
        paramMap.put(DESCRIPTION_FIELD, device.getDescription());
        paramMap.put(IMEI_FIELD, device.getImei());
        paramMap.put(COMPANY_FIELD, device.getCompanyId());
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
        d.setModel(DeviceModel.valueOf((String) map.get(MODEL_FIELD)));
        d.setDescription((String) map.get(DESCRIPTION_FIELD));
        d.setImei((String) map.get(IMEI_FIELD));
        final Number companyId = (Number) map.get(COMPANY_FIELD);
        if (companyId != null) { //null is impossible, but handled now.
            d.setCompany(companyId.longValue());
        }
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
        final DeviceState st = stateSerializer.parseState(state);

        return st;
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
        filter.addFilter(COMPANY_FIELD, group.getCompanyId());
        filter.addFilter(PROPERTY_DEVICE_GROUP, group);
        final Sorting sorting = new Sorting(NAME_FIELD);

        return findAll(filter, sorting, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#moveToNewCompany(com.visfresh.entities.Device, com.visfresh.entities.Company)
     */
    @Override
    public void moveToNewCompany(final Device device, final Long c) {
        final String sql = "update devices set company=:company, "
                + TRIPCOUNT_FIELD + "= 0,"
                + AUTOSTART_TEMPLATE_FIELD + "= NULL where imei=:device";
        final Map<String, Object> params = new HashMap<>();
        params.put("device", device.getImei());
        params.put("company", c);

        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    public SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()) {
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
        };
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#getListDeviceItems(com.visfresh.entities.Company, com.visfresh.dao.Sorting, com.visfresh.dao.Page)
     */
    @Override
    public List<ListDeviceItem> getDevices(final Long company,
            final Sorting sorting, final Page page) {
        String sql = GET_DEVICES_SQL;

        if (sorting != null && sorting.getSortProperties().length > 0) {
            sql += "order by ";
            final StringBuilder sb = new StringBuilder();
            for (final String prop : sorting.getSortProperties()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(prop);
                sb.append(sorting.isAscentDirection(prop) ? " asc" : " desc");
            }
            sql += sb.toString();
        }
        if (page != null) {
            sql += " limit " + ((page.getPageNumber() - 1) * page.getPageSize()) + "," + page.getPageSize();
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("company", company);

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
        item.setModel(DeviceModel.valueOf((String) row.get(DeviceConstants.PROPERTY_MODEL)));
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Device t, final Map<String, Object> map, final Map<String, Object> cache) {
    }
}
