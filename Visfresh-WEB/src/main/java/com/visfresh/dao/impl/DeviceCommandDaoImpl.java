/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandDaoImpl extends DaoImplBase<DeviceCommand, Long> implements DeviceCommandDao {
    /**
     * Table name.
     */
    public static final String TABLE = "devicecommands";
    /**
     * ID field.
     */
    private static final String ID_FIELD = "id";
    /**
     * Device command.
     */
    private static final String COMMAND_FIELD = "command";
    /**
     * Reference to device.
     */
    private static final String DEVICE_FIELD = "device";

    /**
     * Default constructor.
     */
    public DeviceCommandDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends DeviceCommand> S save(final S cmd) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (cmd.getId() == null) {
            //insert
            paramMap.put("id", cmd.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    COMMAND_FIELD
                    , DEVICE_FIELD
                ) + ")" + " values("
                    + ":"+ COMMAND_FIELD
                    + ", :" + DEVICE_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + COMMAND_FIELD + "=:" + COMMAND_FIELD + ","
                + DEVICE_FIELD + "=:" + DEVICE_FIELD
                + " where id = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, cmd.getId());
        paramMap.put(COMMAND_FIELD, cmd.getCommand());
        paramMap.put(DEVICE_FIELD, cmd.getDevice().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            cmd.setId(keyHolder.getKey().longValue());
        }

        return cmd;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public DeviceCommand findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(id, entityName,
                companyEntityName, resultPrefix, companyResultPrefix,
                deviceEntityName, deviceResultPrefix);
        return list.size() == 0 ? null : createDeviceCommand(resultPrefix,
                deviceResultPrefix, companyResultPrefix, list.get(0));
    }
    /**
     * @param resultPrefix
     * @param deviceResultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    private DeviceCommand createDeviceCommand(final String resultPrefix, final String deviceResultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        final DeviceCommand a = new DeviceCommand();
        a.setId(((Number) map.get(resultPrefix + ID_FIELD)).longValue());
        a.setDevice(DeviceDaoImpl.createDevice(deviceResultPrefix, companyResultPrefix, map));
        a.setCommand((String) map.get(resultPrefix + COMMAND_FIELD));
        return a;
    }
    /**
     * @param id
     * @param entityName
     * @param companyEntityName
     * @param resultPrefix
     * @param companyResultPrefix
     * @param deviceEntityName
     * @param deviceResultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id,
            final String entityName, final String companyEntityName,
            final String resultPrefix, final String companyResultPrefix,
            final String deviceEntityName, final String deviceResultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        final Map<String, String> companyFields = CompanyDaoImpl.createSelectAsMapping(
                companyEntityName, companyResultPrefix);
        final Map<String, String> deviceFields = DeviceDaoImpl.createSelectAsMapping(
                deviceEntityName, deviceResultPrefix);

        params.putAll(fields);
        params.putAll(companyFields);
        params.putAll(deviceFields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + ", " + buildSelectAs(deviceFields)
                + ", " + buildSelectAs(companyFields)
                + " from "
                + TABLE + " " + entityName
                + ", " + DeviceDaoImpl.TABLE + " " + deviceEntityName
                + ", " + CompanyDaoImpl.TABLE + " " + companyEntityName
                + " where "
                + entityName + "." + DEVICE_FIELD + " = "
                + deviceEntityName + "." + DeviceDaoImpl.IMEI_FIELD
                + " and " + deviceEntityName + "." + DeviceDaoImpl.COMPANY_FIELD + " = "
                + companyEntityName + "." + CompanyDaoImpl.ID_FIELD
                + (id == null ? "" : " and " + entityName + "." + ID_FIELD + " = :id"),
                params);
        return list;
    }
    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + ID_FIELD, resultPrefix + ID_FIELD);
        map.put(entityName + "." + COMMAND_FIELD, resultPrefix + COMMAND_FIELD);
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<DeviceCommand> findAll() {
        final String entityName = "a";
        final String companyEntityName = "c";
        final String resultPrefix = "a_";
        final String companyResultPrefix = "c_";
        final String deviceEntityName = "d";
        final String deviceResultPrefix = "d_";

        final List<Map<String, Object>> list = runSelectScript(null, entityName, companyEntityName, resultPrefix,
                companyResultPrefix, deviceEntityName, deviceResultPrefix);

        final List<DeviceCommand> result = new LinkedList<DeviceCommand>();
        for (final Map<String,Object> map : list) {
            result.add(createDeviceCommand(resultPrefix, deviceResultPrefix, companyResultPrefix, map));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }
}
