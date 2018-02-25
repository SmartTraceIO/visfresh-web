/**
 *
 */
package au.smarttrace.tt18.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.smarttrace.tt18.IncorrectPacketLengthException;
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
        } catch (final IncorrectPacketLengthException e) {
            log.error("Failed to read message body, expected: "
                    + e.getExpected() + " bytes, actual: " + e.getActual().length
                    + ", received message as text: " + new String(e.getActual()));

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
     * @throws IncorrectPacketLengthException
     */
    protected void processConnection(final InputStream in, final OutputStream out)
            throws IOException, IncorrectPacketLengthException {
        boolean isCommandSent = false;
        String device = null;

        try {
            byte[] bytes;
            while ((bytes = parser.readMessageData(in)) != null) {
                log.debug("Message has recieved: " + toHexString(bytes));

                final RawMessage msg = parser.parseMessage(bytes);
                device = msg.getImei();

                if (shouldCorrecteDate(msg.getTime())) {
                    //should only correct data on device and not handle message next
                    log.debug("Message for device " + msg.getImei() + " requres time "
                            + msg.getTime() + " correction. Time correction will automatically sent");
                    sendCorrectTimeResponse(out);
                } else {
                    try {
                        final List<String> commands = handler.handleMessage(msg);

                        //write OK response
                        if (!commands.isEmpty()) {
                            isCommandSent = true;

                            //send command list
                            log.debug("Sending command list " + commands + " to device " + msg.getImei());
                            out.write(String.join("\n", commands).getBytes());
                            out.write('\n');
                        }
                    } catch (final Exception e) {
                        log.debug("Failed to get commands from DB", e);
                    }

                    out.write(("@ACK," + msg.getPacketIndex() + "#").getBytes());
                }

                out.flush();

                //not return if send command. Need receive confirmation message or error
                if (!isCommandSent) { //is command sent
                    //just break and close the stream.
                    break;
                } // else continue of receiving data by given session.
            }
        } catch (final IncorrectPacketLengthException e) {
            if (isCommandSent) {
                //if is command sent, not need to propagate error next
                //because it is the response for device command
                log.debug("Response to device " + device
                        + " command has received " + new String(e.getActual()));
            } else {
                throw e;
            }
        }
    }

    /**
     * @param time
     * @return
     */
    private boolean shouldCorrecteDate(final Date time) {
        return time.getTime() < Tt18Server.MIN_ALLOWED_TIME;
    }

    /**
     * Welcome to TZONE Gateway Server
     * @UTC,2018-02-09 19:31:32#Server UTC time:2018-02-09 19:31:32
     * @ACK,10#
     * @param out
     * @throws IOException
     */
    private void sendCorrectTimeResponse(final OutputStream out) throws IOException {
        //create date string.
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long t = System.currentTimeMillis();
        t += TimeZone.getDefault().getOffset(t);
        final String utcTime = df.format(new Date(t));

        //write date correction
        //UTC time:2016-08-02 01:19:48
        out.write(("UTC time:" + utcTime).getBytes());
    }

    /**
     * @param rawData
     * @return
     */
    private String toHexString(final byte[] rawData) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : rawData) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            final String s = Integer.toHexString(0xFF & b);
            if (s.length() < 2) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
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
