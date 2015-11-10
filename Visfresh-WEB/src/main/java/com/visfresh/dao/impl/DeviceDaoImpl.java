/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.DeviceConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;
import com.visfresh.rules.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceDaoImpl extends EntityWithCompanyDaoImplBase<Device, String> implements DeviceDao {
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
     * Serial number field.
     */
    public static final String SN_FIELD = "sn";
    /**
     * Company name.
     */
    public static final String COMPANY_FIELD = "company";
    /**
     * Device trip count.
     */
    public static final String TRIPCOUNT_FIELD = "tripcount";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();
    private final DeviceStateSerializer stateSerializer = new DeviceStateSerializer();
    /**
     * Default constructor.
     */
    public DeviceDaoImpl() {
        super();

        propertyToDbFields.put(DeviceConstants.PROPERTY_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_NAME, NAME_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_SN, SN_FIELD);
        propertyToDbFields.put(DeviceConstants.PROPERTY_IMEI, IMEI_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Device> S save(final S device) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (findOne(device.getId()) == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD,
                    IMEI_FIELD,
                    SN_FIELD,
                    COMPANY_FIELD,
                    TRIPCOUNT_FIELD,
                    DESCRIPTION_FIELD
                ) + ")" + " values("
                    + ":"+ NAME_FIELD
                    + ", :" + IMEI_FIELD
                    + ", :" + SN_FIELD
                    + ", :" + COMPANY_FIELD
                    + ", :" + TRIPCOUNT_FIELD
                    + ", :" + DESCRIPTION_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + NAME_FIELD + ","
                + IMEI_FIELD + "=:" + IMEI_FIELD + ","
                + SN_FIELD + "=:" + SN_FIELD + ","
                + COMPANY_FIELD + "=:" + COMPANY_FIELD + ","
                + TRIPCOUNT_FIELD + "=:" + TRIPCOUNT_FIELD + ","
                + DESCRIPTION_FIELD + "=:" + DESCRIPTION_FIELD
                + " where imei = :" + IMEI_FIELD
            ;
        }

        paramMap.put(IMEI_FIELD, device.getId());
        paramMap.put(NAME_FIELD, device.getName());
        paramMap.put(DESCRIPTION_FIELD, device.getDescription());
        paramMap.put(IMEI_FIELD, device.getImei());
        paramMap.put(SN_FIELD, device.getSn());
        paramMap.put(COMPANY_FIELD, device.getCompany().getId());
        paramMap.put(TRIPCOUNT_FIELD, device.getTripCount());

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
        d.setSn((String) map.get(SN_FIELD));
        d.setImei((String) map.get(IMEI_FIELD));
        d.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        return d;
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
            jdbc.update("update devicestates set steate = :state where device = :device", params);
        }
    }
}
