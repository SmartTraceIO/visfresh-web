/**
 *
 */
package com.visfresh.db;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.Device;
import com.visfresh.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerEventDao {
    public static final String TABLE = "trackerevents";

    public static final String ID_FIELD = "id";
    public static final String TYPE_FIELD = "type";
    public static final String TIME_FIELD = "time";
    public static final String BATTERY_FIELD = "battery";
    public static final String TEMPERATURE_FIELD = "temperature";
    public static final String DEVICE_FIELD = "device";
    public static final String LATITUDE_FIELD = "latitude";
    public static final String LONGITUDE_FIELD = "longitude";

    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    /**
     * Default constructor.
     */
    public TrackerEventDao() {
        super();
    }

    public void create(final Device device, final TrackerEvent event) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        //insert
        paramMap.put("id", event.getId());
        final String sql = "insert into " + TABLE + " ("
                + TYPE_FIELD
                + "," + TIME_FIELD
                + "," + BATTERY_FIELD
                + "," + TEMPERATURE_FIELD
                + "," + DEVICE_FIELD
                + "," + LATITUDE_FIELD
                + "," + LONGITUDE_FIELD
                + ")" + " values("
                + ":"+ TYPE_FIELD
                + ", :" + TIME_FIELD
                + ", :" + BATTERY_FIELD
                + ", :" + TEMPERATURE_FIELD
                + ", :" + DEVICE_FIELD
                + ", :" + LATITUDE_FIELD
                + ", :" + LONGITUDE_FIELD
                + ")";

        paramMap.put(ID_FIELD, event.getId());
        paramMap.put(TYPE_FIELD, event.getType());
        paramMap.put(TIME_FIELD, event.getTime());
        paramMap.put(BATTERY_FIELD, event.getBattery());
        paramMap.put(TEMPERATURE_FIELD, event.getTemperature());
        paramMap.put(LATITUDE_FIELD, event.getLatitude());
        paramMap.put(LONGITUDE_FIELD, event.getLongitude());
        paramMap.put(DEVICE_FIELD, device.getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            event.setId(keyHolder.getKey().longValue());
        }
    }
}
