/**
 *
 */
package au.smarttrace.junit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class TestUtils {
    /**
     * Default constructor.
     */
    private TestUtils() {
        super();
    }

    /**
     * @param jdbc JDBC template.
     * @param name company name.
     * @return company ID.
     */
    public static Long createSimpleCompany(final NamedParameterJdbcTemplate jdbc, final String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        final String sql = "insert into companies(name) values(:name)";

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(params), keyHolder);
        return keyHolder.getKey().longValue();
    }
    /**
     * Deletes all companies.
     * @param jdbc JDBC template.
     */
    public static void deleteCompanies(final NamedParameterJdbcTemplate jdbc) {
        jdbc.update("delete from companies", new HashMap<>());
    }
}
