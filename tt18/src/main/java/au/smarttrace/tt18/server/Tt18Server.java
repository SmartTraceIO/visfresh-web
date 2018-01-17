/**
 *
 */
package au.smarttrace.tt18.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class Tt18Server {
    private static final Logger log = LoggerFactory.getLogger(Tt18Server.class);
    private ServerSocket server;
    private int port;
    private int socketTimeOut;
    private int threadPoolSize;
    private AtomicBoolean isStopped = new AtomicBoolean(false);
    private ThreadPoolExecutor threadPool;

    /**
     * Default constructor.
     */
    public Tt18Server(final Environment env) {
        super();
        port = env.getProperty("tt18.port", Integer.class, 3232);
        socketTimeOut = env.getProperty("tt18.tcp.timeout", Integer.class, 10000);
        threadPoolSize = env.getProperty("tt18.tcp.timeout", Integer.class, 100);
    }

    @PostConstruct
    public void start() {
        threadPool = new ThreadPoolExecutor(3, threadPoolSize, 3000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        threadPool.prestartAllCoreThreads();

        final Thread t = new Thread("TT18 Server") {
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                startSerrverThread();
            }
        };
        t.start();
    }

    protected void startSerrverThread() {
        while (!isStopped.get()) {
            try {
                server = new ServerSocket(port);
                while (true) {
                    processConnection(server.accept());
                }
            } catch (final Exception e) {
                if (!isStopped.get()) {
                    log.error("Server socket crushed", e);
                }
                if (server != null) {
                    try {
                        server.close();
                    } catch (final IOException e1) {}
                }
            }

            server = null;
        }
    }

    /**
     * @param s socket.
     * @throws IOException
     */
    private void processConnection(final Socket s) throws IOException {
        s.setSoTimeout(this.socketTimeOut);
        threadPool.execute(new T18Session(s));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            try {
                isStopped.set(true);
                server.close();
            } catch (final IOException e) {
                log.error("Failed to close server socket", e);
            }
        }
    }
}
