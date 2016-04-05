/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import com.visfresh.controllers.init.RestServicesTestConfig;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DaoTestRunner;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceRunner extends BlockJUnit4ClassRunner {
    public static final String SHARED_COMPANY_NAME = "Special JUnit Company";

    /**
     * WEB application context.
     */
    protected static AbstractApplicationContext context;
    private static Server server;
    protected static URL url;

    static {
        final int port = getFreePort();
        //search free port
        try {
            final ServletHandler handler = getServletHandler();
            server = createServer(handler, port);
            server.start();

            //get WEB application context.
            context = (AbstractApplicationContext) ((FrameworkServlet) handler.getServlet("SpringDispatcher").getServlet())
                    .getWebApplicationContext();

            url = new URL("http://localhost:" + port + "/web/vf");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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
        contextHandler.setSessionHandler(new SessionHandler());

        final QueuedThreadPool pool = new QueuedThreadPool();
        pool.setDaemon(true); // it allows the server to be stopped after tests finished
        final Server server = new Server(pool);

        final ServerConnector connector = new ServerConnector(server);
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
        holder.setInitParameter("contextConfigLocation", RestServicesTestConfig.class.getPackage().getName());

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
            final Company c = new Company();
            c.setName(SHARED_COMPANY_NAME);
            c.setDescription("JUnit company");
            context.getBean(CompanyDao.class).save(c);

            //create user
            final AuthService service = context.getBean(AuthService.class);
            final User user = new User();
            user.setEmail("a@b.c");
            user.setFirstName("Yury");
            user.setLastName("Gagarin");
            user.setCompany(c);
            final Set<Role> roles = new HashSet<Role>();
            roles.add(Role.Admin);
            user.setRoles(roles);

            service.saveUser(user, "", false);

            try {
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
            rt.setServiceUrl(url);
        }
        return test;
    }
    /**
     *
     */
    private void cleanUp() {
        DaoTestRunner.clearDb(context);
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
