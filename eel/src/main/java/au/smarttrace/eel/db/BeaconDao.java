/**
 *
 */
package au.smarttrace.eel.db;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.GatewayBinding;

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
    private final String sql;

    /**
     * Default constructor.
     */
    public BeaconDao() {
        super();
        sql = loadSql();
    }

    public Beacon getBeaconById(final String imei) {
        final String entityName = "d";
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(IMEI_FIELD, imei);

        final Map<String, String> fields = createSelectAsMapping(entityName);
        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);

        return list.size() == 0 ? null : createBeacon(list.get(0));
    }
    /**
     * @param map
     * @return
     */
    private static Beacon createBeacon(final Map<String, Object> map) {
        final Beacon d = new Beacon();
        d.setImei((String) map.get(IMEI_FIELD));
        d.setActive(Boolean.TRUE.equals(map.get(ACTIVE_FIELD)));
        final Number company = (Number) map.get(COMPANY_FIELD);
        if (company != null) {
            d.setCompany(company.longValue());
        }

        final String ppa = (String) map.get("ppa");
        if (ppa != null) {
            d.setGateway(parseGateway(ppa));
        } else {
            final String ppb = (String) map.get("ppb");
            if (ppb != null) {
                d.setGateway(parseGateway(ppb));
            }
        }
        return d;
    }
    /**
     * @param json JSON to parse.
     * @return
     */
    private static GatewayBinding parseGateway(final String json) {
        final JsonObject e = new JsonParser().parse(new StringReader(json)).getAsJsonObject();
        final GatewayBinding g = new GatewayBinding();
        g.setId(e.get("id").getAsLong());
        g.setGateway(e.get("imei").getAsString());
        g.setActive(e.get("active").getAsBoolean());
        g.setCompany(e.get("company").getAsLong());
        return g;
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
     * @return
     */
    private String loadSql() {
        final StringBuilder sb = new StringBuilder();
        try {
            final Reader r = new InputStreamReader(
                    BeaconDao.class.getResourceAsStream("getBeacon.sql"), "UTF-8");

            try {
                int len;
                final char[] buff = new char[255];

                while ((len = r.read(buff)) > -1) {
                    sb.append(new String(buff, 0, len));
                }
            } finally {
                r.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    /**
     * @param imei
     * @return
     */
    public boolean isTrackerRegistered(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select imei from devices where imei = :imei and active", params);

        return !list.isEmpty();
    }
}
