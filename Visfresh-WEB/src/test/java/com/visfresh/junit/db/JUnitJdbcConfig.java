/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.base.JdbcConfigBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {JdbcConfigBase.class, DaoImplBase.class})
@PropertySource("classpath:/junit.db.configuration")
public class JUnitJdbcConfig {
    /**
     * Default constructor.
     */
    public JUnitJdbcConfig() {
        super();
    }
}
