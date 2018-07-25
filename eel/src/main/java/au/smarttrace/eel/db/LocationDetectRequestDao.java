/**
 *
 */
package au.smarttrace.eel.db;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationDetectRequestDao {
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public LocationDetectRequestDao() {
        super();
    }

    /**
     * @param msg message to send.
     */
    public void sendRequest(final DeviceMessage msg) {
        final Map<String, Object> params = createBaseMessageParameters(msg);

        //encode station signals
        final StringBuilder sb = new StringBuilder();
        for (final StationSignal station : msg.getStationSignals()) {
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
            + "(imei, type, time, battery, temperature, numretry, retryon, stations, humidity, radio) "
            + "VALUES(:imei, :type, :time, :battery, :temperature, :numretry, :readyon, :stations, :humidity, :radio)"
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
        map.put("type", msg.getType());
        //time datetime NOT NULL,
        map.put("time", msg.getTime());
        //battery int NOT NULL,
        map.put("battery", new Integer(msg.getBattery()));
        //temperature float NOT NULL,
        map.put("temperature", new Float(msg.getTemperature()));
        //humidity
        map.put("humidity", msg.getHumidity());
        //current retry number.
        map.put("numretry", 0);
        //the ready time.
        map.put("readyon", new Date());
        map.put("radio", msg.isLte() ? "lte" : "gsm");

        return map;
    }
}
