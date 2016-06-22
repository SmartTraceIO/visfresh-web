/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.controllers.AbstractController;
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.jdbc.JdbcConfig;
import com.visfresh.l12n.XmlResourceBundle;
import com.visfresh.mpl.ruleengine.VisfreshRuleEngine;
import com.visfresh.mpl.services.TrackerMessageDispatcher;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.services.DefaultAuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {
        AbstractController.class,
        VisfreshRuleEngine.class,
        JdbcConfig.class,
        DefaultAuthService.class,
        DaoImplBase.class,
        XmlResourceBundle.class,
        PdfReportBuilder.class,
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
