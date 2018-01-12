/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.impl.ruleengine.VisfreshRuleEngine;
import com.visfresh.impl.services.TrackerMessageDispatcher;
import com.visfresh.init.jdbc.JdbcConfig;
import com.visfresh.init.rest.RestConfig;
import com.visfresh.l12n.XmlResourceBundle;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.services.DefaultAuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import({JdbcConfig.class, RestConfig.class})
@ComponentScan(basePackageClasses = {
        VisfreshRuleEngine.class,
        DefaultAuthService.class,
        DaoImplBase.class,
        XmlResourceBundle.class,
        PdfReportBuilder.class,
        TrackerMessageDispatcher.class})
@EnableScheduling
@PropertySource("classpath:/app.properties")
public class ProductionConfig {
    /**
     * Default constructor.
     */
    public ProductionConfig() {
        super();
    }
}
