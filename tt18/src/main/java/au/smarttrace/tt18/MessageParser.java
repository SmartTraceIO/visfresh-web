/**
 *
 */
package au.smarttrace.tt18;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    private static final int _0D0A_suffix = 2;

    /**
     * Reads only data from one message from stream. Not more extra bytes.
     * @param in input stream.
     * @return message data.
     * @throws IOException
     */
    public byte[] readMessageData(final InputStream in) throws IOException {
        final byte[] header = new byte[4];
        if (in.read(header) < -1) {
            throw new EOFException("Failed to read header data");
        }

        final int len = 0xFF & ((header[2] << 8) | header[3]) + _0D0A_suffix;

        final byte[] msg = new byte[len + header.length];
        //copy header
        System.arraycopy(header, 0, msg, 0, header.length);

        //read message body
        in.read(msg, header.length, len);
        return msg;
    }
}
