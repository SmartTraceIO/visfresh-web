/**
 *
 */
package au.smarttrace.eel.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.rawdata.RawMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EelService {
    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    private static final int DEFAULT_TCP_TIMEOUT = 10000;
    public static final long MIN_ALLOWED_TIME = new GregorianCalendar(2018, 0, 1).getTimeInMillis();

    private static final Logger log = LoggerFactory.getLogger(EelService.class);

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
    public EelService(final Environment env) {
        this(
            env.getProperty("eel.port", Integer.class, 3434),
            env.getProperty("eel.tcp.timeout", Integer.class, DEFAULT_TCP_TIMEOUT),
            env.getProperty("eel.tcp.maxthreads", Integer.class, DEFAULT_THREAD_POOL_SIZE)
        );
    }
    /**
     * @param port TCP port for listen.
     */
    public EelService(final int port) {
        this(port, DEFAULT_TCP_TIMEOUT, DEFAULT_THREAD_POOL_SIZE);
    }
    /**
     * @param port
     * @param socketTimeOut
     * @param maxTreadCount
     */
    protected EelService(final int port, final int socketTimeOut, final int maxTreadCount) {
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
                    synchronized (lock) {
                        if (isStopped) {
                            break;
                        }
                    }
                    processConnection(server.accept());
                }
            } catch (final Exception e) {
                if (!isStopped) {
                    log.error("Server socket crushed", e);
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
        final Thread t = new Thread(new EelSession(s, handler), "TT-18 Conneection thread"){
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
            try {
                server.close();
            } catch (final IOException e) {
                e.printStackTrace();
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
