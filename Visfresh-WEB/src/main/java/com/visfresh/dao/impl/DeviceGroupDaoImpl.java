/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.DeviceGroupConstants;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceGroupDaoImpl extends EntityWithCompanyDaoImplBase<DeviceGroup, Long> implements DeviceGroupDao {
    /**
     * Table name.
     */
    public static final String TABLE = "devicegroups";

    private static final String PROPERTY_DEVICE = "device";
    private static final String ID_FIELD = "id";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "name";
    public static final String COMPANY_FIELD = "company";

    public static final String RELATIONS_TABLE = "devicegrouprelations";
    public static final String RELATIONS_DEVICE = PROPERTY_DEVICE;
    public static final String RELATIONS_GROUP = "group";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public DeviceGroupDaoImpl() {
        super();

        propertyToDbFields.put(DeviceGroupConstants.PROPERTY_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(DeviceGroupConstants.PROPERTY_NAME, NAME_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends DeviceGroup> S save(final S group) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (group.getId() == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD,
                    COMPANY_FIELD,
                    DESCRIPTION_FIELD
                ) + ")" + " values("
                    + ":"+ NAME_FIELD
                    + ", :" + COMPANY_FIELD
                    + ", :" + DESCRIPTION_FIELD
                    + ")";
        } else {
            paramMap.put(ID_FIELD, group.getId());

            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + NAME_FIELD + ","
                + COMPANY_FIELD + "=:" + COMPANY_FIELD + ","
                + DESCRIPTION_FIELD + "=:" + DESCRIPTION_FIELD
                + " where id = :" + ID_FIELD
            ;
        }

        paramMap.put(NAME_FIELD, group.getName());
        paramMap.put(DESCRIPTION_FIELD, group.getDescription());
        paramMap.put(COMPANY_FIELD, group.getCompany().getId());

        if (group.getId() == null) {
            final GeneratedKeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(sql, new MapSqlParameterSource(paramMap), kh);
            group.setId(kh.getKey().longValue());
        } else {
            jdbc.update(sql, paramMap);
        }

        return group;
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
    protected DeviceGroup createEntity(final Map<String, Object> map) {
        final DeviceGroup g = new DeviceGroup();
        g.setId(((Number) map.get(ID_FIELD)).longValue());
        g.setName((String) map.get(NAME_FIELD));
        g.setDescription((String) map.get(DESCRIPTION_FIELD));
        return g;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#addDevice(com.visfresh.entities.DeviceGroup, com.visfresh.entities.Device)
     */
    @Override
    public void addDevice(final DeviceGroup group, final Device device) {
        final String sql = "insert into " + RELATIONS_TABLE + "(`"
                + RELATIONS_GROUP + "`, " + RELATIONS_DEVICE + ") values (:group, :device)";

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("group", group.getId());
        params.put(PROPERTY_DEVICE, device.getId());

        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#moveToNewDevice(com.visfresh.entities.Device, com.visfresh.entities.Device)
     */
    @Override
    public void moveToNewDevice(final Device oldDevice, final Device newDevice) {
        final String sql = "update " + RELATIONS_TABLE + " set device = :new where device = :old";
        final Map<String, Object> params = new HashMap<>();
        params.put("old", oldDevice.getImei());
        params.put("new", newDevice.getImei());

        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#removeDevice(com.visfresh.entities.DeviceGroup, com.visfresh.entities.Device)
     */
    @Override
    public void removeDevice(final DeviceGroup group, final Device device) {
        final String sql = "delete from " + RELATIONS_TABLE
                + " where `" + RELATIONS_GROUP + "` = :group and " + RELATIONS_DEVICE + " = :device";

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("group", group.getId());
        params.put(PROPERTY_DEVICE, device.getId());

        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#getDevices(java.lang.String)
     */
    @Override
    public List<DeviceGroup> findByDevice(final Device device) {
        final Filter filter = new Filter();
        filter.addFilter(COMPANY_FIELD, device.getCompany().getId());
        filter.addFilter(PROPERTY_DEVICE, device);
        final Sorting sorting = new Sorting(NAME_FIELD);

        return findAll(filter, sorting, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.Object, java.util.Map, java.util.List)
     */
    @Override
    protected void addFilterValue(final String property, final Object value,
            final Map<String, Object> params, final List<String> filters) {
        if (PROPERTY_DEVICE.equals(property)) {
            final String imei = value instanceof Device ? ((Device) value).getImei() : value.toString();
            final String key = DEFAULT_FILTER_KEY_PREFIX + "imei";

            filters.add(ID_FIELD + " in (select `" + RELATIONS_GROUP
                    + "` from " + RELATIONS_TABLE + " where " + RELATIONS_DEVICE + " = :" + key + ")");
            params.put(key, imei);
        } else {
            super.addFilterValue(property, value, params, filters);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#getShipmentGroups(java.util.Set)
     */
    @Override
    public Map<Long, List<DeviceGroupDto>> getShipmentGroups(final Set<Long> ids) {
        final Map<Long, List<DeviceGroupDto>> map = new HashMap<>();
        //prepare map
        for (final Long id : ids) {
            map.put(id, new LinkedList<DeviceGroupDto>());
        }

        //check map of ID is empty
        if (ids.isEmpty()) {
            return map;
        }

        final String sql = "select dg.*, s.id as shipment from " + TABLE + " dg"
                + " join devicegrouprelations rel on rel.group = dg.id"
                + " join devices d on rel.device = d.imei"
                + " join shipments s on s.device = d.imei"
                + " where s.id in(" + StringUtils.combine(ids, ",") + ") order by dg.name";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, new HashMap<String, Object>());
        for (final Map<String, Object> row : rows) {
            final Long shipment = ((Number) row.get("shipment")).longValue();
            map.get(shipment).add(new DeviceGroupDto(createEntity(row)));
        }
        return map;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceGroupDao#findByName(java.lang.String)
     */
    @Override
    public DeviceGroup findByName(final String groupName) {
        final Filter f = new Filter();
        f.addFilter(NAME_FIELD, groupName);

        final List<DeviceGroup> groups = findAll(f, new Sorting(ID_FIELD), new Page(1, 1));
        return groups.isEmpty() ? null : groups.get(0);
    }
}
