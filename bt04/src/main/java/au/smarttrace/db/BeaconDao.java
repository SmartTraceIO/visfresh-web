/**
 *
 */
package au.smarttrace.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import au.smarttrace.Beacon;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BeaconDao {
    public static final String TABLE = "devices";

    public static final String IMEI_FIELD = "imei";
    public static final String ACTIVE_FIELD = "active";
    public static final String COMPANY_FIELD = "company";

    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public BeaconDao() {
        super();
    }

    public Beacon getByImei(final String imei) {
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
    public static Beacon createDevice(final Map<String, Object> map) {
        final Beacon d = new Beacon();
        d.setImei((String) map.get(IMEI_FIELD));
        d.setActive(Boolean.TRUE.equals(map.get(ACTIVE_FIELD)));
        d.setCompany(((Number) map.get(COMPANY_FIELD)).longValue());
        return d;
    }
    /**
     * @param entityName
     * @return
     */
    public static Map<String, String> createSelectAsMapping(final String entityName) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + IMEI_FIELD, IMEI_FIELD);
        map.put(entityName + "." + ACTIVE_FIELD, ACTIVE_FIELD);
        map.put(entityName + "." + COMPANY_FIELD, COMPANY_FIELD);
        return map ;
    }
    /**
     * @param imei
     * @return
     */
    public String getCompanyEmail(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select email from companies"
                + " join devices on devices.company = companies.id and devices.imei = :imei limit 1",
                params);

        return list.size() == 0 ? null : (String) list.get(0).get("email");
    }
}
