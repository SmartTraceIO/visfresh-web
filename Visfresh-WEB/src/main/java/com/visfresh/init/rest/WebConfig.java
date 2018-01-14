/**
 *
 */
package com.visfresh.init.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.visfresh.controllers.AbstractController;
import com.visfresh.controllers.audit.AuditInterceptor;
import com.visfresh.controllers.io.JsonMessageConverter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import(WebSecurityConfig.class)
@ComponentScan(basePackageClasses = {AbstractController.class})
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private ApplicationContext context;

    /**
     * Default constructor.
     */
    public WebConfig() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureMessageConverters(java.util.List)
     */
    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new JsonMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
    }
    /* (non-Javadoc)
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(context.getBean(AuditInterceptor.class));
    }
}
