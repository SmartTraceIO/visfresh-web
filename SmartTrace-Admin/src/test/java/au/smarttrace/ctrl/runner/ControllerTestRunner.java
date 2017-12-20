/**
 *
 */
package au.smarttrace.ctrl.runner;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;

import au.smarttrace.ctrl.AuthController;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ControllerTestRunner extends BlockJUnit4ClassRunner {
    private static final String CONTEXT_PATCH = "/music";
    private static AbstractApplicationContext context = startServer();

    /**
     * @param klass testing class.
     */
    public ControllerTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * @return
     */
    private static AbstractApplicationContext startServer() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final int port = getFreePort();
        //search free port
        try {
            final WebAppContext handler = getServletHandler();
            final Server server = createServer(handler, port);
            server.start();

            //get WEB application context.
            final AbstractApplicationContext context = (AbstractApplicationContext) ((FrameworkServlet) handler.getServletHandler().getServlet(
                    "SpringDispatcher").getServlet()).getWebApplicationContext();

            context.getBean(ServiceUrlHolder.class).setServiceUrl(
                    "http://localhost:" + port + CONTEXT_PATCH + "/");
            return context;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        final AutowireCapableBeanFactory f = context.getAutowireCapableBeanFactory();
        f.autowireBean(test);
        return test;
    }
    /**
     * @param port
     * @return
     */
    private static Server createServer(final WebAppContext handler, final int port) {
        final QueuedThreadPool pool = new QueuedThreadPool();
        pool.setDaemon(true); // it allows the server to be stopped after tests finished
        final Server server = new Server(pool);

        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        server.setHandler(handler);
        return server;
    }

    /**
     * @return
     */
    protected static WebAppContext getServletHandler() {
        final String rootPath = AuthController.class.getClassLoader().getResource(".").toString();
        final WebAppContext webapp = new WebAppContext(rootPath + "../../src/main/webapp", CONTEXT_PATCH);
        webapp.setParentLoaderPriority(true);
        return webapp;
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
}
