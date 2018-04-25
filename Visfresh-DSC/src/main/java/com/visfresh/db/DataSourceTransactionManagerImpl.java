/**
 *
 */
package com.visfresh.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
    public DataSourceTransactionManagerImpl(final Environment env) {
        super();
        setDataSource(createDataSource(env));
    }
    /**
     * @return JDBC data source.
     */
    private static DataSource createDataSource(final Environment env) {
        //data source
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("dataSource.driverClassName"));
        config.setJdbcUrl(env.getProperty("dataSource.url"));
        config.setUsername(env.getProperty("dataSource.username"));
        config.setPassword(env.getProperty("dataSource.password"));
        return new HikariDataSource(config);
    }
}
