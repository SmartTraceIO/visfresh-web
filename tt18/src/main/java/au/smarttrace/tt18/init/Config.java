/**
 *
 */
package au.smarttrace.tt18.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {})
@PropertySource("classpath:/app.properties")
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
