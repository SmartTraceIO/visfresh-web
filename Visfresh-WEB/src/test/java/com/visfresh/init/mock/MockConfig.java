/**
 *
 */
package com.visfresh.init.mock;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.visfresh.controllers.AbstractController;
import com.visfresh.dao.mock.MockDaoConfig;
import com.visfresh.mock.MockRestService;

/**
 * This configuration is targeted to mock package and is used only on
 * first steps of application implementation. I next steps when the production
 * rest service will implemented, this package should be moved to unit tests.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@ComponentScan(basePackageClasses = {AbstractController.class, MockRestService.class, MockDaoConfig.class})
@Configuration
public class MockConfig {
    /**
     * Default constructor.
     */
    public MockConfig() {
        super();
    }
}
