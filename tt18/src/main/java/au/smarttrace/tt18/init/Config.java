/**
 *
 */
package au.smarttrace.tt18.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import au.smarttrace.tt18.server.Tt18Server;
import au.smarttrace.tt18.st.service.SmartTraceRawMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {Tt18Server.class, SmartTraceRawMessageHandler.class})
@PropertySource("classpath:/app.properties")
@EnableScheduling
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
