/**
 *
 */
package com.visfresh.controllers.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.visfresh.init.rest.WebConfig;
import com.visfresh.junit.db.JUnitDbConfig;
import com.visfresh.l12n.XmlResourceBundle;
import com.visfresh.mock.MockSystemMessageDispatcher;
import com.visfresh.services.RuleEngine;

/**
 * This configuration is targeted to mock package and is used only on
 * first steps of application implementation. I next steps when the production
 * rest service will implemented, this package should be moved to unit tests.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Import({JUnitDbConfig.class, //JUnit DB DAO configuration,
    WebConfig.class})
@ComponentScan(basePackageClasses = {
        RuleEngine.class, //services package
        XmlResourceBundle.class, //resource bundles
        MockSystemMessageDispatcher.class // mock services
        })
@Configuration
public class RestServicesTestConfig {
    /**
     * Default constructor.
     */
    public RestServicesTestConfig() {
        super();
    }
}
