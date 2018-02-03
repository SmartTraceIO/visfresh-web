/**
 *
 */
package au.smarttrace.tt18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.smarttrace.tt18.junit.FastTest;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(FastTest.class)
public class MessageParserTest {
    private MessageParser parser;

    /**
     * Default constructor.
     */
    public MessageParserTest() {
        super();
    }

    /**
     * Initializes test.
     */
    @Before
    public void setUp() {
        parser = new MessageParser();
    }
    @Test
    public void testReadMessageData() throws IOException {
        final String data[] = IOUtils.toString(MessageParserTest.class.getResource("msg.txt")).split(" +");
        final byte[] msg = parser.readMessageData(new ByteArrayInputStream(decode(data)));

        assertEquals(data.length, msg.length);
    }
    @Test
    public void testEmptyStream() throws IOException {
        assertNull(parser.readMessageData(new ByteArrayInputStream(new byte[0])));
    }
    @Test
    public void testNotFullHeader() throws IOException {
        final byte[] correctData = readTestMessage();
        final byte[] bytes = new byte[correctData.length - 10];
        System.arraycopy(correctData, 0, bytes, 0, bytes.length);

        try {
            assertNull(parser.readMessageData(new ByteArrayInputStream(bytes)));
            throw new AssertionFailedError("EOF exception should be thrown");
        } catch(final EOFException e) {
        }
    }
    @Test
    public void testNotFullBody() throws IOException {
        try {
            assertNull(parser.readMessageData(new ByteArrayInputStream(new byte[2])));
            throw new AssertionFailedError("EOF exception should be thrown");
        } catch(final EOFException e) {
        }
    }
    @Test
    public void testParseMessage() throws IOException {
        final byte[] bytes = readTestMessage();

        final RawMessage msg = parser.parseMessage(bytes);
        assertEquals("TZ", msg.getCompany());
        assertEquals("$$", msg.getProtocolNumber());
        assertEquals("0403", msg.getHardwareType());
    }

    /**
     * @param data the data.
     * @return decoded data
     */
    private static byte[] decode(final String[] data) {
        final byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(data[i], 16);
        }
        return bytes;
    }
    /**
     * @return
     * @throws IOException
     */
    public static byte[] readTestMessage() throws IOException {
        final String msg = IOUtils.toString(MessageParserTest.class.getResource("msg.txt"));
        return decodeMessage(msg);
    }
    /**
     * @param msg
     * @return
     */
    public static byte[] decodeMessage(final String msg) {
        return decode(msg.split(" +"));
    }
}
