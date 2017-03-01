/**
 *
 */
package com.visfresh.jdbc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.tracker.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerEventDao {

    /**
     * Table name.
     */
    private static final String TABLE = "trackerevents";

    private static final String TYPE_FIELD = "type";
    private static final String TIME_FIELD = "time";
    private static final String CREATED_ON_FIELD = "createdon";
    private static final String BATTERY_FIELD = "battery";
    private static final String TEMPERATURE_FIELD = "temperature";
    private static final String LATITUDE_FIELD = "latitude";
    private static final String LONGITUDE_FIELD = "longitude";
    private static final String DEVICE_FIELD = "device";

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     */
    public TrackerEventDao() {
        super();
    }

    public void save(final DeviceMessage event, final Long shipment) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.put(CREATED_ON_FIELD, event.getTime());
        paramMap.put(TYPE_FIELD, event.getType().toString());
        paramMap.put(TIME_FIELD, event.getTime());
        paramMap.put(BATTERY_FIELD, event.getBattery());
        paramMap.put(TEMPERATURE_FIELD, event.getTemperature());
        if (event.getLocation() != null) {
            paramMap.put(LATITUDE_FIELD, event.getLocation().getLatitude());
            paramMap.put(LONGITUDE_FIELD, event.getLocation().getLongitude());
        }
        paramMap.put(DEVICE_FIELD, event.getImei());
        if (shipment != null) {
            paramMap.put("shipment", shipment);
        }

        final String sql = createInsertScript(paramMap.keySet());
        jdbc.update(sql, paramMap);
    }
    /**
     * @param fields fields.
     * @param idFieldName ID field name.
     * @return insert.
     */
    private String createInsertScript(final Collection<String> fields) {
        final StringBuilder names = new StringBuilder();
        final StringBuilder values = new StringBuilder();

        boolean first = true;
        for (final String field : fields) {
            if (!first) {
                names.append(',');
                values.append(',');
            } else {
                first = false;
            }

            names.append(field);
            values.append(':').append(field);
        }

        return "insert into " + TABLE + "(" + names + ") values (" + values + ")";
    }
}
