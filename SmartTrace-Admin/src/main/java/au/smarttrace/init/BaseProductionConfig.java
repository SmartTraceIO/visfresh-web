/**
 *
 */
package au.smarttrace.init;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import au.smarttrace.ctrl.GlobalDefaultExceptionHandler;
import au.smarttrace.dao.AbstractDao;
import au.smarttrace.svc.AccessServiceImpl;

/**
 * Production config without properties and scheduling.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {
    GlobalDefaultExceptionHandler.class,
    AccessServiceImpl.class,
    AbstractDao.class
})
@Import({JdbcConfig.class, SecurityConfig.class, WebConfig.class})
public class BaseProductionConfig {
    /**
     * Default constructor.
     */
    public BaseProductionConfig() {
        super();
    }
}
