/**
 *
 */
package au.smarttrace.cfg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import au.smarttrace.db.BeaconDao;
import au.smarttrace.geolocation.impl.dao.SystemMessageDao;
import au.smarttrace.spring.jdbc.SpringDbConfig;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@Import(SpringDbConfig.class)
@ComponentScan(basePackageClasses = {
        BeaconDao.class, SystemMessageDao.class})
public class JdbcConfig {

    /**
     * Default constructor.
     */
    public JdbcConfig() {
        super();
    }
}
