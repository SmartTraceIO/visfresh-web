/**
 *
 */
package au.smarttrace.tt18;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParserTest {

    /**
     * Default constructor.
     */
    public MessageParserTest() {
        super();
    }

    @Test
    public void testReadMessageData() throws IOException {
        final String data[] = IOUtils.toString(MessageParserTest.class.getResource("msg.txt")).split(" +");
        final byte[] msg = new MessageParser().readMessageData(new ByteArrayInputStream(decode(data)));

        assertEquals(data.length, msg.length);
    }

    /**
     * @param data the data.
     * @return decoded data
     */
    private byte[] decode(final String[] data) {
        final byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(data[i], 16);
        }
        return bytes;
    }
}
