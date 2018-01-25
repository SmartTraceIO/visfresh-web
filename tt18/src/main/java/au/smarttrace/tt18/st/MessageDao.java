/**
 *
 */
package au.smarttrace.tt18.st;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MessageDao {
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public MessageDao() {
        super();
    }

    public void saveForNextProcessingInDcs(final DeviceMessage msg) {
        createDeviceMessage(msg);
    }

    /**
     * @param msg device message.
     */
    private void createDeviceMessage(final DeviceMessage msg) {
        final Map<String, Object> params = createBaseMessageParameters(msg);

        //encode station signals
        final StringBuilder sb = new StringBuilder();
        for (final StationSignal station : msg.getStations()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(station.getMcc()).append('|');
            sb.append(station.getMnc()).append('|');
            sb.append(station.getLac()).append('|');
            sb.append(station.getCi()).append('|');
            sb.append(station.getLevel()).append('|');
        }

        //stations varchar(256) NOT NULL,
        params.put("stations", sb.toString());

        final GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbc.update("INSERT INTO devicemsg"
            + "(imei, type, time, battery, temperature, numretry, retryon, stations) "
            + "VALUES(:imei, :type, :time, :battery, :temperature, :numretry, :readyon, :stations)"
            , new MapSqlParameterSource(params), holder);

        msg.setId(holder.getKey().longValue());
    }

    /**
     * @param msg
     * @return
     */
    private Map<String, Object> createBaseMessageParameters(final DeviceMessage msg) {
        final Map<String, Object> map = new HashMap<String, Object>();
        //imei varchar(15) NOT NULL,
        map.put("imei", msg.getImei());
        //type varchar(3) NOT NULL,
        map.put("type", msg.getType().name());
        //time datetime NOT NULL,
        map.put("time", msg.getTime());
        //battery int NOT NULL,
        map.put("battery", new Integer(msg.getBattery()));
        //temperature float NOT NULL,
        map.put("temperature", new Float(msg.getTemperature()));
        //current retry number.
        map.put("numretry", msg.getNumberOfRetry());
        //the ready time.
        map.put("readyon", msg.getRetryOn());

        return map;
    }

    /**
     * @param imei device IMEI code.
     * @return
     */
    public boolean checkDevice(final String imei) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", imei);
        final List<Map<String, Object>> rows = jdbc.queryForList("select imei from devices where imei = :imei and active", params);
        return !rows.isEmpty();
    }
}
