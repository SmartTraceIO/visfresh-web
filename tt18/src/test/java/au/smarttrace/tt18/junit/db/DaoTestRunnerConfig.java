/**
 *
 */
package au.smarttrace.tt18.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.spring.jdbc.SpringDbConfigJUnit;
import au.smarttrace.tt18.st.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfigJUnit.class)
@ComponentScan(basePackageClasses = {
    MessageDao.class
})
@PropertySource("classpath:/junit.app.properties")
public class DaoTestRunnerConfig {
}
