/**
 *
 */
package com.visfresh.spring.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.db.DeviceCommandDao;

import au.smarttrace.spring.jdbc.SpringDbConfig;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@Import(SpringDbConfig.class)
@ComponentScan(basePackageClasses = {
        DeviceCommandDao.class})
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
