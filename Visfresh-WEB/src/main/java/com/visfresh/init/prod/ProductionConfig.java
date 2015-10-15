/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.visfresh.controllers.RestServiceController;
import com.visfresh.mpl.services.RestServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@ComponentScan(basePackageClasses = {RestServiceController.class,
        RestServiceImpl.class
// ,MockRestService.class //this package should be replaced to reverence
// production classes instead of mock
})
@Configuration
public class ProductionConfig {
    /**
     * Default constructor.
     */
    public ProductionConfig() {
        super();
    }
}
