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
public class DeviceCommandDaoImpl extends DaoImplBase<DeviceCommand, Long> implements DeviceCommandDao {
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
