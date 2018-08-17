/**
 *
 */
package au.smarttrace.tt18.st.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

    /**
     * @param imei device IMEI code.
     * @return
     */
    public boolean checkDevice(final String imei) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", imei);
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select imei from devices where imei = :imei and active", params);
        return !rows.isEmpty();
    }
}
