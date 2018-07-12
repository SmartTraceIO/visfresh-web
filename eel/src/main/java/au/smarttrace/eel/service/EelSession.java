/**
 *
 */
package au.smarttrace.eel.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.MessageParser;
import au.smarttrace.eel.rawdata.RawMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EelSession implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EelSession.class);
    private final MessageParser parser = new MessageParser();
    private RawMessageHandler handler;

    /**
     * Socket.
     */
    private Socket socket;

    /**
     * @param so socket.
     */
    public EelSession(final Socket so, final RawMessageHandler handler) {
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
        final boolean isCommandSent = false;
        String device = null;

        try {
            byte[] bytes;
            while ((bytes = parser.readMessageData(in)) != null) {
                log.debug("Message has recieved: " + Hex.encodeHexString(bytes));

                final EelMessage msg = parser.parseMessage(bytes);
                device = msg.getImei();

//                if (shouldCorrecteDate(msg.getTime())) {
//                    //should only correct data on device and not handle message next
//                    log.debug("Message for device " + msg.getImei() + " requres time "
//                            + msg.getTime() + " correction. Time correction will automatically sent");
//                    sendCorrectTimeResponse(out);
//                } else {
//                    try {
//                        final List<String> commands = handler.handleMessage(msg);
//
//                        //write OK response
//                        if (!commands.isEmpty()) {
//                            isCommandSent = true;
//
//                            //send command list
//                            log.debug("Sending command list " + commands + " to device " + msg.getImei());
//                            out.write(String.join("\n", commands).getBytes());
//                            out.write('\n');
//                        }
//                    } catch (final Exception e) {
//                        log.debug("Failed to get commands from DB", e);
//                    }
//
//                    out.write(("@ACK," + msg.getPacketIndex() + "#").getBytes());
//                }

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
