/**
 *
 */
package com.visfresh.spring.mock;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.mail.mock.MockEmailSender;
import com.visfresh.service.DeviceMessageService;
import com.visfresh.spring.jdbc.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {
    MockEmailSender.class,
    DeviceMessageService.class})
@PropertySource("classpath:/application-junit.properties")
public class JUnitConfig {
    /**
     * Default constructor.
     */
    public JUnitConfig() {
        super();
    }

    /**
     * @return application context.
     */
    public static AnnotationConfigApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan(JUnitConfig.class.getPackage().getName());
        ctx.refresh();
        return ctx;
    }
}
