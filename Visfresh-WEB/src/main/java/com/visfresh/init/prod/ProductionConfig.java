/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.controllers.RestServiceController;
import com.visfresh.init.jdbc.JdbcConfig;
import com.visfresh.mpl.services.RestServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {RestServiceController.class,
        RestServiceImpl.class, JdbcConfig.class
// ,MockRestService.class //this package should be replaced to reverence
// production classes instead of mock
})
@PropertySource("classpath:/app.properties")
public class ProductionConfig {
    /**
     * Default constructor.
     */
    public ProductionConfig() {
        super();
    }
}
