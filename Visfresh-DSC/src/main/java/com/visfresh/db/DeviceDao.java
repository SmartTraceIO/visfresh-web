/**
 *
 */
package com.visfresh.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceDao {
    public static final String TABLE = "devices";

    public static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "name";
    public static final String IMEI_FIELD = "imei";
    public static final String ACTIVE_FIELD = "active";

    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public DeviceDao() {
        super();
    }

    public Device getByImei(final String imei) {
        final String entityName = "d";
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(IMEI_FIELD, imei);

        final Map<String, String> fields = createSelectAsMapping(entityName);
        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from "
                + TABLE + " " + entityName
                + " where "
                + entityName + "." + IMEI_FIELD + " = :" + IMEI_FIELD,
                params);

        return list.size() == 0 ? null : createDevice(list.get(0));
    }
    /**
     * @param map
     * @return
     */
    public static Device createDevice(final Map<String, Object> map) {
        final Device d = new Device();
        d.setName((String) map.get(NAME_FIELD));
        d.setDescription((String) map.get(DESCRIPTION_FIELD));
        d.setImei((String) map.get(IMEI_FIELD));
        d.setActive(Boolean.TRUE.equals(map.get(ACTIVE_FIELD)));
        return d;
    }
    /**
     * @param entityName
     * @return
     */
    public static Map<String, String> createSelectAsMapping(final String entityName) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + NAME_FIELD, NAME_FIELD);
        map.put(entityName + "." + DESCRIPTION_FIELD, DESCRIPTION_FIELD);
        map.put(entityName + "." + IMEI_FIELD, IMEI_FIELD);
        map.put(entityName + "." + ACTIVE_FIELD, ACTIVE_FIELD);
        return map ;
    }
}
