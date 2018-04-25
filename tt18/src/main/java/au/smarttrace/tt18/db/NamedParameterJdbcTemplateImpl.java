/**
 *
 */
package au.smarttrace.tt18.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NamedParameterJdbcTemplateImpl extends NamedParameterJdbcTemplate {
    /**
     * @param dataSource
     */
    @Autowired
    public NamedParameterJdbcTemplateImpl(final DataSourceTransactionManager mgr) {
        super(mgr.getDataSource());
    }
}
