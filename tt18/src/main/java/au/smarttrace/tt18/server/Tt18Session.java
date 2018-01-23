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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tt18Session implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Tt18Session.class);

    /**
     * Socket.
     */
    private Socket socket;

    /**
     * @param so socket.
     */
    public Tt18Session(final Socket so) {
        this.socket = so;
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
     */
    protected void processConnection(final InputStream in, final OutputStream out) {
        // TODO Auto-generated method stub
    }
}
