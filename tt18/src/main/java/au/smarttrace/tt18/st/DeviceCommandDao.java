/**
 *
 */
package au.smarttrace.tt18.st;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandDao {
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    /**
     * Default constructor.
     */
    public DeviceCommandDao() {
        super();
    }

    public List<DeviceCommand> getFoDevice(final String id) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("device", id);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from devicecommands cmd where cmd.device = :device order by cmd.id",
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
        jdbc.update("delete from devicecommands where id = :id", params);
    }
    /**
     * @param row
     * @return
     */
    private DeviceCommand createDeviceCommand(final Map<String, Object> row) {
        final DeviceCommand a = new DeviceCommand();
        a.setId(((Number) row.get("id")).longValue());
        a.setCommand((String) row.get("command"));
        return a;
    }
}
