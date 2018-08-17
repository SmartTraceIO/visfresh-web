/**
 *
 */
package au.smarttrace.tt18.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import au.smarttrace.spring.jdbc.SpringDbConfig;
import au.smarttrace.tt18.st.db.MessageDao;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfig.class)
@ComponentScan(basePackageClasses = {MessageDao.class})
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
