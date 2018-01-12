/**
 *
 */
package com.visfresh.controllers.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;

import com.visfresh.controllers.AbstractRestServiceTest;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DbTestRunner;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.mock.MockAuditSaver;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockRestSessionManager;
import com.visfresh.services.AuthService;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceRunner extends BlockJUnit4ClassRunner {
    public static final String SHARED_COMPANY_NAME = "Special JUnit Company";
    private static final String CONTEXT_PATCH = "/web";

    /**
     * WEB application context.
     */
    private static Server server;
    protected static URL url;
    private static AbstractApplicationContext context = startServer();
    /**
     * @param klass testing class.
     */
    public RestServiceRunner(final Class<?> klass) throws InitializationError {
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

            url = new URL("http://localhost:" + port + CONTEXT_PATCH + "/vf");
            return context;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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
        //create test webapp context
        final File f = new File(System.getProperty("user.home") + File.separatorChar + ".junitwebapp/WEB-INF");
        if (!f.exists()) {
            f.mkdirs();
        }

        //create test web.xml
        final String originWebXml = RestServiceRunner.class.getClassLoader().getResource(".").toString()
                + "../../src/main/webapp/WEB-INF/web.xml";
        String webXml;
        try {
            webXml = StringUtils.getContent(new URL(originWebXml), "UTF-8");
            webXml = webXml.replace("com.visfresh.init.prod", RestServicesTestConfig.class.getPackage().getName());
            writeTo(webXml, new File(f, "web.xml"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final WebAppContext webapp = new WebAppContext(f.getParent(), CONTEXT_PATCH);
        webapp.setParentLoaderPriority(true);
        return webapp;
    }
    /**
     * @param webXml web.xml file content.
     * @param file target file
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private static void writeTo(final String webXml, final File file) throws IOException {
        final Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            out.write(webXml);
            out.flush();
        } finally {
            out.close();
        }
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
//        final Object test = super.createTest();
//        final AutowireCapableBeanFactory f = context.getAutowireCapableBeanFactory();
//        f.autowireBean(test);
//        return test;
        final Object test = super.createTest();
        if (test instanceof AbstractRestServiceTest) {
            final AbstractRestServiceTest rt = (AbstractRestServiceTest) test;
            rt.setContext(context);
            rt.setServiceUrl(url);
        }
        return test;
    }
    /**
     * Clears the DB and Mock services.
     */
    private void cleanUp() {
        DbTestRunner.clearDb(context);
        context.getBean(MockEmailService.class).clear();
        context.getBean(MockAuditSaver.class).clear();
        context.getBean(MockRestSessionManager.class).clear();
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
