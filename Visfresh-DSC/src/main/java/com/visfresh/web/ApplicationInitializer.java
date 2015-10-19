/**
 *
 */
package com.visfresh.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.opengts.db.tables.Account;
import org.opengts.dbtools.DBAdmin;
import org.opengts.dbtools.DBException;
import org.opengts.dbtools.DBFactory;
import org.opengts.dbtools.DBRecord;
import org.opengts.util.OrderedMap;
import org.opengts.war.tools.RTConfigContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.visfresh.config.DeviceConstants;
import com.visfresh.dispatcher.DeviceMessageDispatcher;
import com.visfresh.dispatcher.ResolvedMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ApplicationInitializer extends RTConfigContextListener {
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
        super.contextInitialized(sce);

        //initialize spring config
        final String springConfig = "application-context.xml";
        final ConfigurableApplicationContext ctx
            = new ClassPathXmlApplicationContext(springConfig);
        sce.getServletContext().setAttribute(BEAN_FACTORY_PROPERTY, ctx);

        //initialize account
        final DeviceConstants constants = ctx.getBean(DeviceConstants.class);
        try {
            createDbIfNeed();

            Account acc = Account.getAccount(constants.getAccountId());
            if (acc == null) {
                acc = Account.createNewAccount(null, constants.getAccountId(), null);
                log.debug("New OpenGTS account has created " + constants.getAccountId());
            }
        } catch (final DBException e) {
            throw new RuntimeException("Failed to create OpenGTS account", e);
        }

        //initialize dispatcher
        ctx.getBean(DeviceMessageDispatcher.class).start();
        ctx.getBean(ResolvedMessageDispatcher.class).start();

        log.debug("Application has initialized. Spring config: " + springConfig);
    }

    /**
     * @throws DBException
     *
     */
    private void createDbIfNeed() throws DBException {
        @SuppressWarnings("rawtypes")
        final OrderedMap<String, DBFactory<? extends DBRecord>> factories = DBAdmin.getTableFactoryMap();

        final String key = factories.getFirstKey();
        if (!factories.get(key).tableExists()) {
            final int size = factories.size();
            for (int i = 0; i < size; i++) {
                factories.get(i).createTable();
            }
        }
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

        super.contextDestroyed(sce);
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
