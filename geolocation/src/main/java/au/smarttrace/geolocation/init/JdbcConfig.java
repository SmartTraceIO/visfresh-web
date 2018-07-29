/**
 *
 */
package au.smarttrace.geolocation.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import au.smarttrace.geolocation.init.db.NamedParameterJdbcTemplateImpl;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {NamedParameterJdbcTemplateImpl.class})
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
