/**
 *
 */
package com.visfresh.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dispatcher.DeviceMessageDispatcher;
import com.visfresh.spring.prod.Config;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ApplicationInitializer implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicationInitializer.class);
    private static final String BEAN_FACTORY_PROPERTY = "SpringBeanFactory";

    /**
     * Default constructor.
     */
    public ApplicationInitializer() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        //initialize spring config
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan(Config.class.getPackage().getName());
        ctx.refresh();
        sce.getServletContext().setAttribute(BEAN_FACTORY_PROPERTY, ctx);

        //initialize dispatcher
        ctx.getBean(DeviceMessageDispatcher.class).start();

        log.debug("Application has initialized.");
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
        final ConfigurableApplicationContext ctx = getBeanContext(servletContext);

        ctx.getBean(DeviceMessageDispatcher.class).stop();

        //destroy spring context
        servletContext.removeAttribute(BEAN_FACTORY_PROPERTY);
        ctx.close();

        log.debug("Application has stopped");
    }
    /**
     * @param servletContext
     * @return
     */
    public static ConfigurableApplicationContext getBeanContext(
            final ServletContext servletContext) {
        final ConfigurableApplicationContext ctxt = (ConfigurableApplicationContext) servletContext
                .getAttribute(BEAN_FACTORY_PROPERTY);
        return ctxt;
    }
}
