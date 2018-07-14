/**
 *
 */
package au.smarttrace.eel.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.GregorianCalendar;

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
import au.smarttrace.eel.rawdata.ImmediatelyResponder;
import au.smarttrace.eel.rawdata.MessageParser;
import au.smarttrace.eel.rawdata.MessageWriter;
import au.smarttrace.eel.rawdata.RawMessageHandler;

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
    private static final int DEFAULT_THREAD_POOL_SIZE = 100;
    public static final long MIN_ALLOWED_TIME = new GregorianCalendar(2018, 0, 1).getTimeInMillis();

    private static final Logger log = LoggerFactory.getLogger(EelService.class);

    private DatagramSocket server;

    private final int port;
    private final int maxTreadCount;
    private int numThreads;
    private volatile boolean isStopped;
    private volatile Object lock = new Object();

    @Autowired
    private RawMessageHandler handler;
    @Autowired
    private ImmediatelyResponder immediatelyResponder;

    private final MessageParser parser = new MessageParser();
    private final MessageWriter writer = new MessageWriter();

    /**
     * Default constructor.
     */
    @Autowired
    public EelService(final Environment env) {
        this(
            env.getProperty("eel.port", Integer.class, 3434),
            env.getProperty("eel.udp.maxthreads", Integer.class, DEFAULT_THREAD_POOL_SIZE)
        );
    }
    /**
     * @param port TCP port for listen.
     */
    public EelService(final int port) {
        this(port, DEFAULT_THREAD_POOL_SIZE);
    }
    /**
     * @param port
     * @param socketTimeOut
     * @param maxTreadCount
     */
    protected EelService(final int port, final int maxTreadCount) {
        super();
        this.port = port;
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
        }

        //process message asynchronously.

    }
    /**
     * @param s socket.
     * @throws IOException
     */
    private void processConnection(final DatagramPacket s) throws IOException {
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
            server.close();
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
