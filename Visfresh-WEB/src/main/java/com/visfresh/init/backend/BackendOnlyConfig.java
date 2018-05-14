/**
 *
 */
package com.visfresh.init.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.visfresh.init.BackendConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(BackendConfig.class)
@EnableScheduling
@PropertySource("classpath:/app.properties")
public class BackendOnlyConfig {
    /**
     * Default constructor.
     */
    public BackendOnlyConfig() {
        super();
    }
}
