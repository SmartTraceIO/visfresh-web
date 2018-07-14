/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WriteBuffer {
    private OutputStream out;

    /**
     *
     */
    public WriteBuffer(final OutputStream out) {
        super();
        this.out = out;
    }

    /**
     * @param bytes
     * @param offset
     * @return
     */
    public void writeOne(final int value) {
        try {
            out.write(value);
        } catch (final IOException e) {
        }
    }
    public void writeTwo(final int value) {
        try {
            out.write((byte) (0xFF & (value >> 8)));
            out.write((byte) (0xFF & value));
        } catch (final IOException e) {
        }
    }
    public void writeFour(final long value) {
        try {
            out.write((byte) (0xFF & (value >> 24)));
            out.write((byte) (0xFF & (value >> 16)));
            out.write((byte) (0xFF & (value >> 8)));
            out.write((byte) (0xFF & value));
        } catch (final IOException e) {
        }
    }

    public void writeImei(final String imei) {
        writeBsdString(imei, 8);
    }
    private void writeBsdString(final String str, final int len) {
        final char[] buff = createCharArray(str, len);

        //write buffer
        try {
            for (int i = 0; i < len; i++) {
                final int b = ((buff[2 * i] - '0') << 4) | (buff[2 * i + 1] - '0');
                out.write(b);
            }
        } catch (final IOException e) {
        }
    }
    public void writeHexString(final String str, final int len) {
        final char[] buff = createCharArray(str, len);

        //write buffer
        try {
            for (int i = 0; i < len; i++) {
                final String tmp = new String(buff, i * 2, 2);
                out.write(Integer.parseInt(tmp));
            }
        } catch (final IOException e) {
        }
    }

    /**
     * @param str
     * @param len
     * @return
     */
    protected char[] createCharArray(final String str, final int len) {
        if (str.length() > len * 2) {
            throw new RuntimeException("String size " + str.length()
                + " is more than requested buffer size " + len);

        }

        final char[] buff = new char[len * 2];
        Arrays.fill(buff, '0');

        //copy string to buffer
        final int offset = buff.length - str.length();
        for (int i = 0; i < str.length(); i++) {
            buff[offset + i] = str.charAt(i);
        }
        return buff;
    }
    public void writeString(final String str, final int len) {
        final byte[] buff = new byte[len];
        Arrays.fill(buff, (byte) ' ');

        final int offset = len - str.length();
        for (int i = 0; i < len; i++) {
            buff[offset + i] = (byte) str.charAt(i);
        }

        try {
            out.write(buff, 0, buff.length);
        } catch (final IOException e) {
        }
    }
    /**
     * @param len
     * @return
     */
    public void writeBytes(final byte[] bytes, final int len) {
        try {
            final int offset = len - bytes.length;
            if (offset > 0) {
                for (int i = 0; i < offset; i++) {
                    out.write(0);
                }
            }

            out.write(bytes);
        } catch (final IOException e) {
        }
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    public void writeMacAddress(final String mac) {
        for (final String str: mac.split(Pattern.quote(":"))) {
            writeHexString(str, 1);
        }
    }
    /**
     * @return
     */
    public void writeAllAsString(final String str) {
        writeBytes(str.getBytes());
    }
    public void writeAllAsBytes(final byte[] bytes) {
        writeBytes(bytes);
    }
    /**
     * @param bytes
     * @return
     */
    private void writeBytes(final byte[] bytes) {
        try {
            out.write(bytes);
        } catch (final IOException e) {
        }
    }
}
