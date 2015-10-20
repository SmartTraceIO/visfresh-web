/**
 *
 */
package com.visfresh.db;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandDao {
    public static final String TABLE = "devicecommands";

    private static final String ID_FIELD = "id";
    private static final String COMMAND_FIELD = "command";
    private static final String DEVICE_FIELD = "device";

    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    /**
     * Default constructor.
     */
    public DeviceCommandDao() {
        super();
    }

    public List<DeviceCommand> getFoDevice(final String id) {
        final String entityName = "cmd";

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("device", id);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from "
                + TABLE + " " + entityName
                + " where "
                + entityName + "." + DEVICE_FIELD + " = :device order by "
                        + entityName + "." + ID_FIELD,
                params);

        final List<DeviceCommand> commands = new LinkedList<DeviceCommand>();
        for (final Map<String,Object> row : list) {
            commands.add(createDeviceCommand(row));
        }
        return commands;
    }
    /**
     * @param cmd
     */
    public void delete(final DeviceCommand cmd) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", cmd.getId());
        jdbc.update("delete from " + TABLE + " where id = :id", params);
    }
    /**
     * @param row
     * @return
     */
    private DeviceCommand createDeviceCommand(final Map<String, Object> row) {
        final DeviceCommand a = new DeviceCommand();
        a.setId(((Number) row.get(ID_FIELD)).longValue());
        a.setCommand((String) row.get(COMMAND_FIELD));
        return a;
    }
}
