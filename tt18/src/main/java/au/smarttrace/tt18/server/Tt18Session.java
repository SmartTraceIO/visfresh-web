/**
 *
 */
package au.smarttrace.tt18.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.smarttrace.tt18.MessageParser;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.RawMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tt18Session implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Tt18Session.class);
    private final MessageParser parser = new MessageParser();
    private RawMessageHandler handler;

    /**
     * Socket.
     */
    private Socket socket;

    /**
     * @param so socket.
     */
    public Tt18Session(final Socket so, final RawMessageHandler handler) {
        this.socket = so;
        this.handler = handler;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            processConnection(socket.getInputStream(), socket.getOutputStream());
        } catch(final Exception e) {
            log.error("Error while processing incomming connection", e);
        } finally {
            try {
                socket.close();
            } catch (final IOException e) {}
        }
    }

    /**
     * @param in input stream.
     * @param out output stream.
     * @throws IOException
     */
    protected void processConnection(final InputStream in, final OutputStream out) throws IOException {
        int numRead = 0;

        byte[] bytes;
        while ((bytes = parser.readMessageData(in)) != null) {
            final RawMessage msg = parser.parseMessage(bytes);
            handler.handleMessage(msg);
            numRead++;

            //write OK response
            out.write(("@ACK," + msg.getPacketIndex() + "#").getBytes());
            out.flush();
        }

        log.debug(numRead + " message have successfully readen by one connection session");
    }
    /**
     * @return the handler
     */
    public RawMessageHandler getHandler() {
        return handler;
    }
    /**
     * @param handler the handler to set
     */
    public void setHandler(final RawMessageHandler handler) {
        this.handler = handler;
    }
}
