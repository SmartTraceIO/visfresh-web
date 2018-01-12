/**
 *
 */
package com.visfresh.init.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.visfresh.controllers.AbstractController;
import com.visfresh.controllers.lite.LiteShipmentController;
import com.visfresh.websecurity.AuthTokenAuthenticationFilter;
import com.visfresh.websecurity.JdbcAuthenticationManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan(basePackageClasses = {
    AbstractController.class,
    LiteShipmentController.class,
    JdbcAuthenticationManager.class})
public class RestConfig extends WebSecurityConfigurerAdapter {
    /**
     * Default constructor.
     */
    public RestConfig() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManager()
     */
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return getApplicationContext().getBean(JdbcAuthenticationManager.class);
    }
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().disable();
        http.jee().disable();
        http.formLogin().disable();
        http.httpBasic().disable();
        http.x509().disable();
        http.requestCache().disable();
        http.sessionManagement().disable();
        http.addFilterBefore(
            getApplicationContext().getBean(AuthTokenAuthenticationFilter.class),
            BasicAuthenticationFilter.class);
    }
}
