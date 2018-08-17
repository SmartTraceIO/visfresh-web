/**
 *
 */
package au.smarttrace.geolocation.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.spring.jdbc.SpringDbConfigJUnit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfigJUnit.class)
@ComponentScan(basePackageClasses = {
    RetryableEventDao.class
})
@PropertySource("classpath:/junit.app.properties")
public class DaoTestRunnerConfig {
}
