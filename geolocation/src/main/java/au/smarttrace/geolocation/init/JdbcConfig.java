/**
 *
 */
package au.smarttrace.geolocation.init;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import au.smarttrace.spring.jdbc.SpringDbConfig;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfig.class)
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
