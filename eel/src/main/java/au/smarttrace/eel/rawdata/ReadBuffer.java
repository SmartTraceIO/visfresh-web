/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.Arrays;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadBuffer {
    private byte[] data;
    private int offset;
    private int endExclusive;

    /**
     *
     */
    public ReadBuffer(final byte[] data) {
        this(data, 0, data.length);
    }
    /**
     *
     */
    public ReadBuffer(final byte[] data, final int offset) {
        this(data, offset, data.length - offset);
    }
    /**
     *
     */
    public ReadBuffer(final byte[] data, final int offset, final int size) {
        super();
        this.data = data;
        this.offset = offset;
        if (offset >= data.length) {
            throw new ArrayIndexOutOfBoundsException("byte lenght " + data.length + " less then offset " + offset);
        }
        this.endExclusive = offset + size;
        if (offset + size > data.length) {
            throw new ArrayIndexOutOfBoundsException("Incorrect reqested data size " + size + ", data lenght "
                    + data.length + " less then offset " + offset);
        }
    }

    /**
     * @param bytes
     * @param offset
     * @return
     */
    public int readOne() {
        checkAvailable(1);
        final int i = 0xFF & data[offset];
        offset += 1;
        return i;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    public int readTwo() {
        checkAvailable(2);
        final int i1 = (0xFF & data[offset]) << 8;
        final int i2 = 0xFF & data[offset + 1];
        offset += 2;
        return i1 | i2;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    public long readFour() {
        checkAvailable(4);
        final int i1 = (0xFF & data[offset]) << 24;
        final int i2 = (0xFF & data[offset + 1]) << 16;
        final int i3 = (0xFF & data[offset + 2]) << 8;
        final int i4 = 0xFF & data[offset + 3];
        offset += 4;
        return i1 | i2 | i3 | i4;
    }

    /**
     * @param bytes
     * @param offset
     * @return
     */
    public String readImei() {
        final String imei = readBsdString(8, false);
        //cut zero symbols from end
        int offset = 0;
        while (imei.charAt(offset) == '0') {
            offset++;
        }
        if (offset > 0) {
            return imei.substring(offset);
        }

        return imei;
    }

    /**
     * @param bytes
     *            bytes.
     * @param offset
     *            offset.
     * @param len
     *            bytes length.
     * @param cutLeadingZero
     *            cutting of leading zero symbol.
     * @return string converted from BCD format.
     */
    private String readBsdString(final int len, final boolean cutLeadingZero) {
        checkAvailable(len);
        final char[] chars = new char[len * 2];
        for (int i = 0; i < len; i++) {
            final int b = data[offset + i] & 0xff;

            chars[2 * i] = (char) ('0' + (b >> 4));
            chars[2 * i + 1] = (char) ('0' + (b & 0x0f));
        }
        offset += len;
        return chars[0] == '0' && cutLeadingZero ? new String(chars, 1, chars.length - 1) : new String(chars);
    }

    /**
     * @param bytes
     *            bytes.
     * @param offset
     *            offset.
     * @param len
     *            length.
     * @return
     */
    public String readHexString(final int len) {
        checkAvailable(len);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final String hex = Integer.toString((0xFF & data[offset + i]), 16);
            if (hex.length() < 2) {
                sb.append('0');
            }

            sb.append(hex);
        }

        offset += len;
        return sb.toString();
    }

    /**
     * @param bytes
     *            source bytes.
     * @param offset
     *            data offset.
     * @param len
     *            data length.
     * @return
     */
    public String readString(final int len) {
        checkAvailable(len);
        final char[] buff = new char[len];
        for (int i = 0; i < len; i++) {
            buff[i] = (char) (0xFF & data[offset + i]);
        }
        offset += len;
        return new String(buff);
    }
    public ReadBuffer readToNewBuffer(final int size) {
        checkAvailable(size);
        final ReadBuffer b = new ReadBuffer(data, offset, size);
        offset += size;
        return b;
    }
    /**
     * @param len
     * @return
     */
    public byte[] readBytes(final int len) {
        checkAvailable(len);
        final byte[] bytes = Arrays.copyOfRange(data, offset, offset + len);
        offset += len;
        return bytes;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    public String readMacAddress() {
        final StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) {
                mac.append(':');
            }
            mac.append(readHexString(1));
        }

        return mac.toString();
    }
    /**
     * @return
     */
    public String readAllAsString() {
        return new String(readAllAsBytes());
    }
    /**
     * @return
     */
    public byte[] readAllAsBytes() {
        return readBytes(endExclusive - offset);
    }
    /**
     * @param len
     */
    private void checkAvailable(final int len) {
        if (!hasData(len)) {
            throw new ArrayIndexOutOfBoundsException(
                    "Incorrect reqested data length " + len + ", avaiable data size is " + (endExclusive - offset));
        }
    }
    /**
     * @return
     */
    public boolean hasData() {
        return hasData(1);
    }
    /**
     * @param len
     * @return
     */
    private boolean hasData(final int len) {
        return offset + len <= endExclusive;
    }
    /**
     * @return current offset.
     */
    public int getCurrentOffset() {
        return offset;
    }
}
