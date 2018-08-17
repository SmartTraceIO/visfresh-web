/**
 *
 */
package au.smarttrace.spring.jdbc.impl;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NamedParameterJdbcTemplateImpl extends NamedParameterJdbcTemplate {
    private DataSourceImpl dataSource;
    /**
     * @param ds data source.
     */
    @Autowired
    public NamedParameterJdbcTemplateImpl(final DataSourceImpl ds) {
        super(ds);
        this.dataSource = ds;
    }
    /**
     * Destroys data source.
     */
    @PreDestroy
    public void destroy() {
        dataSource.close();
    }
}
