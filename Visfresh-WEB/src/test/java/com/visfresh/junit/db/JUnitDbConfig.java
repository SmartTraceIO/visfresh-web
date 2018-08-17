/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.instance.InstanceConfig;
import com.visfresh.l12n.XmlResourceBundle;
import com.visfresh.mock.MockRuleEngine;

import au.smarttrace.spring.jdbc.SpringDbConfigJUnit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Import({SpringDbConfigJUnit.class, InstanceConfig.class})
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {
        XmlResourceBundle.class,
        MockRuleEngine.class,
        DaoImplBase.class})
@PropertySource("classpath:/junit.app.properties")
public class JUnitDbConfig {
    /**
     * Default constructor.
     */
    public JUnitDbConfig() {
        super();
    }
}
