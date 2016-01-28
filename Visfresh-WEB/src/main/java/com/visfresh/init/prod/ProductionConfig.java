/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.controllers.AbstractController;
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.drools.DroolsRuleEngine;
import com.visfresh.init.jdbc.JdbcConfig;
import com.visfresh.mpl.services.TrackerMessageDispatcher;
import com.visfresh.services.DefaultAuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {
        AbstractController.class,
        DroolsRuleEngine.class,
        JdbcConfig.class,
        DefaultAuthService.class,
        DaoImplBase.class,
        TrackerMessageDispatcher.class})
@PropertySource("classpath:/app.properties")
public class ProductionConfig {
    /**
     * Default constructor.
     */
    public ProductionConfig() {
        super();
    }
}
