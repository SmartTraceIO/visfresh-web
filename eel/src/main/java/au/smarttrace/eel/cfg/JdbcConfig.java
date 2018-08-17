/**
 *
 */
package au.smarttrace.eel.cfg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import au.smarttrace.eel.db.BeaconDao;
import au.smarttrace.spring.jdbc.SpringDbConfig;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfig.class)
@ComponentScan(basePackageClasses = {BeaconDao.class})
public class JdbcConfig {
    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
