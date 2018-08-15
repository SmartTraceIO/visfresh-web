/**
 *
 */
package au.smarttrace.cfg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import au.smarttrace.db.BeaconDao;
import au.smarttrace.geolocation.impl.dao.SystemMessageDao;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
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
