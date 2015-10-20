/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.jdbc.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {JdbcConfig.class, DaoImplBase.class})
@PropertySource("classpath:/junit.app.properties")
public class JUnitConfig {
    /**
     * Default constructor.
     */
    public JUnitConfig() {
        super();
    }
}
