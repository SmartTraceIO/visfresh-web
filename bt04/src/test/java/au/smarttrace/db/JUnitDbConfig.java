/**
 *
 */
package au.smarttrace.db;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.cfg.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@PropertySource("classpath:/application-junit.properties")
public class JUnitDbConfig {
    /**
     * Default constructor.
     */
    public JUnitDbConfig() {
        super();
    }

    /**
     * @return application context.
     */
    public static AnnotationConfigApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan(JUnitDbConfig.class.getPackage().getName());
        ctx.refresh();
        return ctx;
    }
}
