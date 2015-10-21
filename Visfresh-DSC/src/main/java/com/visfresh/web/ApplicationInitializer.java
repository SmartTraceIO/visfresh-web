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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.visfresh.dispatcher.DeviceMessageDispatcher;
import com.visfresh.dispatcher.ResolvedMessageDispatcher;

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
        final String springConfig = "application-context.xml";
        final ConfigurableApplicationContext ctx
            = new ClassPathXmlApplicationContext(springConfig);
        sce.getServletContext().setAttribute(BEAN_FACTORY_PROPERTY, ctx);

        //initialize dispatcher
        ctx.getBean(DeviceMessageDispatcher.class).start();
        ctx.getBean(ResolvedMessageDispatcher.class).start();

        log.debug("Application has initialized. Spring config: " + springConfig);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
        final ConfigurableApplicationContext ctx = getBeanContext(servletContext);

        ctx.getBean(DeviceMessageDispatcher.class).stop();
        ctx.getBean(ResolvedMessageDispatcher.class).stop();

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
