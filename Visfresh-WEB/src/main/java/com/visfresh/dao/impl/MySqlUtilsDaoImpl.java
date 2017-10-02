/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.MySqlUtilsDao;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MySqlUtilsDaoImpl implements MySqlUtilsDao {
    /**
     * JDBC template.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public MySqlUtilsDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.MySqlUtilsDao#buildCurrentProcessList()
     */
    @Override
    public String getCurrentProcesses() {
        final StringBuilder sb = new StringBuilder();

        final List<Map<String, Object>> rows = jdbc.queryForList("show processlist", new HashMap<>());
        for (final Map<String, Object> row : rows) {
            sb.append(processRowToString(row));
            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * @param row process row.
     * @return string representation of process row.
     */
    private String processRowToString(final Map<String, Object> row) {
        final List<String> list = new LinkedList<>();
        for (final Map.Entry<String, Object> e : row.entrySet()) {
            list.add(e.getKey() + "=" + e.getValue());
        }

        return StringUtils.combine(list, ", ");
    }
}
