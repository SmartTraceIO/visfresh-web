/**
 *
 */
package au.smarttrace.ctrl.runner;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import au.smarttrace.init.BaseProductionConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(BaseProductionConfig.class)
@PropertySource("classpath:/junit.app.properties")
public class ControllerTestRunnerConfig {
}
