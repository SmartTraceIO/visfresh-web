/**
 *
 */
package au.smarttrace.tt18.junit.db;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.tt18.init.JdbcConfig;
import au.smarttrace.tt18.st.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {
    MessageDao.class
})
@PropertySource("classpath:/junit.app.properties")
public class DaoTestRunnerConfig {
}
