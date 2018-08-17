/**
 *
 */
package com.visfresh.init.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;

import au.smarttrace.spring.jdbc.SpringDbConfig;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Import(SpringDbConfig.class)
@ComponentScan(basePackageClasses = {JdbcConfig.class, DaoImplBase.class})
public class JdbcConfig {

    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
