/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import com.visfresh.dao.mock.MockNotificationScheduleDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.init.mock.MockConfig;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.mock.MockAuthService;
import com.visfresh.mock.MockRestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceRunner extends BlockJUnit4ClassRunner {
    /**
     * WEB application context.
     */
    protected static WebApplicationContext context;
    protected static RestServiceFacade facade;
    private static Server server;

    static {
        final int port = getFreePort();
        //search free port
        try {
            final ServletHandler handler = getServletHandler();
            server = createServer(handler, port);
            server.start();

            //get WEB application context.
            context = ((FrameworkServlet) handler.getServlet("SpringDispatcher").getServlet())
                    .getWebApplicationContext();

            facade = new RestServiceFacade();
            facade.setServiceUrl(new URL("http://localhost:" + port + "/web/vf"));
            facade.setReferenceResolver(new ReferenceResolver() {
                private Company getCompany() {
                    return getRestService().getCompanies().get(0);
                }
                @Override
                public Shipment getShipment(final Long id) {
                    return getRestService().getShipment(getCompany(), id);
                }
                @Override
                public NotificationSchedule getNotificationSchedule(final Long id) {
                    return getNotificationScheduleDao().findOne(id);
                }
                @Override
                public LocationProfile getLocationProfile(final Long id) {
                    return getRestService().getLocationProfile(getCompany(), id);
                }
                @Override
                public Device getDevice(final String id) {
                    return getRestService().getDevice(getCompany(), id);
                }
                @Override
                public Company getCompany(final Long id) {
                    return getRestService().getCompany(id);
                }
                @Override
                public AlertProfile getAlertProfile(final Long id) {
                    return getRestService().getAlertProfile(getCompany(), id);
                }
            });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    protected static MockRestService getRestService() {
        return context.getBean(MockRestService.class);
    }
    /**
     * @param klass testing class.
     */
    public RestServiceRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }
    /**
     * @param port
     * @return
     */
    private static Server createServer(final ServletHandler handler, final int port) {
        //context handler
        final ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath( "/web" );
        contextHandler.setHandler(handler);

        final QueuedThreadPool pool = new QueuedThreadPool();
        pool.setDaemon(true); // it allows the server to be stopped after tests finished
        final Server server = new Server(pool);

        final ServerConnector connector=new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        server.setHandler(contextHandler);
        return server;
    }

    /**
     * @return
     */
    protected static ServletHandler getServletHandler() {
        //configure servlet
        final ServletHolder holder = new ServletHolder();

        holder.setHeldClass(DispatcherServlet.class);
        holder.setName("SpringDispatcher");
        holder.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
        holder.setInitParameter("contextConfigLocation", MockConfig.class.getPackage().getName());

        //add servlet mapping
        final ServletMapping mapping = new ServletMapping();
        mapping.setPathSpec("/vf/*");
        mapping.setServletName(holder.getName());

        //set handler to servlet.
        final ServletHandler handler = new ServletHandler();
        handler.addServlet(holder);
        handler.addServletMapping(mapping);
        return handler;
    }

    /**
     * @return
     */
    private static int getFreePort() {
        try {
            final int port;
            final ServerSocket so = new ServerSocket(0);
            try {
                port = so.getLocalPort();
            } finally {
                so.close();
            }
            return port;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            //create company
            final Company c = new Company(1l);
            c.setName("JUnit");
            c.setDescription("JUnit company");
            getRestService().companies.put(c.getId(), c);

            //create user
            final MockAuthService service = context.getBean(MockAuthService.class);
            final User user = new User();
            user.setLogin("anylogin");
            user.setCompany(c);
            user.getRoles().add(Role.GlobalAdmin);

            service.createUser(user, "");

            try {
                final String authToken = facade.login(user.getLogin(), "");
                facade.setAuthToken(authToken);

                super.runChild(method, notifier);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } finally {
            cleanUp();
        }
    }
    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        if (test instanceof AbstractRestServiceTest) {
            final AbstractRestServiceTest rt = (AbstractRestServiceTest) test;
            rt.setContext(context);
            rt.setFacade(facade);
        }
        return test;
    }


    /**
     *
     */
    private void cleanUp() {
        getNotificationScheduleDao().clear();
        context.getBean(MockRestService.class).clear();
    }
    /**
     * @return
     */
    protected static MockNotificationScheduleDao getNotificationScheduleDao() {
        return context.getBean(MockNotificationScheduleDao.class);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        if (server != null) {
            server.destroy();
        }
        try {
            super.finalize();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
