/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.BroadcastPackageBody.MessageType;
import au.smarttrace.eel.rawdata.InstructionPackageBody.InstructionType;
import au.smarttrace.eel.rawdata.LoginPackageBody.Language;
import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;
import au.smarttrace.eel.rawdata.WarningPackageBody.WarningType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    private static final int HEADER_SIZE = 14;

    /**
     * Reads only data from one message from stream. Not more extra bytes.
     * @param in input stream.
     * @return message data.
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    public byte[] readMessageData(final InputStream in) throws IOException, IncorrectPacketLengthException {
        final byte[] header = new byte[HEADER_SIZE];
        if ((in.read(header)) < header.length) {
            throw new EOFException("Failed to read header data");
        }

        final ReadBuffer tmp = new ReadBuffer(header, 2, 2);
        final int len = tmp.readTwo() - 2 - 8; //-checksum - IMEI, because already read

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
        final ReadBuffer buff = new ReadBuffer(bytes);

        final EelMessage msg = readMessageWithoutPackages(buff);
        msg.setRawData(bytes);

        // read packages
        while (buff.hasData()) {
            final EelPackage pckg = readIncommingPackage(buff);
            msg.getPackages().add(pckg);
        }

        return msg;
    }
    /**
     * @param buff
     * @return
     */
    public EelMessage readMessageWithoutPackages(final ReadBuffer buff) {
        final EelMessage msg = new EelMessage();
        // header
        msg.setMark(buff.readHexString(2));
        msg.setSize(buff.readTwo());
        msg.setCheckSum(buff.readTwo());
        msg.setImei(buff.readImei());
        return msg;
    }
    /**
     * @param buff
     * @param offset
     * @return
     */
    protected EelPackage readIncommingPackage(final ReadBuffer buff) {
        final PackageHeader header = readPackageHeader(buff);
        final PackageBody body = readIncomingPackageBody(buff, header);

        final EelPackage p = new EelPackage();
        p.setHeader(header);
        p.setBody(body);

        return p;
    }
    /**
     * @param buff
     * @param header
     * @return
     */
    protected PackageBody readIncomingPackageBody(final ReadBuffer buff, final PackageHeader header) {
        final ReadBuffer pckgBuff = buff.readToNewBuffer(header.getSize());

        PackageBody body;

        switch (header.getPid()) {
            case Login:
                body = parseLoginPackage(pckgBuff);
                break;
            case Heartbeat:
                body = parseHeartbeatPackage(pckgBuff);
                break;
            case Location:
                body = parseLocationPackage(pckgBuff);
                break;
            case Warning:
                body = parseWarningPackage(pckgBuff);
                break;
            case Message:
                body = parseMessagePackage(pckgBuff);
                break;
            case ParamSet:
                body = parseParamSetPackage(pckgBuff);
                break;
            case Instruction:
                body = parseInstructionPackageResponse(pckgBuff);
                break;
            case Broadcast:
                body = parseBroadcastPackage(pckgBuff);
                break;
            case Undefined:
                body = parseUndefinedPackage(pckgBuff);
                break;
                default:
                    throw new RuntimeException("Unhandled package " + header.getPid());
        }

        if (pckgBuff.hasData()) {
            throw new RuntimeException("Not all data readen");
        }
        return body;
    }
    /**
     * @param buff
     * @return
     */
    public PackageHeader readPackageHeader(final ReadBuffer buff) {
        //        Mark 2 0x67 0x67
        //        PID 1 Package identifier
        //        Size 2 Package size from next byte to end --- Unsigned 16 bits integer
        //        Sequence 2 Package sequence number --- Unsigned 16 bits integer
        //        Content N Package content
        final PackageHeader header = new PackageHeader();
        header.setMark(buff.readHexString(2));
        final int pid = buff.readOne();
        try {
            header.setPid(PackageIdentifier.valueOf(pid));
        } catch (final Exception e) {
            header.setPid(PackageIdentifier.Undefined);
            header.setPidOriginValue(pid);
        }
        header.setSize(buff.readTwo() - 2);
        header.setSequence(buff.readTwo());
        return header;
    }
    private UndefinedPackageBody parseUndefinedPackage(final ReadBuffer pckgBuff) {
        final UndefinedPackageBody p = new UndefinedPackageBody();
        p.setRawData(pckgBuff.readAllAsBytes());
        return p;
    }
    protected BroadcastPackageBody parseBroadcastPackage(final ReadBuffer buff) {
        final BroadcastPackageBody p = new BroadcastPackageBody();
        //Type 1 Broadcast type (see note 1) --- Unsigned 8 bits integer
        p.setType(MessageType.valueOf(buff.readOne()));
        //Number 21 Phone number (see note 2) --- String
        p.setPhoneNumber(buff.readString(21).trim());

        //Content N Message content --- String
        p.setContent(buff.readAllAsString());
        return p;
    }
    protected InstructionPackageResponseBody parseInstructionPackageResponse(final ReadBuffer buff) {
        final InstructionPackageResponseBody p = new InstructionPackageResponseBody();

        //Type 1 Instruction type (see note 1) --- Unsigned 8 bits integer
        p.setType(InstructionType.valueOf(buff.readOne()));
        //UID 4 Instruction UID (see note 2) --- Unsigned 32 bits integer
        p.setUid((int) buff.readFour());

        //Content N Instruction content --- String
        p.setInstructionResult(buff.readAllAsString());

        return p;
    }
    protected InstructionPackageBody parseInstructionPackage(final ReadBuffer buff) {
        final InstructionPackageBody p = new InstructionPackageBody();

        //Type 1 Instruction type (see note 1) --- Unsigned 8 bits integer
        p.setType(InstructionType.valueOf(buff.readOne()));
        //UID 4 Instruction UID (see note 2) --- Unsigned 32 bits integer
        p.setUid((int) buff.readFour());

        //Content N Instruction content --- String
        p.setInstruction(buff.readAllAsString());
        return p;
    }
    protected ParamSetPackageBody parseParamSetPackage(final ReadBuffer buff) {
        final ParamSetPackageBody p = new ParamSetPackageBody();
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        p.setVersion(buff.readTwo());
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        p.setOriginalSize(buff.readTwo());
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        p.setCompressedSize(buff.readTwo());
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        p.setChecksum(buff.readTwo());
        //Offset 2 Uploading offset --- Unsigned 16 bits integer
        p.setOffset(buff.readTwo());

        //Data N Uploading data
        p.setData(buff.readAllAsBytes());
        return p;
    }
    protected MessagePackageBody parseMessagePackage(final ReadBuffer buff) {
        final MessagePackageBody p = new MessagePackageBody();

        //Location N Device position, see Section 3.6 POSITION
        final DevicePosition pos = parseDevicePosition(buff);
        p.setLocation(pos);

        //Number 21 Content N Phone number --- String
        p.setPhoneNumber(buff.readString(21).trim());

        //Message content --- String
        p.setMessage(buff.readAllAsString());
        return p;
    }
    private WarningPackageBody parseWarningPackage(final ReadBuffer buff) {
        final WarningPackageBody p = new WarningPackageBody();
        //Location N Device position, see Section 3.6 POSITION
        final DevicePosition pos = parseDevicePosition(buff);
        p.setLocation(pos);

        //Warning 1 Warning type --- Unsigned 8 bits integer
        p.setWarningType(WarningType.valueOf(buff.readOne()));
        //Status 2 Device status, see Section 3.5 STATUS
        p.setStatus(new Status(buff.readTwo()));
        return p;
    }
    /**
     * @param bytes
     * @param contentOffset
     * @return
     */
    protected LoginPackageBody parseLoginPackage(final ReadBuffer buff) {
        final LoginPackageBody p = new LoginPackageBody();
        //IMEI 8 Device IMEI
        p.setImei(buff.readImei());
        //Language 1 Device language: 0x00 --- Chinese; 0x01 --- English; Other --- Undefined
        p.setLanguage(Language.valueOf(buff.readOne()));
        //Timezone 1 Device timezone --- Signed 8 Bits integer (in 15 mins)
        p.setTimeZone(buff.readOne());
        //Sys Ver 2 System version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        p.setSysVersion(buff.readTwo());
        //App Ver 2 Application version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        p.setAppVersion(buff.readTwo());
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        p.setPsVersion(buff.readTwo());
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        p.setPsOriginSize(buff.readTwo());
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        p.setPsCompressedSize(buff.readTwo());
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        p.setPsChecksum(buff.readTwo());

        return p;
    }
    protected HeartbeatPackageBody parseHeartbeatPackage(final ReadBuffer buff) {
        final HeartbeatPackageBody p = new HeartbeatPackageBody();
        //Status 2 Device status, see Section 3.5 STATUS
        p.setStatus(new Status(buff.readTwo()));
        return p;
    }
    public DefaultPackageResponseBody parseDefaultPackageResponseBody(final ReadBuffer buff) {
        final DefaultPackageResponseBody p = new DefaultPackageResponseBody();
        return p;
    }
    /**
     * @param bytes
     * @param originOffset
     * @param size
     * @return
     */
    protected LocationPackageBody parseLocationPackage(final ReadBuffer buff) {
        final LocationPackageBody p = new LocationPackageBody();

        final DevicePosition pos = parseDevicePosition(buff);
        p.setLocation(pos);

        //Status 2 Device status, see Section 3.5 STATUS
        p.setDeviceStatus(new Status(buff.readTwo()));
        //Battery 2 Battery voltage (in mV) --- Unsigned 16 bits integer
        p.setBattery(buff.readTwo());
        //Mileage 4 Device mileage (in m) --- Unsigned 32 bits integer
        p.setMileage((int) buff.readFour());
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        p.setTemperature(buff.readTwo());
        //Humidity 2 Sensor humidity (in (1/10)%) --- Unsigned 16 bits integer
        p.setHumidity(buff.readTwo());
        //Illuminance 4 Sensor illuminance (in (1/256)lx) --- Unsigned 32 bits integer
        p.setIlluminance((int) buff.readFour());
        //CO2 4 Sensor CO2 concentration (in ppm) --- Unsigned 32 bits integer
        p.setCo2((int) buff.readFour());
        //2 Sensor Accelerometer X axis(in g)--- Unsigned 16 bits integer
        p.setxAcceleration(buff.readTwo());
        //2 Sensor Accelerometer Y axis(in g)--- Unsigned 16 bits integer
        p.setyAcceleration(buff.readTwo());
        //2 Sensor Accelerometer Z axis(in g)--- Unsigned 16 bits integer
        p.setzAcceleration(buff.readTwo());

        if (buff.hasData()) {
            final int beaconCount = buff.readOne();
            p.setBeaconVersion(buff.readOne());

            for (int i = 0; i < beaconCount; i++) {
                p.getBeacons().add(parseBeacon(buff));
            }
        }

        return p;
    }
    protected BeaconData parseBeacon(final ReadBuffer buff) {
        final BeaconData b = new BeaconData();
        //Address 6 The Becaon device Bluetooth address (in big endian)
        b.setAddress(buff.readBeaconAddress());
        //Tppe 1
        b.setTppe((byte) buff.readOne());
        //RSSI 1 Bluetooth signal level --- Signed 8 bits integer (in dB)
        b.setRssi(buff.readOne());
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        b.setBattery(buff.readTwo());
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        b.setTemperature(buff.readTwo());
        return b;
    }
    protected DevicePosition parseDevicePosition(final ReadBuffer buff) {
        // time 4 The event time (UTC) when position data is collected
        final DevicePosition dp = new DevicePosition();
        dp.setTime(buff.readFour());
        //The mask to indicate which data are valid (BIT0 ~ BIT6: GPS, BSID0, BSID1,
        //BSID2, BSS0, BSS1, BSS2)
        final int mask = buff.readOne();
        dp.setMask((byte) mask);

        if ((mask & 1) > 0) { // GPS is valid
            dp.setGpsData(parseGpsData(buff.readToNewBuffer(15)));
        }

        int mcc = 0;
        int mnc = 0;
        if (((mask >> 1) & 1) > 0) { // BSID0 is valid
            // MCC 2 Mobile Country Code --- Unsigned 16 bits integer
            mcc = buff.readTwo();
            // MNC 2 Mobile Network Code --- Unsigned 16 bits integer
            mnc = buff.readTwo();

            dp.getTowerSignals().add(parseTowerSignal(buff.readToNewBuffer(7), mcc, mnc));
        }
        if (((mask >> 2) & 1) > 0) { // BSID1 is valid
            dp.getTowerSignals().add(parseTowerSignal(buff.readToNewBuffer(7), mcc, mnc));
        }
        if (((mask >> 3) & 1) > 0) { // BSID2 is valid
            dp.getTowerSignals().add(parseTowerSignal(buff.readToNewBuffer(7), mcc, mnc));
        }

        if (((mask >> 4) & 1) > 0) { // BSS0
            dp.getWiFiSignals().add(parseWiFiSignal(buff.readToNewBuffer(7)));
        }
        if (((mask >> 5) & 1) > 0) { // BSS1
            dp.getWiFiSignals().add(parseWiFiSignal(buff.readToNewBuffer(7)));
        }
        if (((mask >> 6) & 1) > 0) { // BSS2
            dp.getWiFiSignals().add(parseWiFiSignal(buff.readToNewBuffer(7)));
        }

        return dp;
    }
    private WiFiStationSignal parseWiFiSignal(final ReadBuffer buff) {
        final WiFiStationSignal s = new WiFiStationSignal();
        //BSSID 6 Same as definition in BSS0
        s.setBssid(buff.readMacAddress());
        //RSSI 1 Same as definition in BSS0
        s.setRssi(buff.readOne());
        return s;
    }
    /**
     * @param bytes
     * @param offset
     * @param mcc
     * @param mnc
     * @return
     */
    private GsmStationSignal parseTowerSignal(final ReadBuffer buff, final int mcc, final int mnc) {
        final GsmStationSignal s = new GsmStationSignal();
        s.setMcc(mcc);
        s.setMnc(mnc);

        //LAC 2 Location Area Code --- Unsigned 16 bits integer
        s.setLac(buff.readTwo());
        //CI 4 Cell ID with RNC --- Unsigned 32 bits integer
        s.setRnc(buff.readTwo());
        s.setCid(buff.readTwo());
        //RxLev 1 Cell signal level --- Unsigned 8 bits integer (0: -110dB 1:-109dB 2:-108dB ...110: 0dB)
        s.setRxLevel(buff.readOne());
        return s;
    }
    private GpsData parseGpsData(final ReadBuffer buff) {
        final GpsData gps = new GpsData();
        gps.setLatitude(buff.readFour());
        gps.setLongitude(buff.readFour());
        gps.setAltitude(buff.readTwo());
        gps.setSpeed(buff.readTwo());
        gps.setCourse(buff.readTwo());
        gps.setSatellites(buff.readOne());
        return gps;
    }
}
