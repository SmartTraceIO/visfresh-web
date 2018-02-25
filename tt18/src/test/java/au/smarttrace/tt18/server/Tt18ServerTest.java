/**
 *
 */
package au.smarttrace.tt18.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.smarttrace.tt18.MessageParserTest;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.junit.TestWithPauses;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(TestWithPauses.class)
public class Tt18ServerTest {
    private Tt18Server server;
    private int port;

    /**
     * Default constructor.
     */
    public Tt18ServerTest() {
        super();
    }

    @Before
    public void startUp() throws IOException {
        //get free port
        final ServerSocket ss = new ServerSocket(0);
        port = ss.getLocalPort();
        ss.close();

        server = new Tt18Server(port, 3000, 5) {};
        //set default handler
        server.setHandler(m -> {
            return new LinkedList<>();
        });
        server.start();
    }
    @After
    public void shutDown() {
        if (server != null) {
            server.stop();
        }
    }
    @Test
    public void testMessageWithCommands() throws IOException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //check read two messages
        server.setHandler(m -> {
            final LinkedList<String> cmd = new LinkedList<>();
            cmd.add("cmd1");
            cmd.add("cmd2");
            return cmd;
        });

        final List<String> msgs= send(msg);

        assertEquals(1, msgs.size());
        final String[] lines = new String(msgs.get(0)).split("\n");

        assertEquals(3, lines.length);
    }

    @Test
    public void testCorruptedMessage() throws IOException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //create two messages stream
        final byte[] corrupted = new byte[msg.length / 2];
        System.arraycopy(msg, 0, corrupted, 0, corrupted.length);

        //check read two messages
        final List<RawMessage> msgs = new LinkedList<>();
        server.setHandler(m -> {
            msgs.add(m);
            return new LinkedList<>();
        });

        send(corrupted);
        assertEquals(0, msgs.size());
    }
    @Test
    public void testCorrectTime() throws IOException {
        final String message = "54 5A 00 2F 24 24 04 03 01 07 00 00 08 66 10 40 27 00 34 28 09 "
                + "01 01 05 03 0D 00 08 25 33 78 37 04 60 00 01 00 09 AA 00 17 37 01 95 09 DA 45 00 0A 57 0A 0D 0A";

        final String response = send(MessageParserTest.decodeMessage(message)).get(0);
        assertTrue(response.startsWith("UTC"));
    }
    /**
     * @param msg
     * @throws IOException
     */
    private List<String> send(final byte[] msg) throws IOException {
        final List<String> responses = new LinkedList<>();
        final Socket  s = new Socket("127.0.0.1", port);
        try {
            final OutputStream out = s.getOutputStream();
            final InputStream in = s.getInputStream();

            out.write(msg);
            out.flush();

            responses.add(readResponse(in));
        } finally {
            s.close();
        }

        return responses;
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private String readResponse(final InputStream in) throws IOException {
        //Welcome to TZONE Gateway Server
        //@UTC,2018-02-09 19:31:32#Server UTC time:2018-02-09 19:31:32
        //@ACK,10#
        //UTC time:2016-08-02 01:19:48
        final String ack = "@ACK,";
        final int timeCorrectionLen = "UTC time:2016-08-02 01:19:48".length();

        //read response
        final StringBuilder sb = new StringBuilder();
        boolean isEndNormal = false;
        boolean isTimeCorrection = false;
        int b;
        while ((b = in.read()) > -1) {
            sb.append((char) b);

            if (isEndNormal) {
                if (b == '#') {
                    break;
                }
            } else if (isTimeCorrection) {
                if (sb.length() == timeCorrectionLen) {
                    break;
                }
            } else if (endsWith(sb, ack)) {
                isEndNormal = true;
            } else if (endsWith(sb, "UTC")) {
                isTimeCorrection = true;
            }
        }

        return sb.toString();
    }

    /**
     * @param sb
     * @param test
     * @return
     */
    private boolean endsWith(final StringBuilder sb, final String test) {
        final int len = sb.length();
        if (len >= test.length() && sb.substring(len - test.length(), len).equals(test)) {
            return true;
        }
        return false;
    }
}
