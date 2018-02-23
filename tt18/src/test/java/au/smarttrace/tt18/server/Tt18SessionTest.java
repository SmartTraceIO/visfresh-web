/**
 *
 */
package au.smarttrace.tt18.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.smarttrace.tt18.IncorrectPacketLengthException;
import au.smarttrace.tt18.MessageParserTest;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.junit.FastTest;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(FastTest.class)
public class Tt18SessionTest {
    private AccessibleTt18Session session;

    /**
     * Default constructor.
     */
    public Tt18SessionTest() {
        super();
    }

    @Before
    public void setUp() {
        session = new AccessibleTt18Session();
        //set default handler.
        session.setHandler(m -> {
            return new LinkedList<>();
        });
    }

    @Test
    public void testResponse() throws IOException, IncorrectPacketLengthException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        session.processConnection(new ByteArrayInputStream(MessageParserTest.readTestMessage()), out);
        final String resp = new String(out.toByteArray());

        assertTrue(resp.startsWith("@ACK,"));
        assertTrue(resp.endsWith("#"));
    }
    @Test
    public void testTwoMessages() throws IOException, IncorrectPacketLengthException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //create two messages stream
        final byte[] input = new byte[msg.length * 2];
        System.arraycopy(msg, 0, input, 0, msg.length);
        System.arraycopy(msg, 0, input, msg.length, msg.length);

        //check read two messages
        final List<RawMessage> msgs = new LinkedList<>();
        session.setHandler(m -> {
            msgs.add(m);
            return new LinkedList<>();
        });

        session.processConnection(new ByteArrayInputStream(input), new NullOutputStream());

        assertEquals(2, msgs.size());
    }
    @Test
    public void testCorruptedMessage() throws IOException {
        final byte[] msg = MessageParserTest.readTestMessage();

        //create two messages stream
        final byte[] input = new byte[msg.length  +  msg.length / 2];
        System.arraycopy(msg, 0, input, 0, msg.length);
        System.arraycopy(msg, 0, input, msg.length, msg.length / 2);

        //check read two messages
        final List<RawMessage> msgs = new LinkedList<>();
        session.setHandler(m ->
            {
                msgs.add(m);
                return new LinkedList<>();
            }
        );

        try {
            session.processConnection(new ByteArrayInputStream(input), new NullOutputStream());
            throw new AssertionFailedError("EOF exception should be thrown");
        } catch (final IncorrectPacketLengthException e) {
            //correct
        }

        assertEquals(1, msgs.size());
    }
}
