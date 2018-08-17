/**
 *
 */
package au.smarttrace.spring.jdbc.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DataSourceTransactionManagerImpl extends DataSourceTransactionManager {
    private static final long serialVersionUID = 8205030473604858685L;

    /**
     * Default constructor.
     */
    @Autowired
    public DataSourceTransactionManagerImpl(final DataSourceImpl ds) {
        super();
        setDataSource(ds);
    }
}
