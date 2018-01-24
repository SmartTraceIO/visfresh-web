/**
 *
 */
package au.smarttrace.tt18.server;

import static org.junit.Assert.assertEquals;

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
        server.setHandler(m -> {});
        server.start();
    }
    @After
    public void shutDown() {
        if (server != null) {
            server.stop();
        }
    }
    @Test
    public void testTwoMessages() throws IOException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //check read two messages
        final List<RawMessage> msgs = new LinkedList<>();
        server.setHandler(m -> msgs.add(m));

        send(msg, msg);

        assertEquals(2, msgs.size());
    }
    @Test
    public void testCorruptedMessage() throws IOException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //create two messages stream
        final byte[] corrupted = new byte[msg.length / 2];
        System.arraycopy(msg, 0, corrupted, 0, corrupted.length);

        //check read two messages
        final List<RawMessage> msgs = new LinkedList<>();
        server.setHandler(m -> msgs.add(m));

        send(msg, corrupted);
        assertEquals(1, msgs.size());
    }
    /**
     * @param msgs
     * @throws IOException
     */
    private void send(final byte[]... msgs) throws IOException {
        final Socket  s = new Socket("127.0.0.1", port);
        try {
            final OutputStream out = s.getOutputStream();
            final InputStream in = s.getInputStream();

            for (final byte[] bytes : msgs) {
                out.write(bytes);
                out.flush();

                //read response
                final StringBuilder sb = new StringBuilder();
                char b = (char) in.read();
                if (b != '@') {
                    throw new IOException("Unexpected first response symbol: " + b);
                }

                sb.append(b);
                while (true) {
                    b = (char) in.read();
                    if (b == -1) {
                        break;
                    }

                    sb.append(b);
                    if (b == '#') {
                        //end of response
                        break;
                    }
                }

                System.out.println(sb);
            }
        } finally {
            s.close();
        }
    }
}
