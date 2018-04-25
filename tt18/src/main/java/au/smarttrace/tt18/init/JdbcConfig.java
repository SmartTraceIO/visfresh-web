/**
 *
 */
package au.smarttrace.tt18.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import au.smarttrace.tt18.db.NamedParameterJdbcTemplateImpl;

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
