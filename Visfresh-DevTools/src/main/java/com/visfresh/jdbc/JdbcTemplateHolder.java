/**
 *
 */
package com.visfresh.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JdbcTemplateHolder {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public JdbcTemplateHolder() {
        super();
    }

    /**
     * @return the jdbc
     */
    public NamedParameterJdbcTemplate getJdbc() {
        return jdbc;
    }
}
