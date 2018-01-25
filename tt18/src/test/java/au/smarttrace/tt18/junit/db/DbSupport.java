/**
 *
 */
package au.smarttrace.tt18.junit.db;

import java.util.HashMap;
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
public class DbSupport {
    /**
     * JDBC template
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public DbSupport() {
        super();
    }

    /**
     * @param jdbc JDBC template.
     * @param name company name.
     * @return company ID.
     */
    public Long createSimpleCompany(final String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        final String sql = "insert into companies(name) values(:name)";

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(params), keyHolder);
        return keyHolder.getKey().longValue();
    }
    /**
     * @param jdbc JDBC template.
     */
    public void deleteCompanies() {
        jdbc.update("delete from companies", new HashMap<>());
    }
    /**
     * Deletes devices.
     */
    public void deleteDevices() {
        jdbc.update("delete from devices", new HashMap<>());
    }
    /**
     *
     */
    public void deleteMessages() {
        jdbc.update("delete from devicemsg", new HashMap<>());
    }
    /**
     * @return the jdbc
     */
    public NamedParameterJdbcTemplate getJdbc() {
        return jdbc;
    }

    /**
     * @param companyId
     * @param imei
     */
    public void createSimpleDevice(final Long companyId, final String imei) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", "JUnit");
        params.put("imei", imei);
        params.put("company", companyId);
        params.put("model", "TT18");

        final String sql = "insert into devices(name, imei, company, model) values(:name, :imei, :company, :model)";
        jdbc.update(sql, params);
    }
}
