/**
 *
 */
package com.visfresh.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.visfresh.controllers.svcimpl.DefaultAuthService;
import com.visfresh.impl.ruleengine.VisfreshRuleEngine;
import com.visfresh.impl.services.TrackerMessageDispatcher;
import com.visfresh.init.instance.InstanceConfig;
import com.visfresh.init.jdbc.JdbcConfig;
import com.visfresh.l12n.XmlResourceBundle;
import com.visfresh.reports.PdfReportBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import({InstanceConfig.class, JdbcConfig.class})
@ComponentScan(basePackageClasses = {
    VisfreshRuleEngine.class,
    DefaultAuthService.class,
    XmlResourceBundle.class,
    PdfReportBuilder.class,
    TrackerMessageDispatcher.class})
public class BackendConfig {
}
