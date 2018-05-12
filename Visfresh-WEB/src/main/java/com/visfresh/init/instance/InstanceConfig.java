/**
 *
 */
package com.visfresh.init.instance;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@ComponentScan(basePackageClasses = {InstanceConfig.class})
public class InstanceConfig {
}
