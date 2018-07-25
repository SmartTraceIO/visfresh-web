/**
 *
 */
package au.smarttrace.eel.cfg.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import au.smarttrace.eel.cfg.JdbcConfig;
import au.smarttrace.eel.email.EmailSender;
import au.smarttrace.eel.service.EelService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {
        EmailSender.class,
        EelService.class})
@EnableScheduling
@PropertySource({"classpath:/app.properties"})
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
