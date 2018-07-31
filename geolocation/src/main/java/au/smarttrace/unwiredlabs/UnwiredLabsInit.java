/**
 *
 */
package au.smarttrace.unwiredlabs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {UnwiredLabsService.class})
@PropertySource({"classpath:/unwiredlabs.properties"})
public class UnwiredLabsInit {
}
