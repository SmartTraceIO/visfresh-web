/**
 *
 */
package au.smarttrace.cfg.prod;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import au.smarttrace.bt04.Bt04Service;
import au.smarttrace.cfg.JdbcConfig;
import au.smarttrace.email.EmailSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(JdbcConfig.class)
@ComponentScan(basePackageClasses = {
        EmailSender.class,
        Bt04Service.class})
@EnableScheduling
@PropertySource("classpath:/application.properties")
public class Config {
    /**
     *
     * Default constructor.
     */
    public Config() {
        super();
    }
}
