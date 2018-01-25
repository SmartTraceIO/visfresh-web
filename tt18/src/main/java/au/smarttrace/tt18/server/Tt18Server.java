/**
 *
 */
package au.smarttrace.tt18.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import au.smarttrace.tt18.RawMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class Tt18Server {
    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    private static final int DEFAULT_TCP_TIMEOUT = 10000;

    private static final Logger log = LoggerFactory.getLogger(Tt18Server.class);

    private ServerSocket server;
    private final int port;
    private final int socketTimeOut;
    private final int maxTreadCount;
    private int numThreads;
    private volatile boolean isStopped;
    private volatile Object lock = new Object();

    @Autowired
    private RawMessageHandler handler;

    /**
     * Default constructor.
     */
    @Autowired
    public Tt18Server(final Environment env) {
        this(
            env.getProperty("tt18.port", Integer.class, 3232),
            env.getProperty("tt18.tcp.timeout", Integer.class, DEFAULT_TCP_TIMEOUT),
            env.getProperty("tt18.tcp.maxthreads", Integer.class, DEFAULT_THREAD_POOL_SIZE)
        );
    }
    /**
     * @param port TCP port for listen.
     */
    public Tt18Server(final int port) {
        this(port, DEFAULT_TCP_TIMEOUT, DEFAULT_THREAD_POOL_SIZE);
    }
    /**
     * @param port
     * @param socketTimeOut
     * @param maxTreadCount
     */
    protected Tt18Server(final int port, final int socketTimeOut, final int maxTreadCount) {
        super();
        this.port = port;
        this.socketTimeOut = socketTimeOut;
        this.maxTreadCount = maxTreadCount;
    }


    @PostConstruct
    public void start() {
        final Thread t = new Thread("TT18 Server") {
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                runServer();
                log.debug("TT-18 server has stopped");
            }
        };

        t.start();
    }

    protected void runServer() {
        log.debug("TT-18 Server has started");

        while (true) {
            synchronized (lock) {
                if (isStopped) {
                    return;
                }
            }

            try {
                server = new ServerSocket(port);
                while (true) {
                    processConnection(server.accept());
                }
            } catch (final Exception e) {
                if (!isStopped) {
                    log.error("Server socket crushed", e);
                }
            } finally {
                try {
                    server.close();
                } catch (final IOException e) {
                    log.error("Failed to close server socket", e);
                }
            }
        }
    }

    /**
     * @param s socket.
     * @throws IOException
     */
    private void processConnection(final Socket s) throws IOException {
        synchronized (lock) {
            while (numThreads >= maxTreadCount) {
                //wait of num threads
                try {
                    lock.wait();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                if (isStopped) {
                    return;
                }
            }

            numThreads++;
        }

        s.setSoTimeout(socketTimeOut);
        final Thread t = new Thread(new Tt18Session(s, handler), "TT-18 Conneection thread"){
            /* (non-Javadoc)
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                try {
                    super.run();
                } finally {
                    synchronized (lock) {
                        numThreads--;
                    }
                    try {
                        s.close();
                    } catch (final IOException e) {
                        log.error("Socket closing error", e);
                    }
                }
            }
        };

        t.start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            synchronized (lock) {
                isStopped = true;
                lock.notifyAll();
            }
        }
    }
    /**
     * @param handler the handler to set
     */
    public void setHandler(final RawMessageHandler handler) {
        this.handler = handler;
    }
    /**
     * @return the handler
     */
    public RawMessageHandler getHandler() {
        return handler;
    }
}
