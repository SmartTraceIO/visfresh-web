/**
 *
 */
package au.smarttrace.spring.jdbc.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DataSourceImpl extends HikariDataSource {
    /**
     * Default constructor.
     */
    @Autowired
    public DataSourceImpl(final Environment env) {
        super(createConfiguration(env));
    }
    /**
     * @param env
     * @return
     */
    private static HikariConfig createConfiguration(final Environment env) {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("dataSource.driverClassName"));
        config.setJdbcUrl(env.getProperty("dataSource.url"));
        config.setUsername(env.getProperty("dataSource.username"));
        config.setPassword(env.getProperty("dataSource.password"));
        config.setMaximumPoolSize(Integer.parseInt(env.getProperty("dataSource.numConnections", "3")));
        return config;
    }
}
