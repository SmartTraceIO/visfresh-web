/**
 *
 */
package com.visfresh.init;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class JdbcConfigBase implements TransactionManagementConfigurer {
    protected final Properties configuration;
    private final DataSource dataSource;

    /**
     * Default constructor.
     */
    public JdbcConfigBase() {
        super();

        final Properties props = new Properties();
        final InputStream in = new BufferedInputStream(JdbcConfigBase.class.getClassLoader().getResourceAsStream(
                getPropertyFilePrefix() + ".db.configuration"));
        try {
            try {
                props.load(in);
            } finally {
                in.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.configuration = props;

        //data source
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(configuration.getProperty("dataSource.driverClassName"));
        config.setJdbcUrl(configuration.getProperty("dataSource.url"));
        config.setUsername(configuration.getProperty("dataSource.username"));
        config.setPassword(configuration.getProperty("dataSource.password"));

        dataSource = new HikariDataSource(config);
    }

    /**
     * @return JDBC data source.
     */
    public DataSource configureDataSource() {
        return dataSource;
    }
    /**
     * @return JDBC template.
     */
    public NamedParameterJdbcTemplate configureJdbcTemplate() {
        return new NamedParameterJdbcTemplate(configureDataSource());
    }
    /* (non-Javadoc)
     * @see org.springframework.transaction.annotation.TransactionManagementConfigurer#annotationDrivenTransactionManager()
     */
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(configureDataSource());
    }
    /**
     * @return returns the prefix of configuration file.
     */
    protected abstract String getPropertyFilePrefix();
}
