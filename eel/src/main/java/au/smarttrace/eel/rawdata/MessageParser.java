/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.AbstractPackage.PackageIdentifier;
import au.smarttrace.eel.rawdata.BroadcastPackage.MessageType;
import au.smarttrace.eel.rawdata.InstructionPackage.InstructionType;
import au.smarttrace.eel.rawdata.LoginPackage.Language;
import au.smarttrace.eel.rawdata.WarningPackage.WarningType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    private static final int HEADER_SIZE = 7;
    private static final int PACKAGE_HEADER_SIZE = 7;

    /**
     * Reads only data from one message from stream. Not more extra bytes.
     * @param in input stream.
     * @return message data.
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    public byte[] readMessageData(final InputStream in) throws IOException, IncorrectPacketLengthException {
        final byte[] header = new byte[HEADER_SIZE];
        int size;
        if ((size = in.read(header)) < header.length) {
            if (size == -1) {
                //correct behavior, not next data.
                return null;
            }
            throw new EOFException("Failed to read header data");
        }

        final int len = getMessageSize(header);

        final byte[] msg = new byte[len + header.length];
        //copy header
        System.arraycopy(header, 0, msg, 0, header.length);

        //read message body
        readBody(in, msg);

        return msg;
    }
    /**
     * @param in
     * @param header
     * @param len
     * @param msg
     * @throws IOException
     * @throws IncorrectPacketLengthException
     * @throws EOFException
     */
    protected void readBody(final InputStream in, final byte[] msg)
            throws IOException, IncorrectPacketLengthException {
        final int len = msg.length - HEADER_SIZE;
        int offset = 0;

        while (offset < len) {
            final int readen = in.read(msg, HEADER_SIZE + offset, len - offset);
            if (readen < 0) {
                final byte[] actual = new byte[HEADER_SIZE + offset];
                System.arraycopy(msg, 0, actual, 0, actual.length);

                throw new IncorrectPacketLengthException(msg.length, actual);
            }
            offset += readen;
        }
    }

    public EelMessage parseMessage(final byte[] bytes) {
        final EelMessage msg = createWithHeaderInfo(bytes);
        return msg;
    }
    /**
     * @param bytes
     * @return
     */
    private EelMessage createWithHeaderInfo(final byte[] bytes) {
        final EelMessage msg = new EelMessage();
        // header
        msg.setMark(asHexString(bytes, 0, 2));
        msg.setSize(getMessageSize(bytes));
        msg.setCheckSumm(0xFF & bytes[4]);
        msg.setImei(parseImei(bytes, 6));

        // read packages
        int offset = 14;
        while (offset < bytes.length) {
            final AbstractPackage pckg = readPackage(bytes, offset);
            offset += pckg.getSize() + 5;
            msg.getPackages().add(pckg);
        }

        return msg;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    protected AbstractPackage readPackage(final byte[] bytes, final int offset) {
//        Mark 2 0x67 0x67
//        PID 1 Package identifier
//        Size 2 Package size from next byte to end --- Unsigned 16 bits integer
//        Sequence 2 Package sequence number --- Unsigned 16 bits integer
//        Content N Package content
        final PackageIdentifier pid = PackageIdentifier.valueOf(0xFF & bytes[offset + 2]);
        final int size = readTwoBytesValue(bytes, offset + 3) - 2;

        AbstractPackage p;

        switch (pid) {
            case Login:
                p = parseLoginPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Heartbeat:
                p = parseHeartbeatPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Location:
                p = parseLocationPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Warning:
                p = parseWarningPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Message:
                p = parseMessagePackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case ParamSet:
                p = parseParamSetPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Instruction:
                p = parseInstructionPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
            case Broadcast:
                p = parseBroadcastPackage(bytes, offset + PACKAGE_HEADER_SIZE, size);
                break;
                default:
                    throw new RuntimeException("Unhandled package " + pid);
        }

//      Mark 2 0x67 0x67
//      PID 1 Package identifier
//      Size 2 Package size from next byte to end --- Unsigned 16 bits integer
//      Sequence 2 Package sequence number --- Unsigned 16 bits integer
//      Content N Package content
        p.setMark(asHexString(bytes, offset, 2));
        p.setPid(pid);
        p.setSize(size);
        p.setSequence(readTwoBytesValue(bytes, offset + 5));

        return p;
    }
    /**
     * @param bytes
     * @param origin
     * @param size
     * @return
     */
    protected BroadcastPackage parseBroadcastPackage(final byte[] bytes,
            final int originOffset, final int size) {
        int offset = originOffset;
        final BroadcastPackage p = new BroadcastPackage();
        //Type 1 Broadcast type (see note 1) --- Unsigned 8 bits integer
        p.setType(MessageType.valueOf(0xFF & bytes[offset]));
        //Number 21 Phone number (see note 2) --- String
        p.setPhoneNumber(asString(bytes, offset + 1, 21).trim());

        offset += 22;
        //Content N Message content --- String
        p.setContent(asString(bytes, offset, size + originOffset - offset));
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    protected InstructionPackage parseInstructionPackage(final byte[] bytes,
            final int originOffset, final int size) {
        int offset = originOffset;
        final InstructionPackage p = new InstructionPackage();

        //Type 1 Instruction type (see note 1) --- Unsigned 8 bits integer
        p.setType(InstructionType.valueOf(0xFF & bytes[offset]));
        //UID 4 Instruction UID (see note 2) --- Unsigned 32 bits integer
        p.setUid(readFourBytesValue(bytes, offset + 1));

        offset += 5;
        //Content N Instruction content --- String
        p.setInstruction(asString(bytes, offset, size + originOffset - offset));
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    protected ParamSetPackage parseParamSetPackage(final byte[] bytes, final int originOffset, final int size) {
        int offset = originOffset;

        final ParamSetPackage p = new ParamSetPackage();
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        p.setVersion(readTwoBytesValue(bytes, offset));
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        p.setOriginalSize(readTwoBytesValue(bytes, offset + 2));
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        p.setCompressedSize(readTwoBytesValue(bytes, offset + 4));
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        p.setChecksum(readTwoBytesValue(bytes, offset + 6));
        //Offset 2 Uploading offset --- Unsigned 16 bits integer
        p.setOffset(readTwoBytesValue(bytes, offset + 8));

        //Data N Uploading data
        offset += 10;
        p.setData(Arrays.copyOfRange(bytes, offset, size + originOffset - offset));
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    protected MessagePackage parseMessagePackage(final byte[] bytes, final int originOffset, final int size) {
        final MessagePackage p = new MessagePackage();
        //Location N Device position, see Section 3.6 POSITION
        int offset = originOffset;
        final DevicePosition pos = parseDevicePosition(bytes, offset);
        p.setLocation(pos);
        offset += pos.getDataSize();

        //Number 21 Content N Phone number --- String
        p.setPhoneNumber(asString(bytes, offset, 21).trim());
        offset += 21;

        //Message content --- String
        p.setMessage(asString(bytes, offset, size + originOffset - offset));
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    private WarningPackage parseWarningPackage(final byte[] bytes, final int originOffset, final int size) {
        final WarningPackage p = new WarningPackage();
        //Location N Device position, see Section 3.6 POSITION
        int offset = originOffset;
        final DevicePosition pos = parseDevicePosition(bytes, offset);
        p.setLocation(pos);
        offset += pos.getDataSize();

        //Warning 1 Warning type --- Unsigned 8 bits integer
        p.setWarningType(WarningType.valueOf(0xFF & bytes[offset]));
        //Status 2 Device status, see Section 3.5 STATUS
        p.setStatus(new Status(0xFF & bytes[offset + 1]));
        return p;
    }
    /**
     * @param bytes
     * @param contentOffset
     * @return
     */
    protected LoginPackage parseLoginPackage(final byte[] bytes, final int contentOffset,
            final int contentSize) {
        final int offset = contentOffset;
        final LoginPackage p = new LoginPackage();
        //IMEI 8 Device IMEI
        p.setImei(parseImei(bytes, offset));
        //Language 1 Device language: 0x00 --- Chinese; 0x01 --- English; Other --- Undefined
        p.setLanguage(Language.valueOf(0xFF & bytes[contentOffset + 8]));
        //Timezone 1 Device timezone --- Signed 8 Bits integer (in 15 mins)
        p.setTimeZone(0xFF & bytes[contentOffset + 9]);
        //Sys Ver 2 System version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        p.setSysVersion(readTwoBytesValue(bytes, contentOffset + 10));
        //App Ver 2 Application version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        p.setAppVersion(readTwoBytesValue(bytes, contentOffset + 12));
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        p.setPsVersion(readTwoBytesValue(bytes, contentOffset + 14));
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        p.setPsOriginSize(readTwoBytesValue(bytes, contentOffset + 16));
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        p.setPsCompressedSize(readTwoBytesValue(bytes, contentOffset + 18));
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        p.setPsChecksum(readTwoBytesValue(bytes, contentOffset + 20));

        return p;
    }
    /**
     * @param bytes
     * @param offset
     * @param size
     * @return
     */
    protected HeartbeatPackage parseHeartbeatPackage(final byte[] bytes, final int offset, final int size) {
        final HeartbeatPackage p = new HeartbeatPackage();
        //Status 2 Device status, see Section 3.5 STATUS
        p.setStatus(new Status(readTwoBytesValue(bytes, offset)));
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    protected LocationPackage parseLocationPackage(final byte[] bytes, final int originOffset, final int size) {
        final LocationPackage p = new LocationPackage();

        int offset = originOffset;
        final DevicePosition pos = parseDevicePosition(bytes, offset);
        p.setLocation(pos);
        offset += pos.getDataSize();

        //Status 2 Device status, see Section 3.5 STATUS
        p.setDeviceStatus(new Status(readTwoBytesValue(bytes, offset)));
        //Battery 2 Battery voltage (in mV) --- Unsigned 16 bits integer
        p.setBattery(readTwoBytesValue(bytes, offset + 2));
        //Mileage 4 Device mileage (in m) --- Unsigned 32 bits integer
        p.setMileage((int) readFourBytesValue(bytes, offset + 4));
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        p.setTemperature(readTwoBytesValue(bytes, offset + 8));
        //Humidity 2 Sensor humidity (in (1/10)%) --- Unsigned 16 bits integer
        p.setHumidity(readTwoBytesValue(bytes, offset + 10));
        //Illuminance 4 Sensor illuminance (in (1/256)lx) --- Unsigned 32 bits integer
        p.setIlluminance((int) readFourBytesValue(bytes, offset + 12));
        //CO2 4 Sensor CO2 concentration (in ppm) --- Unsigned 32 bits integer
        p.setCo2((int) readFourBytesValue(bytes, offset + 16));
        //2 Sensor Accelerometer X axis(in g)--- Unsigned 16 bits integer
        p.setxAcceleration(readTwoBytesValue(bytes, offset + 20));
        //2 Sensor Accelerometer Y axis(in g)--- Unsigned 16 bits integer
        p.setyAcceleration(readTwoBytesValue(bytes, offset + 22));
        //2 Sensor Accelerometer Z axis(in g)--- Unsigned 16 bits integer
        p.setzAcceleration(readTwoBytesValue(bytes, offset + 24));

        final int beaconCount = 0xFF & bytes[offset + 26];
        p.setBeaconVersion(0xFF & bytes[offset + 27]);

        offset+= 28;
        for (int i = 0; i < beaconCount; i++) {
            p.getBeacons().add(parseBeacon(bytes, offset));
            offset += 12;
        }

        return p;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    protected BeaconData parseBeacon(final byte[] bytes, final int offset) {
        final BeaconData b = new BeaconData();
        //Address 6 The Becaon device Bluetooth address (in big endian)
        b.setAddress(Hex.encodeHexString(Arrays.copyOfRange(bytes, offset, offset + 6)));
        //Tppe 1
        b.setTppe(bytes[offset + 6]);
        //RSSI 1 Bluetooth signal level --- Signed 8 bits integer (in dB)
        b.setRssi(0xFF & bytes[offset + 7]);
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        b.setBattery(readTwoBytesValue(bytes, offset + 8));
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        b.setTemperature(readTwoBytesValue(bytes, offset + 10));
        return b;
    }
    /**
     * @param bytes
     * @param originOffset
     * @return
     */
    protected DevicePosition parseDevicePosition(final byte[] bytes, final int offset) {
        // time 4 The event time (UTC) when position data is collected
        final DevicePosition dp = new DevicePosition();
        dp.setTime(readFourBytesValue(bytes, offset));
        //The mask to indicate which data are valid (BIT0 ~ BIT6: GPS, BSID0, BSID1,
        //BSID2, BSS0, BSS1, BSS2)
        final int mask = 0xFF & bytes[offset + 4];
        dp.setMask((byte) mask);

        int localOffset = 5;

        if ((mask & 1) > 0) { // GPS is valid
            dp.setGpsData(parseGpsData(bytes, offset + localOffset));
            localOffset += 15;
        }

        int mcc = 0;
        int mnc = 0;
        if (((mask >> 1) & 1) > 0) { // BSID0 is valid
            // MCC 2 Mobile Country Code --- Unsigned 16 bits integer
            mcc = readTwoBytesValue(bytes, offset + localOffset);
            // MNC 2 Mobile Network Code --- Unsigned 16 bits integer
            mnc = readTwoBytesValue(bytes, offset + localOffset + 2);
            localOffset += 4;

            dp.getTowerSignals().add(parseTowerSignal(bytes, offset + localOffset, mcc, mnc));
            localOffset += 7;
        }

        if (((mask >> 2) & 1) > 0) { // BSID1 is valid
            dp.getTowerSignals().add(parseTowerSignal(bytes, offset + localOffset, mcc, mnc));
            localOffset += 7;
        }

        if (((mask >> 3) & 1) > 0) { // BSID2 is valid
            dp.getTowerSignals().add(parseTowerSignal(bytes, offset + localOffset, mcc, mnc));
            localOffset += 7;
        }

        if (((mask >> 4) & 1) > 0) { // BSS0
            dp.getWiFiSignals().add(parseWiFiSignal(bytes, offset + localOffset));
            localOffset += 7;
        }

        if (((mask >> 5) & 1) > 0) { // BSS1
            dp.getWiFiSignals().add(parseWiFiSignal(bytes, offset + localOffset));
            localOffset += 7;
        }

        if (((mask >> 6) & 1) > 0) { // BSS2
            dp.getWiFiSignals().add(parseWiFiSignal(bytes, offset + localOffset));
            localOffset += 7;
        }

        dp.setDataSize(localOffset);
        return dp;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    private WiFiStationSignal parseWiFiSignal(final byte[] bytes, final int offset) {
        final WiFiStationSignal s = new WiFiStationSignal();
        //BSSID 6 Same as definition in BSS0
        s.setBssid(parseMacAddress(bytes, offset));
        //RSSI 1 Same as definition in BSS0
        s.setRssi(0xFF & bytes[offset + 6]);
        return s;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    private String parseMacAddress(final byte[] bytes, final int offset) {
        final StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) {
                mac.append(':');
            }
            mac.append(asHexString(bytes, offset + i, 1));
        }

        return mac.toString();
    }
    /**
     * @param bytes
     * @param offset
     * @param mcc
     * @param mnc
     * @return
     */
    private GsmStationSignal parseTowerSignal(final byte[] bytes, final int offset, final int mcc, final int mnc) {
        final GsmStationSignal s = new GsmStationSignal();
        s.setMcc(mcc);
        s.setMnc(mnc);
        //LAC 2 Same as definition in BSID0
        s.setLac(readTwoBytesValue(bytes, offset));
        //CI 4 Same as definition in BSID0
        s.setCid((int) readFourBytesValue(bytes, offset + 2));
        //RxLev 1 Same as definition in BSID0
        s.setRxLevel(0xFF & bytes[offset + 6]);
        return s;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    private GpsData parseGpsData(final byte[] bytes, final int offset) {
        final GpsData gps = new GpsData();
        gps.setLatitude(readFourBytesValue(bytes, offset));
        gps.setLongitude(readFourBytesValue(bytes, offset + 4));
        gps.setAltitude(readTwoBytesValue(bytes, offset + 8));
        gps.setSpeed(readTwoBytesValue(bytes, offset + 10));
        gps.setCourse(readTwoBytesValue(bytes, offset + 12));
        gps.setSatellites(0xFF & bytes[offset + 14]);
        return gps;
    }
    /**
     * @param bytes bytes.
     * @param offset offset.
     * @param len length.
     * @return
     */
    private String asHexString(final byte[] bytes, final int offset, final int len) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final String hex = Integer.toString((0xFF & bytes[offset + i]), 16);
            if (hex.length() < 2) {
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
            buff[i] = (char) (0xFF & bytes[offset + i]);
        }
        return new String(buff);
    }
    /**
     * @param header
     * @return
     */
    private int getMessageSize(final byte[] header) {
        return readTwoBytesValue(header, 2);
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
     * @param bytes
     * @param offset
     * @return
     */
    private long readFourBytesValue(final byte[] bytes, final int offset) {
        final int i1 = (0xFF & bytes[offset]) << 24;
        final int i2 = (0xFF & bytes[offset + 1]) << 16;
        final int i3 = (0xFF & bytes[offset + 2]) << 8;
        final int i4 = 0xFF & bytes[offset + 3];
        return i1 | i2 | i3 | i4;
    }
    /**
     * @param bytes
     * @param offset
     * @return
     */
    private String parseImei(final byte[] bytes, final int offset) {
        return stringFromBcd(bytes, offset, 8, false);
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
        return chars[0] == '0' && cutLeadingZero ? new String(chars, 1, chars.length - 1)
                : new String(chars);
    }
    /**
     * @param mask
     * @return
     */
    @SuppressWarnings("unused") // for testings
    private String printMask(final int mask) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (((mask >> i) & 1) == 1) {
                sb.insert(0, '1');
            } else {
                sb.insert(0, '0');
            }
        }
        return sb.toString();
    }
}
