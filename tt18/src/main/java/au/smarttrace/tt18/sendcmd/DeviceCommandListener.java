/**
 *
 */
package au.smarttrace.tt18.sendcmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandListener {
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public DeviceCommandListener() {
        super();
    }

    @Scheduled(fixedDelay = 15 * 1000l)
    public void checkAndSendDeviceCommands() {

    }
}
