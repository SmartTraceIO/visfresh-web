/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.init.base.JdbcConfigBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:/prod.jdbc.configuration")
@ComponentScan(basePackageClasses = {JdbcConfigBase.class})
public class ProductionJdbcConfig {
    /**
     * Default constructor.
     */
    public ProductionJdbcConfig() {
        super();
    }
}
