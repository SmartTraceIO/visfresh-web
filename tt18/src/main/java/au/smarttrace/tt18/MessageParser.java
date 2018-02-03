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
import java.util.TimeZone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    private static final int _0D0A_suffix = 2;

    private static final int BIT0_MASK = 1;
    private static final int BIT1_MASK = Integer.parseInt("10", 2);
    private static final int BIT2_MASK = Integer.parseInt("100", 2);
    private static final int BIT3_MASK = Integer.parseInt("1000", 2);
    private static final int BIT4_MASK = Integer.parseInt("10000", 2);
    private static final int BIT5_MASK = Integer.parseInt("100000", 2);

    /**
     * Reads only data from one message from stream. Not more extra bytes.
     * @param in input stream.
     * @return message data.
     * @throws IOException
     */
    public byte[] readMessageData(final InputStream in) throws IOException {
        final byte[] header = new byte[4];
        int size;
        if ((size = in.read(header)) < header.length) {
            if (size == -1) {
                //correct behavior, not next data.
                return null;
            }
            throw new EOFException("Failed to read header data");
        }

        final int len = getPacketLength(header);

        final byte[] msg = new byte[len + header.length];
        //copy header
        System.arraycopy(header, 0, msg, 0, header.length);

        //read message body
        if ((size = in.read(msg, header.length, len)) < len) {
            throw new EOFException("Failed to message body, expected: "
                    + len + " bytes, actual: " + size);
        }

        return msg;
    }
    public RawMessage parseMessage(final byte[] bytes) {
        final RawMessage msg = new RawMessage();
        msg.setRawData(bytes);

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
            msg.setLac(readTwoBytesValue(bytes, 28));

            //CELL ID 2 Variable GSM’s serving CELL ID 0x78 0x37 means that CELL ID is 7837
            msg.setCellId(readTwoBytesValue(bytes, 30));

            //MCC 2 Variable Mobile Country Code, ignore the first digital, only 3 digital, 04 60 means that MCC is 460.
            msg.setMcc(getMcc(bytes[32], bytes[33]));

            //MNC 2 Variable Mobile Network Code, 2 or 3 digital. If the first digital is 8 , MNC is 3 digital.
            //If the first digital is 0, MNC is 2
            //digital. 87 56 means that MNC is 756. 00 56 means 56.
            msg.setMnc(getMnc(bytes[32], bytes[33]));

            //Extension bits A=0. For future extending the protocol use, currently, has nothing, does not possess any byte
        }

        int offset = 28 + lbsDataLength;
        final int statusDataLength = readTwoBytesValue(bytes, offset);
        offset += 2;

        if (statusDataLength > 0) {
            // Alarm type 1 Variable The value of this part has four possibilities, Temperature/humidity included in all the GPRS data.
            //0xAA Interval GPRS data
            //0x10 Low battery Alarm
            //0xA0 Temperature/Humidity over threshold alarm
            //0xA1 Temperature/Humidity sensor abnormal alarm
            msg.setAlarm(parseAlarm(0xFF & bytes[offset]));

            // Terminal information 1 Variable
            int b = 0xFF & bytes[offset + 1];
            //Bit 7 to bit 5 are reserved for future use.
            //Bit4: 1 RTC time is abnormal
            //0 RTC time is normal
            msg.setRtcTimeAbnormal((b & BIT4_MASK) != 0);
            //Bit3: 1 The temperature/Humidity sensor is
            //abnormal
            msg.setTimeHumiditySensorAbnormal((b & BIT3_MASK) != 0);
            //0 The temperature/Humidity sensor is normal
            //Bit2: 1 The temperature/Humidity is over threshold
            //0 The temperature/Humidity is normal
            msg.setTemparatureHumidityOverTreshold((b & BIT2_MASK) != 0);
            //Bit1: 1 The battery low voltage
            //0 The battery is normal
            msg.setBatteryLow((b & BIT1_MASK) != 0);
            //Bit0: 1 The machine is charging
            //0 The machine is not charging
            msg.setCharging((b & BIT0_MASK) != 0);

            //GMS signal strength 1 Variable CSQ value , Hex code
            msg.setSignalLevel(0xFF & bytes[offset + 2]);

            //GSM status 1 Variable
            b = 0xFF & bytes[offset + 3];
            //Bit 7 to bit 6 are reserved for future use.
            //Bit 5: 1 Internet connection is established
            //0 Internet connection is not established
            msg.setInternetConnectionEstablished((b & BIT5_MASK) != 0);
            //Bit4: 1 GPRS is registered successfully
            //0 GPRS is not registered
            msg.setGprsIsRegistered((b & BIT4_MASK) != 0);
            //Bit3: 1 The GSM is in roaming mode
            //0 The GSM is in home network mode
            msg.setInRoaming((b & BIT3_MASK) != 0);
            //Bit2: 1 GSM is registered successfully
            //0 GSM is not registered yet
            msg.setGsmIsRegistered((b & BIT2_MASK) != 0);
            //Bit1: 1 Detected SIM card
            //0 Not detected SIM card
            msg.setSimCardDetected((b & BIT1_MASK) != 0);
            //Bit0: 1 The GSM module is started
            //0 The GSM module is not started yet
            msg.setGsmModuleStarted((b & BIT0_MASK) != 0);

            //Battery voltage 2 0 Unit:10mv, for example: 0195H=405(DEC), 405*10mV=4.05V.
            msg.setBattery(readTwoBytesValue(bytes, offset + 4) * 10);

            //Temperature 2 Unit:0.01°C, convert to binary first, mark in the highest bit ,
            b = readTwoBytesValue(bytes, offset + 6);
            //1-disconnect
            //0-connect ,
            if ((b & 0x8000) >> 1 == 0) {
                //negative/positive mark
                //1-the temperature is negative
                //0-the temperature is positive.
                double t = (0x4000 & b) != 0 ? -1. : 1.;
                //Remaining is the temperature value, convert to HEX
                t = t * (b & 0x3FFF) / 100.;
                //first , and multiply 0.01°C.
                //for example:09 DA=25.22°C , 49 DA= - 25.22°C
                //80 00= not connect temperature/humidity sensor
                msg.setTemperature(Double.valueOf(t));
            }

            final int humidity = 0xFF & bytes[offset + 8];
            if (humidity != 0xFF) {
                //Unit:100%, Hex code,
                //for example: 45=69%,
                //FF = not connect temperature /humidity sensor
                msg.setHumidity(Integer.valueOf(humidity));
            }

            //Extension bits B=0 For future use, currently, this part has nothing, does not have any byte
        }

        offset += statusDataLength;
        //Extension bits C=0 For future use, currently, this part has nothing, does not have any byte
        //Packet index 2 Variable The value range of this part is between 1 and 9999
        msg.setPacketIndex(readTwoBytesValue(bytes, offset));
        return msg;
    }
    /**
     * @param value
     * @return
     */
    private Alarm parseAlarm(final int value) {
        // Alarm type 1 The value of this part has four possibilities, Temperature/humidity included in all the GPRS data.
        switch (value) {
            //0xAA Interval GPRS data
            case 0xAA:
            return Alarm.IntervalGPRSdata;
            //0x10 Low battery Alarm
            case 0x10:
            return Alarm.LowBattery;
            //0xA0 Temperature/Humidity over threshold alarm
            case 0xA0:
            return Alarm.TemperatureHumidityOverThreshold;
            //0xA1 Temperature/Humidity sensor abnormal alarm
            case 0xA1:
            return Alarm.TemperatureHumiditySensorAbnormal;
        }

        return null;
    }
    /**
     * @param b1
     * @param b2
     * @return
     */
    private int getMcc(final byte b1, final byte b2) {
        //MCC 2 Variable Mobile Country Code, ignore the first digital, only 3 digital, 04 60 means that MCC is 460.
        final int i1 = (0x0F & b1) << 8;
        final int i2 = 0xFF & b2;
        return i1 | i2;
    }
    /**
     * MNC 2 Variable Mobile Network Code, 2 or 3 digital. If the first digital is 8 , MNC is 3 digital.
     * If the first digital is 0, MNC is 2
     * digital. 87 56 means that MNC is 756. 00 56 means 56.
     * @return mobile network code.
     * @param b1
     * @param b2
     * @return
     */
    private int getMnc(final byte b1, final byte b2) {
        if ((0xF0 & b1) >> 4 == 8) {
            final int i1 = (0x0F & b1) << 8;
            final int i2 = 0xFF & b2;
            return i1 | i2;
        }
        return 0xFF & b2;
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

        //convert to UTC
        long t = c.getTimeInMillis();
        t -= TimeZone.getDefault().getOffset(t);

        return new Date(t);
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
            final int  b = bytes[offset + i] & 0xff ;

            chars[2 * i] = (char) ('0' + (b >> 4));
            chars[2 * i + 1] = (char) ('0' + (b & 0x0f));
        }
        return chars[0] == '0' && cutLeadingZero ? new String(chars, 1, chars.length - 1) : new String(chars);
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
        if (b2 != 0 || b3 != 0 || b4 != 0) {
            sb.append('.');
            sb.append((0xFF & b2));
        }
        if (b3 != 0 || b4 != 0) {
            sb.append('.');
            sb.append((0xFF & b3));
        }
        if (b4 != 0) {
            sb.append('.');
            sb.append((0xFF & b4));
        }
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
        final int i1 = (0xFF & bytes[offset]) << 8;
        final int i2 = 0xFF & bytes[offset + 1];
        return i1 | i2;
    }
    /**
     * @param b byte.
     * @return character
     */
    private char toChar(final byte b) {
        return (char) (0xFF & b);
    }
}
