/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageWriter {
    private static final String MARK = "6767";

    /**
     * Default constructor.
     */
    public MessageWriter() {
        super();
    }


    /**
     * @param response
     * @return
     */
    public byte[] writeMessage(final EelMessage response) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            writeMessage(response, bout);
        } catch (final IOException e) {
        }
        return bout.toByteArray();
    }
    /**
     * @param response
     * @return
     * @throws IOException
     */
    public void writeMessage(final EelMessage msg, final OutputStream out) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final WriteBuffer bodyBuff = new WriteBuffer(bout);

        bodyBuff.writeImei(msg.getImei());
        for (final AbstractPackage p : msg.getPackages()) {
            writePackage(p, bodyBuff);
        }

        // header
        final WriteBuffer msgBuff = new WriteBuffer(out);
        msgBuff.writeHexString(msg.getMark(), 2);
        msgBuff.writeTwo(msg.getSize() + 2);

        final byte[] body = bout.toByteArray();
        msgBuff.writeTwo(calculateCheckSumm(body));
        out.write(body);
    }

    /**
     * @param buff
     * @param offget
     * @return
     */
    protected void writePackage(final AbstractPackage p, final WriteBuffer buff) {
        final ByteArrayOutputStream pckgOut = new ByteArrayOutputStream();
        final WriteBuffer pckgBuff = new WriteBuffer(pckgOut);

        pckgBuff.writeTwo(p.getSequence());
        switch (p.getPid()) {
            case Login:
                writeLoginPackage((LoginPackage) p, pckgBuff);
                break;
            case Heartbeat:
                writeHeartbeatPackage((HeartbeatPackage) p, pckgBuff);
                break;
            case Location:
                writeLocationPackage((LocationPackage) p, pckgBuff);
                break;
            case Warning:
                writeWarningPackage((WarningPackage) p, pckgBuff);
                break;
            case Message:
                writeMessagePackage((MessagePackage) p, pckgBuff);
                break;
            case ParamSet:
                writeParamSetPackage((ParamSetPackage) p, pckgBuff);
                break;
            case Instruction:
                writeInstructionPackage((InstructionPackage) p, pckgBuff);
                break;
            case Broadcast:
                writeBroadcastPackage((BroadcastPackage) p, pckgBuff);
                break;
                default:
                    throw new RuntimeException("Unhandled package " + p.getPid());
        }

        buff.writeHexString(MARK, 2);
        buff.writeOne(p.getPid().getValue());

        final byte[] data = pckgOut.toByteArray();
        buff.writeTwo(data.length + 2);
        buff.writeBytes(data, data.length);
    }
    protected void writeBroadcastPackage(final BroadcastPackage p, final WriteBuffer buff) {
        //Type 1 Broadcast type (see note 1) --- Unsigned 8 bits integer
        buff.writeOne(p.getType().getValue());
        //Number 21 Phone number (see note 2) --- String
        buff.writeString(p.getPhoneNumber(), 21);

        //Content N Message content --- String
        buff.writeAllAsString(p.getContent());
    }
    protected void writeInstructionPackage(final InstructionPackage p, final WriteBuffer buff) {
        //Type 1 Instruction type (see note 1) --- Unsigned 8 bits integer
        buff.writeOne(p.getType().getValue());
        //UID 4 Instruction UID (see note 2) --- Unsigned 32 bits integer
        buff.writeFour(p.getUid());

        //Content N Instruction content --- String
        buff.writeAllAsString(p.getInstruction());
    }
    protected void writeParamSetPackage(final ParamSetPackage p, final WriteBuffer buff) {
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        buff.writeTwo(p.getVersion());
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        buff.writeTwo(p.getOriginalSize());
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        buff.writeTwo(p.getCompressedSize());
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        buff.writeTwo(p.getChecksum());
        //Offset 2 Uploading offset --- Unsigned 16 bits integer
        p.getOffset();

        //Data N Uploading data
        buff.writeAllAsBytes(p.getData());
    }
    protected void writeMessagePackage(final MessagePackage p, final WriteBuffer buff) {
        //Location N Device position, see Section 3.6 POSITION
        writeDevicePosition(p.getLocation(), buff);

        //Number 21 Content N Phone number --- String
        buff.writeString(p.getPhoneNumber(), 21);

        //Message content --- String
        buff.writeAllAsString(p.getMessage());
    }
    private WarningPackage writeWarningPackage(final WarningPackage p, final WriteBuffer buff) {
        //Location N Device position, see Section 3.6 POSITION
        writeDevicePosition(p.getLocation(), buff);

        //Warning 1 Warning type --- Unsigned 8 bits integer
        buff.writeOne(p.getWarningType().getValue());
        //Status 2 Device status, see Section 3.5 STATUS
        buff.writeTwo(p.getStatus().getStatus());
        return p;
    }
    /**
     * @param bytes
     * @param contentOffset
     * @return
     */
    protected void writeLoginPackage(final LoginPackage p, final WriteBuffer buff) {
        //IMEI 8 Device IMEI
        buff.writeImei(p.getImei());
        //Language 1 Device language: 0x00 --- Chinese; 0x01 --- English; Other --- Undefined
        buff.writeOne(p.getLanguage().getValue());
        //Timezone 1 Device timezone --- Signed 8 Bits integer (in 15 mins)
        buff.writeOne(p.getTimeZone());
        //Sys Ver 2 System version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        buff.writeTwo(p.getSysVersion());
        //App Ver 2 Application version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
        buff.writeTwo(p.getAppVersion());
        //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
        buff.writeTwo(p.getPsVersion());
        //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
        buff.writeTwo(p.getPsOriginSize());
        //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
        buff.writeTwo(p.getPsCompressedSize());
        //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
        buff.writeTwo(p.getPsChecksum());
    }
    protected void writeHeartbeatPackage(final HeartbeatPackage p, final WriteBuffer buff) {
        //Status 2 Device status, see Section 3.5 STATUS
        buff.writeTwo(p.getStatus().getStatus());
    }
    protected void writeLocationPackage(final LocationPackage p, final WriteBuffer buff) {
        writeDevicePosition(p.getLocation(), buff);

        //Status 2 Device status, see Section 3.5 STATUS
        buff.writeTwo(p.getDeviceStatus().getStatus());
        //Battery 2 Battery voltage (in mV) --- Unsigned 16 bits integer
        buff.writeTwo(p.getBattery());
        //Mileage 4 Device mileage (in m) --- Unsigned 32 bits integer
        buff.writeFour(p.getMileage());
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        buff.writeTwo(p.getTemperature());
        //Humidity 2 Sensor humidity (in (1/10)%) --- Unsigned 16 bits integer
        buff.writeTwo(p.getHumidity());
        //Illuminance 4 Sensor illuminance (in (1/256)lx) --- Unsigned 32 bits integer
        buff.writeFour(p.getIlluminance());
        //CO2 4 Sensor CO2 concentration (in ppm) --- Unsigned 32 bits integer
        buff.writeFour(p.getCo2());
        //2 Sensor Accelerometer X axis(in g)--- Unsigned 16 bits integer
        buff.writeTwo(p.getxAcceleration());
        //2 Sensor Accelerometer Y axis(in g)--- Unsigned 16 bits integer
        buff.writeTwo(p.getyAcceleration());
        //2 Sensor Accelerometer Z axis(in g)--- Unsigned 16 bits integer
        buff.writeTwo(p.getzAcceleration());

        buff.writeOne(p.getBeacons().size());
        buff.writeOne(p.getBeaconVersion());

        for (final BeaconData b: p.getBeacons()) {
            writeBeacon(b, buff);
        }
    }
    protected BeaconData writeBeacon(final BeaconData b, final WriteBuffer buff) {
        //Address 6 The Becaon device Bluetooth address (in big endian)
        try {
            buff.writeBytes(Hex.decodeHex(b.getAddress().toCharArray()), 6);
        } catch (final DecoderException e) {
            throw new RuntimeException("Unable to decode beacon address " + b.getAddress(), e);
        }
        //Tppe 1
        buff.writeOne(b.getTppe());
        //RSSI 1 Bluetooth signal level --- Signed 8 bits integer (in dB)
        buff.writeOne(b.getRssi());
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        buff.writeTwo(b.getBattery());
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        buff.writeTwo(b.getTemperature());
        return b;
    }
    protected void writeDevicePosition(final DevicePosition dp, final WriteBuffer buff) {
        // time 4 The event time (UTC) when position data is collected
        buff.writeFour(dp.getTime());
        //The mask to indicate which data are valid (BIT0 ~ BIT6: GPS, BSID0, BSID1,
        //BSID2, BSS0, BSS1, BSS2)
        int mask = 0;
        if (dp.getGpsData() != null) {
            mask |= 1;
        }

        final int numTowers = dp.getTowerSignals().size();
        if (numTowers > 0) {
            mask |= (1 << 1);

            if (numTowers > 1) {
                mask |= (1 << 2);

                if (numTowers > 2) {
                    mask |= (1 << 3);
                }
            }
        }

        final int numWiFies = dp.getWiFiSignals().size();
        if (numWiFies > 0) {
            mask |= (1 << 4);

            if (numWiFies > 1) {
                mask |= (1 << 5);

                if (numWiFies > 2) {
                    mask |= (1 << 6);
                }
            }
        }

        buff.writeOne(mask);

        if (dp.getGpsData() != null) { // GPS is valid
            writeGpsData(dp.getGpsData(), buff);
        }

        //write mcc, mnc
        if (numTowers > 0) {
            final GsmStationSignal ts = dp.getTowerSignals().get(0);
            // MCC 2 Mobile Country Code --- Unsigned 16 bits integer
            buff.writeTwo(ts.getMcc());
            // MNC 2 Mobile Network Code --- Unsigned 16 bits integer
            buff.writeTwo(ts.getMnc());
        }

        for (final GsmStationSignal s : dp.getTowerSignals()) {
            writeTowerSignal(s, buff);
        }
        for (final WiFiStationSignal s : dp.getWiFiSignals()) {
            writeWiFiSignal(s, buff);
        }
    }
    private void writeWiFiSignal(final WiFiStationSignal s, final WriteBuffer buff) {
        //BSSID 6 Same as definition in BSS0
        buff.writeMacAddress(s.getBssid());
        //RSSI 1 Same as definition in BSS0
        buff.writeOne(s.getRssi());
    }
    /**
     * @param bytes
     * @param offget
     * @param mcc
     * @param mnc
     * @return
     */
    private void writeTowerSignal(final GsmStationSignal s, final WriteBuffer buff) {
        //LAC 2 Same as definition in BSID0
        buff.writeTwo(s.getLac());
        //CI 4 Same as definition in BSID0
        buff.writeFour(s.getCid());
        //RxLev 1 Same as definition in BSID0
        buff.writeOne(s.getRxLevel());
    }
    private void writeGpsData(final GpsData gps, final WriteBuffer buff) {
        buff.writeFour(gps.getLatitude());
        buff.writeFour(gps.getLongitude());
        buff.writeTwo((int) gps.getAltitude());
        buff.writeTwo(gps.getSpeed());
        buff.writeTwo(gps.getCourse());
        buff.writeOne(gps.getSatellites());
    }
    /**
     * @param body
     * @return
     */
    private int calculateCheckSumm(final byte[] body) {
        short sum16 = 0;
        for (final byte b : body) {
            sum16 = (short) ((0xFF & (sum16 << 1) | (sum16 >> 15)) + b);
        }
        return sum16;
    }
}