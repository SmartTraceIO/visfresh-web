/**
 *
 */
package com.visfresh.tools;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.visfresh.init.jdbc.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses={JdbcConfig.class})
@PropertySource("classpath:/app.properties")
public class SpringConfig {
    /**
     * Default constructor.
     */
    public SpringConfig() {
        super();
    }
}
