/**
 *
 */
package com.visfresh.init.prod;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.visfresh.init.BackendConfig;
import com.visfresh.init.rest.WebConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import({WebConfig.class, BackendConfig.class})
@EnableScheduling
@PropertySource({"classpath:/app.common.properties", "classpath:/app.properties"})
public class ProductionConfig {
    /**
     * Default constructor.
     */
    public ProductionConfig() {
        super();
    }
}
