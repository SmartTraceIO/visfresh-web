/**
 *
 */
package com.visfresh.spring.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.dispatcher.AbstractDispatcher;
import com.visfresh.mail.EmailSender;
import com.visfresh.service.DeviceMessageService;
import com.visfresh.spring.jdbc.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {
        EmailSender.class,
        AbstractDispatcher.class,
        DeviceMessageService.class})
@PropertySource("classpath:/application.properties")
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
