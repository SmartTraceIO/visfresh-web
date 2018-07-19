/**
 *
 */
package au.smarttrace.eel.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.MessageParser;
import au.smarttrace.eel.rawdata.MessageWriter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EelService {
    /**
     *
     */
    private static final int MAX_PACKET_SIZE = 1200;
    public static final long MIN_ALLOWED_TIME = new GregorianCalendar(2018, 0, 1).getTimeInMillis();

    private static final Logger log = LoggerFactory.getLogger(EelService.class);

    private ThreadFactory threadFactory = new ThreadFactory() {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "eel-msg-thread-";

        {
            final SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(false);
            t.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
            return t;
        }
    };

    private DatagramSocket server;

    private final int port;
    private volatile boolean isStopped;
    private volatile Object lock = new Object();

    @Autowired
    private EelMessageHandler handler;
    @Autowired
    private ImmediatelyResponder immediatelyResponder;

    private final MessageParser parser = new MessageParser();
    private final MessageWriter writer = new MessageWriter();
    private ExecutorService executor;

    /**
     * Default constructor.
     */
    @Autowired
    public EelService(final Environment env) {
        this(
            env.getProperty("eel.port", Integer.class, 3434)
        );
    }
    /**
     * @param port
     * @param socketTimeOut
     * @param maxTreadCount
     */
    protected EelService(final int port) {
        super();
        this.port = port;
    }


    @PostConstruct
    public void start() {
        //create thread pool.
        this.executor = Executors.newCachedThreadPool(threadFactory);

        //start server
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
                server = new DatagramSocket(port);

                while (true) {
                    synchronized (lock) {
                        if (isStopped) {
                            break;
                        }
                    }

                    final byte[] buff = new byte[MAX_PACKET_SIZE];
                    final DatagramPacket pckt = new DatagramPacket(buff, buff.length);
                    server.receive(pckt);
                    if (server == null) {
                        break;
                    }

                    processData(pckt.getData());
                }
            } catch (final Exception e) {
                if (!isStopped) {
                    log.error("UDP server crushed", e);
                }
            }
        }
    }

    /**
     * @param dataGramPacketBuffer
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    private void processData(final byte[] dataGramPacketBuffer)
            throws IOException, IncorrectPacketLengthException {
        final byte[] data = parser.readMessageData(new ByteArrayInputStream(dataGramPacketBuffer));
        log.debug("Message has recieved: " + Hex.encodeHexString(data));

        final EelMessage msg = parser.parseMessage(data);

        //need respond immediately.
        if (immediatelyResponder != null) {
            final EelMessage response = immediatelyResponder.respond(msg);
            final byte[] buff = writer.writeMessage(response);
            final DatagramPacket pckt = new DatagramPacket(buff, buff.length, server.getRemoteSocketAddress());
            server.send(pckt);
        } else {
            log.warn("Immediately responder is not set");
        }

        //process message asynchronously.
        if (handler != null) {
            executor.execute(() -> handler.handleMessage(msg));
        } else {
            log.warn("Message handler is not set");
        }
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            synchronized (lock) {
                isStopped = true;
                lock.notifyAll();
            }
            server.close();
            server = null;

            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                log.error("Wating of shoot down thread pool is failed", e);
            }
        }
    }
    /**
     * @param handler the handler to set
     */
    public void setHandler(final EelMessageHandler handler) {
        this.handler = handler;
    }
    /**
     * @return the handler
     */
    public EelMessageHandler getHandler() {
        return handler;
    }
    /**
     * @param resp the immediatelyResponder to set
     */
    public void setImmediatelyResponder(final ImmediatelyResponder resp) {
        this.immediatelyResponder = resp;
    }
    /**
     * @return the immediatelyResponder
     */
    public ImmediatelyResponder getImmediatelyResponder() {
        return immediatelyResponder;
    }
}
