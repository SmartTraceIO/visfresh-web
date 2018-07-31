/**
 *
 */
package au.smarttrace.geolocation.init.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import au.smarttrace.geolocation.impl.GeoLocationDispatcherImpl;
import au.smarttrace.geolocation.init.JdbcConfig;
import au.smarttrace.unwiredlabs.UnwiredLabsInit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import({JdbcConfig.class, UnwiredLabsInit.class})
@ComponentScan(basePackageClasses = {
        GeoLocationDispatcherImpl.class})
@EnableScheduling
@PropertySource({"classpath:/geo.app.properties"})
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
