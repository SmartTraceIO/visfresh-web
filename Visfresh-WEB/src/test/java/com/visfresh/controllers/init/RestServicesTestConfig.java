/**
 *
 */
package com.visfresh.controllers.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.visfresh.controllers.AbstractController;
import com.visfresh.junit.db.JUnitDbConfig;
import com.visfresh.mock.MockOpenJtsFacade;
import com.visfresh.services.RuleEngine;

/**
 * This configuration is targeted to mock package and is used only on
 * first steps of application implementation. I next steps when the production
 * rest service will implemented, this package should be moved to unit tests.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@ComponentScan(basePackageClasses = {
        AbstractController.class,//controllers package
        RuleEngine.class, //services package
        JUnitDbConfig.class, //JUnit DB DAO configuration
        MockOpenJtsFacade.class //OpenJts facade implementation.
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
