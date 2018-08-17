/**
 *
 */
package au.smarttrace.db;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import au.smarttrace.geolocation.impl.dao.SystemMessageDao;
import au.smarttrace.spring.jdbc.SpringDbConfigJUnit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(SpringDbConfigJUnit.class)
@ComponentScan(basePackageClasses = {
        BeaconDao.class, SystemMessageDao.class})
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
