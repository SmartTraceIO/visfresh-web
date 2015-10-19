/**
 *
 */
package com.visfresh.junit.db;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.JdbcConfigBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {DaoImplBase.class})
public class JUnitJdbcConfig extends JdbcConfigBase {
    /**
     * Default constructor.
     */
    public JUnitJdbcConfig() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#getPropertyFilePrefix()
     */
    @Override
    protected String getPropertyFilePrefix() {
        return "junit";
    }

    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#annotationDrivenTransactionManager()
     */
    @Override
    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return super.annotationDrivenTransactionManager();
    }
    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#configureDataSource()
     */
    @Override
    @Bean(name = "dataSource")
    public DataSource configureDataSource() {
        return super.configureDataSource();
    }
    /* (non-Javadoc)
     * @see com.visfresh.init.JdbcConfigBase#configureJdbcTemplate()
     */
    @Override
    @Bean
    public NamedParameterJdbcTemplate configureJdbcTemplate() {
        return super.configureJdbcTemplate();
    }
}
