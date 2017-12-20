/**
 *
 */
package au.smarttrace.init;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
public class JdbcConfig implements TransactionManagementConfigurer {
    @Autowired
    private Environment env;

    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }

    /**
     * @return JDBC data source.
     */
    protected DataSource createDataSource() {
        //data source
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("dataSource.driverClassName"));
        config.setJdbcUrl(env.getProperty("dataSource.url"));
        config.setUsername(env.getProperty("dataSource.username"));
        config.setPassword(env.getProperty("dataSource.password"));
        return new HikariDataSource(config);
    }
    /**
     * @return JDBC template.
     */
    @Bean
    public NamedParameterJdbcTemplate configureJdbcTemplate(final DataSourceTransactionManager ds) {
        return new NamedParameterJdbcTemplate(ds.getDataSource());
    }
    /**
     * @return JDBC data source.
     */
    @Bean
    public DataSource getDataSource(final DataSourceTransactionManager ds) {
        return ds.getDataSource();
    }
    /* (non-Javadoc)
     * @see org.springframework.transaction.annotation.TransactionManagementConfigurer#annotationDrivenTransactionManager()
     */
    @Bean
    @Override
    public DataSourceTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(createDataSource());
    }
}
