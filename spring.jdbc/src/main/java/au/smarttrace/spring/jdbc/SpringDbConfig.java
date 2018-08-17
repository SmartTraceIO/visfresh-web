/**
 *
 */
package au.smarttrace.spring.jdbc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.spring.jdbc.impl.NamedParameterJdbcTemplateImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {NamedParameterJdbcTemplateImpl.class})
@PropertySource("classpath:/st.db.properties")
public class SpringDbConfig {

    /**
     * Default constructor.
     */
    public SpringDbConfig() {
        super();
    }
}
