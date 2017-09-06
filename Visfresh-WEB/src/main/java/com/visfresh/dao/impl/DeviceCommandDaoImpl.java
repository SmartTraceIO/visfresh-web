/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandDaoImpl extends DaoImplBase<DeviceCommand, DeviceCommand, Long> implements DeviceCommandDao {
    /**
     * Table name.
     */
    public static final String TABLE = "devicecommands";

    private static final String ID_FIELD = "id";
    private static final String COMMAND_FIELD = "command";
    private static final String DEVICE_FIELD = "device";

    @Autowired
    private DeviceDao deviceDao;

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();
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
    public <S extends DeviceCommand> S saveImpl(final S cmd) {
        final Long commandId = cmd.getId();
        final String command = cmd.getCommand();
        final String deviceImei = cmd.getDevice().getImei();

        final Long generatedId = saveCommand(commandId, command, deviceImei);
        if (generatedId != null) {
            cmd.setId(generatedId);
        }

        return cmd;
    }

    /**
     * @param commandId
     * @param command
     * @param deviceImei
     * @return
     */
    protected Long saveCommand(final Long commandId, final String command,
            final String deviceImei) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        String sql;

        if (commandId == null) {
            //insert
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

        paramMap.put(ID_FIELD, commandId);
        paramMap.put(COMMAND_FIELD, command);
        paramMap.put(DEVICE_FIELD, deviceImei);
        final MapSqlParameterSource sqlParamMap = new MapSqlParameterSource(paramMap);

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, sqlParamMap, keyHolder);
        final Number generatedId = keyHolder.getKey();

        return generatedId == null ? null : generatedId.longValue();
    }
    @Override
    public void saveCommand(final String command, final String imei) {
        this.saveCommand(null, command, imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceCommandDao#deleteCommandsFor(com.visfresh.entities.Device)
     */
    @Override
    public void deleteCommandsFor(final Device d) {
        final String sql = "delete from " + TABLE + " where device = :device";
        final Map<String, Object> params = new HashMap<>();
        params.put("device", d.getImei());

        jdbc.update(sql, params);
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
    protected DeviceCommand createEntity(final Map<String, Object> map) {
        final DeviceCommand a = new DeviceCommand();
        a.setId(((Number) map.get(ID_FIELD)).longValue());
        a.setCommand((String) map.get(COMMAND_FIELD));
        return a;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final DeviceCommand cmd, final Map<String, Object> row,
            final Map<String, Object> cache) {
        final String imei = ((String) row.get(DEVICE_FIELD));
        Device device = (Device) cache.get(imei);
        if (device == null) {
            device = deviceDao.findOne(imei);
            cache.put(imei, device);
        }
        cmd.setDevice(device);
    }
}
