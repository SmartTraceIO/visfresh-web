/**
 *
 */
package au.smarttrace.tt18;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

        final int len = getPacketLength(header);

        final byte[] msg = new byte[len + header.length];
        //copy header
        System.arraycopy(header, 0, msg, 0, header.length);

        //read message body
        in.read(msg, header.length, len);
        return msg;
    }
    public RawMessage parseMessage(final byte[] bytes) {
        final RawMessage msg = new RawMessage();

        //header
        // Start bits 2 ‘T’ ‘Z’ Tzone company identifier. This is the header of every packet
        msg.setCompany(asString(bytes, 0, 2));
        // Packet length 2 Variable The bytes length from the start at protocol number to theend at the CRC.
        msg.setPacketLength(getPacketLength(bytes));
        // Protocol number 2 ‘$$’ Normal data
        msg.setProtocolNumber(asString(bytes, 4, 2));
        // Hardware type 2 0x04 0x03 The hardware is TT18
        msg.setHardwareType(asHexString(bytes, 6, 2, false));
        // Firmware version 4 Variable 0xFF0xFF0xFF0xFF = 255.255.255.255
        msg.setHardwareVersion(createHardwareVersion(bytes[8], bytes[9], bytes[10], bytes[11]));
        // IMEI 8 Variable BCD format, i.e. 0x08 0x66 0x10 0x40 0x27 0x00 0x34 0x28 = 866104027003428
        msg.setImei(stringFromBcd(bytes, 12, 8, true));

        //RCT time date 6 Variable The time and date when packet the data.
        //The sequence is Year Month Day Hour Minute Second
        msg.setTime(parseTime(bytes[20], bytes[21], bytes[22], bytes[23], bytes[24], bytes[25]));

        //location
        // LBS data length 2 Variable GSM LBS’s data length excludes LBS data length part. If
        // the value is 0, there is no LBS data
        final int lbsDataLength = readTwoBytesValue(bytes, 26);
        if (lbsDataLength > 0) {
            // LAC 2 Variable GSM’s location area code 0x25 0x33 means LAC is 2533
            msg.setLac(Integer.parseInt(stringFromBcd(bytes, 28, 2, false), 16));

            //CELL ID 2 Variable GSM’s serving CELL ID 0x78 0x37 means that CELL ID is 7837
            msg.setCellId(Integer.parseInt(stringFromBcd(bytes, 30, 2, false), 16));

            //MCC 2 Variable Mobile Country Code, ignore the first digital, only 3 digital, 04 60 means that MCC is 460.
            msg.setMcc(getMcc(stringFromBcd(bytes, 32, 2, false)));

            //MNC 2 Variable Mobile Network Code, 2 or 3 digital. If the first digital is 8 , MNC is 3 digital.
            //If the first digital is 0, MNC is 2
            //digital. 87 56 means that MNC is 756. 00 56 means 56.
            msg.setMnc(getMnc(stringFromBcd(bytes, 34, 2, false)));

            //Extension bits A=0. For future extending the protocol use, currently, has nothing, does not possess any byte
        }

        final int statusDataLength = readTwoBytesValue(bytes, 26);
        int offset = 28 + lbsDataLength + 2;
        if (statusDataLength > 0) {
            // Alarm type 1 The value of this part has four possibilities, Temperature/humidity included in all the GPRS data.
            //0xAA Interval GPRS data
            //0x10 Low battery Alarm
            //0xA0 Temperature/Humidity over threshold alarm
            //0xA1 Temperature/Humidity sensor abnormal alarm
        }

        offset += statusDataLength;

        return msg;
    }
    /**
     * @param str
     * MNC 2 Variable Mobile Network Code, 2 or 3 digital. If the first digital is 8 , MNC is 3 digital.
     * If the first digital is 0, MNC is 2
     * digital. 87 56 means that MNC is 756. 00 56 means 56.
     * @return mobile network code.
     */
    private int getMnc(final String str) {
        if (str.charAt(0) == '0') {
            return Integer.parseInt(str.substring(2));
        } else {
            return Integer.parseInt(str.substring(1));
        }
    }
    /**
     * @param num MCC number.
     * @return normalized MMC number.
     */
    private int getMcc(final String num) {
        return Integer.parseInt(num.length() > 3 ? num.substring(1) : num);
    }
    /**
     * @param b1
     * @param b2
     * @param b3
     * @param b4
     * @param b5
     * @param b6
     * @return date.
     */
    private Date parseTime(final byte b1, final byte b2, final byte b3, final byte b4, final byte b5, final byte b6) {
        final Calendar c = new GregorianCalendar();

        c.set(Calendar.YEAR, 2000 + (0xff & b1));
        c.set(Calendar.MONTH, 0xff & b2 - 1);
        c.set(Calendar.DAY_OF_MONTH, 0xff & b3);
        c.set(Calendar.HOUR_OF_DAY, 0xff & b4);
        c.set(Calendar.MINUTE, 0xff & b5);
        c.set(Calendar.SECOND, 0xff & b6);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }
    /**
     * @param bytes bytes.
     * @param offset offset.
     * @param len bytes length.
     * @param cutLeadingZero cutting of leading zero symbol.
     * @return string converted from BCD format.
     */
    private String stringFromBcd(final byte[] bytes, final int offset, final int len, final boolean cutLeadingZero) {
        final char[] chars = new char[len * 2];
        for (int i = 0; i < len; i++) {
            chars[2 * i] = (char) ('0' + (bytes[offset + i] >> 4));
            chars[2 * i + 1] = (char) ('0' + (bytes[offset + i] & 0x0f));
        }
        return chars[0] == '0' && cutLeadingZero ? new String(chars, 1, chars.length - 1) : new String(chars);
    }
    /**
     * @param bytes bytes.
     * @param offset offset.
     * @param len bytes length.
     * @return string converted from BCD format.
     */
    private int intFromBcd(final byte[] bytes, final int offset, final int len) {
        int res = 0;
        for (int i = 0; i < len; i++) {
            final int b1 = (bytes[offset + i] >> 4);
            final int b2 = bytes[offset + i] & 0x0f;

            res = res * 100  + b1 * 10 + b2;
        }
        return res;
    }
    /**
     * @param b1 first byte.
     * @param b2 second byte.
     * @param b3 third byte.
     * @param b4 fourth byte.
     * @return hardware version.
     */
    private String createHardwareVersion(final byte b1, final byte b2, final byte b3, final byte b4) {
        final StringBuilder sb = new StringBuilder();
        sb.append((0xFF & b1));
        sb.append('.');
        sb.append((0xFF & b2));
        sb.append('.');
        sb.append((0xFF & b3));
        sb.append('.');
        sb.append((0xFF & b4));
        return sb.toString();
    }
    /**
     * @param bytes bytes.
     * @param offset offset.
     * @param len length.
     * @param cutFirstZero should cut first zero symbol '0'
     * @return
     */
    private String asHexString(final byte[] bytes, final int offset, final int len, final boolean cutFirstZero) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final String hex = Integer.toString((0xFF & bytes[offset + i]), 16);
            if (hex.length() < 2 && (!cutFirstZero || i > 0)) {
                sb.append('0');
            }

            sb.append(hex);
        }
        return sb.toString();
    }
    /**
     * @param bytes source bytes.
     * @param offset data offset.
     * @param len data length.
     * @return
     */
    private String asString(final byte[] bytes, final int offset, final int len) {
        final char[] buff = new char[len];
        for (int i = 0; i < len; i++) {
            buff[i] = toChar(bytes[offset + i]);
        }
        return asString(buff);
    }
    /**
     * @param chars
     * @return
     */
    private String asString(final char... chars) {
        return new String(chars);
    }
    /**
     * @param header
     * @return
     */
    private int getPacketLength(final byte[] header) {
        return readTwoBytesValue(header, 2) + _0D0A_suffix;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    private int readTwoBytesValue(final byte[] bytes, final int offset) {
        return 0xFF & ((bytes[offset] << 8) | bytes[offset + 1]);
    }
    /**
     * @param b byte.
     * @return character
     */
    private char toChar(final byte b) {
        return (char) (0xFF & b);
    }
}
