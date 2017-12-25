/**
 *
 */
package au.smarttrace.dao.runner;

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
     * Deletes shipments.
     */
    public void deleteShipments() {
        jdbc.update("delete from shipments", new HashMap<>());
    }
    /**
     *
     */
    public void deleteUsers() {
        //delete all users, should automatically delete sessions.
        jdbc.update("delete from users", new HashMap<>());
    }
    /**
     * Deletes devices.
     */
    public void deleteDevices() {
        jdbc.update("delete from devices", new HashMap<>());
    }
    /**
     * @return the jdbc
     */
    public NamedParameterJdbcTemplate getJdbc() {
        return jdbc;
    }
}
