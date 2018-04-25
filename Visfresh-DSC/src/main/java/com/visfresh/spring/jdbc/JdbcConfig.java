/**
 *
 */
package com.visfresh.spring.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.db.SystemMessageDao;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {
        SystemMessageDao.class})
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
